package com.xm.ib42.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.xm.ib42.dao.AudioDao;
import com.xm.ib42.entity.Audio;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateUtil implements Serializable {
	private static final long serialVersionUID = 0x00001000L;
	private static final int START = 1;					// 开始下载
	private static final int PUBLISH = 2;				// 更新进度
	private static final int PAUSE = 3;					// 暂停下载
	private static final int CANCEL = 4;				// 取消下载
	private static final int ERROR = 5;					// 下载错误
	private static final int SUCCESS = 6;				// 下载成功
	private static final int GOON = 7;  				// 继续下载

	private static final String UA = "Mozilla/5.0 (Windows NT 6.1; WOW64)" +
			" AppleWebKit/537.36 (KHTML, like Gecko)" +
			" Chrome/37.0.2041.4 Safari/537.36";

	private static ExecutorService mThreadPool;// 线程池

	static {
		mThreadPool = Executors.newFixedThreadPool(5);  // 默认5个
	}

	private String mFileName; 							// 本地保存文件名
	private String mUrl; 								// 下载地址
	private String mLocalPath;							// 本地存放目录

	private boolean isPause = false; 					// 是否暂停
	private boolean isCanceled = false;					// 是否手动停止下载

	/**
	 * 添加下载任务
	 */
	public UpdateUtil(Context context) {
		String localfile = Common.getSdCardPath()
				+ SystemSetting.CACHE_MUSIC_DIRECTORY;
		Common.isExistDirectory(localfile);
		String localPath = localfile + ".apk";
		File file = new File(localPath);
		file = file.getParentFile();
		if (!file.exists()) {
			file.mkdirs();
		}
		String[] tempArray = mUrl.split("/");
		mFileName = tempArray[tempArray.length-1];
		mLocalPath = localPath.replaceAll("\"|\\(|\\)", "");
	}
	

	/**
	 * 开始下载
	 * params isGoon是否为继续下载
	 */
	@SuppressLint("HandlerLeak")
	public void start(final boolean isGoon, Context context) {
		// 处理消息
		final Handler handler = new Handler(context.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ERROR:
					break;
				case CANCEL:
					break;
				case PAUSE:
					break;
				case PUBLISH:
					break;
				case SUCCESS:
					audio.setCacheFinish(true);
					audio.setCachePath(mLocalPath);
					audioDao.updateByCacheState(audio.getId(), mLocalPath);
					break;
				case START:
					break;
				case GOON:
					break;
				}
			}
		};
		
		// 真正开始下载
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				download(isGoon,handler);
			}
		});
	}
	
	/**
	 * 下载方法
	 * @param handler 消息处理器
	 */
	private void download(boolean isGoon, Handler handler) {
		Message msg = null;
		Utils.logD("开始下载。。。");
		try {
			RandomAccessFile localFile =
					new RandomAccessFile(new File(mLocalPath), "rwd");

			DefaultHttpClient client = new DefaultHttpClient();
			client.setParams(getHttpParams());
			HttpGet get = new HttpGet(mUrl);

			long localFileLength = getLocalFileLength();
			final long remoteFileLength = getRemoteFileLength();
			long downloadedLength = localFileLength;
			
			// 远程文件不存在
			if (remoteFileLength == -1l) {
				Utils.logD("下载文件不存在...");
				localFile.close();
				handler.sendEmptyMessage(ERROR);
				return;
			}

			// 本地文件存在
			if (localFileLength > -1l && localFileLength < remoteFileLength) {
				Utils.logD("本地文件存在...");
				localFile.seek(localFileLength);
				get.addHeader("Range", "bytes=" + localFileLength + "-"
						+ remoteFileLength);
			}
			
			msg = Message.obtain();
			
			// 如果不是继续下载
			if(!isGoon) {
				// 发送开始下载的消息并获取文件大小的消息
				msg.what = START;
				msg.obj = remoteFileLength;
			}else {
				msg.what = GOON;
				msg.obj = localFileLength;
			}
			
			handler.sendMessage(msg);
			
			HttpResponse response = client.execute(get);
			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode >= 200 && httpCode <= 300) {
				InputStream in = response.getEntity().getContent();
				byte[] bytes = new byte[1024];
				int len = -1;
				while (-1 != (len = in.read(bytes))) {
					localFile.write(bytes, 0, len);
					downloadedLength += len;
					if ((int)(downloadedLength/
							(float)remoteFileLength * 100) % 10 == 0) {
						// 发送更新进度的消息
						msg = Message.obtain();
						msg.what = PUBLISH;
						msg.obj = downloadedLength;
						handler.sendMessage(msg);
					}
					
					// 暂停下载， 退出方法
					if (isPause) {
						// 发送暂停的消息
						handler.sendEmptyMessage(PAUSE);
						Utils.logD("下载暂停...");
						break;
					}
					
					// 取消下载， 删除文件并退出方法
					if (isCanceled) {
						Utils.logD("手动关闭下载。。");
						localFile.close();
						client.getConnectionManager().shutdown();
						new File(mLocalPath).delete();
						// 发送取消下载的消息
						handler.sendEmptyMessage(CANCEL);
						return;
					}
				}

				localFile.close();
				client.getConnectionManager().shutdown();
//				audio.setSize((int) downloadedLength);
//                audio.setDownFinish(true);
//                audio.setFilePath(getFileName());
//                audio.setDisplayName(getLocalFileName());
				// 发送下载完毕的消息
				if(!isPause) handler.sendEmptyMessage(SUCCESS);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 发送下载错误的消息
			handler.sendEmptyMessage(ERROR);
		}
	}

	/**
	 * 关闭下载， 会删除文件
	 */
	public synchronized void cancel() {
		isCanceled = true;
		if(isPause) {
			new File(mLocalPath).delete();
		}
	}

	/**
	 * 获取本地文件大小
	 * @return 本地文件的大小 or 不存在返回-1
	 */
	public synchronized long getLocalFileLength() {
		long size = -1l;
		File localFile = new File(mLocalPath);
		if (localFile.exists()) {
			size = localFile.length();
		}
		Utils.logD("本地文件大小" + size);
		return size <= 0 ? -1l : size;
	}

	/**
	 * 获取远程文件大小 or 不存在返回-1
	 * @return
	 */
	public synchronized long getRemoteFileLength() {
		long size = -1l;
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			client.setParams(getHttpParams());
			HttpGet get = new HttpGet(mUrl);

			HttpResponse response = client.execute(get);
			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode >= 200 && httpCode <= 300) {
				size = response.getEntity().getContentLength();
			}

			client.getConnectionManager().shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Utils.logD("远程文件大小" + size);
		return size;
	}

	/**
	 * 设置http参数 不能设置soTimeout
	 * @return HttpParams http参数
	 */
	private static HttpParams getHttpParams() {
		HttpParams params = new BasicHttpParams();

		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		HttpProtocolParams.setUseExpectContinue(params, true);
		HttpProtocolParams.setUserAgent(params, UA);
//		ConnManagerParams.setTimeout(params, 10000);
//		HttpConnectionParams.setConnectionTimeout(params, 10000);
		
		return params;
	}
	
	/**
	 * 关闭下载线程池
	 */
	public static void closeDownloadThread() {
		if(null != mThreadPool) {
			mThreadPool.shutdownNow();
		}
	}
}