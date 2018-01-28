package com.xm.ib42.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.xm.ib42.constant.Constants;
import com.xm.ib42.dao.DownLoadInfoDao;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.entity.DownLoadInfo;
import com.xm.ib42.service.DownLoadManager;

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

/**
 * 支持断点下载
 * 支持回调进度、完成、手动关闭、下载失败、暂停/继续、取得文件大小, 获取文件名
 * 支持设置同时下载线程个数
 * 还需要优化
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class Download implements Serializable {
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
		
	private int mDownloadId;							// 下载id
	private String mFileName; 							// 本地保存文件名
	private String mUrl; 								// 下载地址
	private String mLocalPath;							// 本地存放目录

	private boolean isPause = false; 					// 是否暂停
	private boolean isCanceled = false;					// 是否手动停止下载
	
	private OnDownloadListener mListener;		// 监听器

	private Audio audio;
	private DownLoadInfoDao mDownLoadInfoDao;
	private int downLoadInfoId;
    private DownLoadInfo mDownLoadInfo;
    private Context mContext;
	private boolean isDown;

	/**
	 * 配置下载线程池的大小
	 * @param maxSize 同时下载的最大线程数
	 */
	public static void configDownloadTheadPool(int maxSize) {
		mThreadPool = Executors.newFixedThreadPool(maxSize);
	}
	
	/**
	 * 添加下载任务
	 */
	public Download(Audio audio, DownLoadInfoDao mDownLoadInfoDao, Context context) {
        this.mContext = context;
		this.mDownLoadInfoDao = mDownLoadInfoDao;
		String localfile = Common.getSdCardPath()
				+ Constants.DOWNLOAD_MUSIC_DIRECTORY;
		Common.isExistDirectory(localfile);
		String localPath = localfile + audio.getTitle() + ".mp3";
		File file = new File(localPath);
		file = file.getParentFile();
		if (!file.exists()) {
			file.mkdirs();
		}
		mDownloadId = audio.getId();
		this.audio = audio;
		mUrl = audio.getNetUrl();
		String[] tempArray = mUrl.split("/");
		mFileName = tempArray[tempArray.length-1];
		mLocalPath = localPath.replaceAll("\"|\\(|\\)", "");

		// 包装成下载任务类
		mDownLoadInfo = new DownLoadInfo();
		mDownLoadInfo.setAlbum(audio.getAlbum().getTitle());
		mDownLoadInfo.setCompleteSize(0);
		mDownLoadInfo.setDurationTime(audio.getDurationTime());
		mDownLoadInfo.setDisplayName(audio.getDisplayName());
		mDownLoadInfo.setFileSize(audio.getSize());
		mDownLoadInfo.setName(audio.getTitle());
		mDownLoadInfo.setUrl(audio.getNetUrl());
		mDownLoadInfo.setAudioId(audio.getId());
		mDownLoadInfo.setFilePath(mLocalPath);
		mDownLoadInfo.setState(DownLoadManager.STATE_DOWNLOADING);// 设置等待下载
//		 添加到下载任务表
        if (!mDownLoadInfoDao.isExist(audio.getNetUrl())){
            downLoadInfoId = mDownLoadInfoDao.add(mDownLoadInfo);
        } else {
            downLoadInfoId = mDownLoadInfoDao.getId(audio.getNetUrl());
        }
        mDownLoadInfo.setId(downLoadInfoId);

	}
	
	/**
	 * 设置监听器
	 * @param listener 设置下载监听器
	 * @return this
	 */
	public Download setOnDownloadListener(OnDownloadListener listener) {
		mListener = listener;
		return this;
	}
	
	/**
	 * 获取文件名
	 * @return 文件名
	 */
	public String getFileName() {
		return mFileName;
	}
	
	public String getLocalFileName() {
		String[] split = mLocalPath.split(File.separator);
		return split[split.length-1];
	}

    public boolean isDown() {
        return isDown;
    }

    /**
	 * 开始下载
	 * params isGoon是否为继续下载
	 */
	@SuppressLint("HandlerLeak")
	public void start(final boolean isGoon) {
        isDown = true;
		// 处理消息
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ERROR:
					mListener.onError(audio);
					break;
				case CANCEL:
					mListener.onCancel(audio);
					break;
				case PAUSE:
					mListener.onPause(audio);
					break;
				case PUBLISH:
					mListener.onPublish(audio,
							Long.parseLong(msg.obj.toString()));
					break;
				case SUCCESS:
					mDownLoadInfoDao.delete(downLoadInfoId);
                    mDownLoadInfo.setState(3);
					Intent intent = new Intent(Constants.ACTION_DOWN_CON);
					intent.putExtra("downLoadInfo", mDownLoadInfo);
                    mContext.sendBroadcast(intent);
					mListener.onSuccess(audio);
					break;
				case START:
					mListener.onStart(audio,
							Long.parseLong(msg.obj.toString()));
					break;
				case GOON:
					mListener.onGoon(audio,
							Long.parseLong(msg.obj.toString()));
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
                mDownLoadInfo.setFileSize((int) response.getEntity().getContentLength());
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
                        mDownLoadInfo.setState(1);
                        mDownLoadInfo.setCompleteSize(downloadedLength);
                        mDownLoadInfoDao.update(mDownLoadInfo);
                        Intent intent = new Intent(Constants.ACTION_DOWN_CON);
                        intent.putExtra("downLoadInfo", mDownLoadInfo);
                        mContext.sendBroadcast(intent);
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
                        mDownLoadInfoDao.delete(mDownLoadInfo.getId());
                        mDownLoadInfo.setState(2);
                        Intent intent = new Intent(Constants.ACTION_DOWN_CON);
                        intent.putExtra("downLoadInfo", mDownLoadInfo);
						return;
					}
					mDownLoadInfo.setCompleteSize(downloadedLength);
                    mDownLoadInfoDao.update(mDownLoadInfo);
                    Intent intent = new Intent(Constants.ACTION_DOWN_CON);
                    intent.putExtra("downLoadInfo", mDownLoadInfo);
                    mContext.sendBroadcast(intent);
				}

				localFile.close();
				client.getConnectionManager().shutdown();
				audio.setSize((int) downloadedLength);
                audio.setDownFinish(true);
                audio.setFilePath(mLocalPath);
                audio.setDisplayName(getLocalFileName());
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
	 * 暂停/继续下载
	 * param pause 是否暂停下载
	 * 暂停 return true
	 * 继续 return false
	 */
	public synchronized boolean pause(boolean pause) {
		if(!pause) {
			Utils.logD("继续下载");
            isDown = true;
			isPause = false;
			start(true); // 开始下载
		}else {
            isDown = false;
			Utils.logD("暂停下载");
			isPause = true;
		}
		return isPause;
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
	/**
	 * 下载过程中的监听器
	 * 更新下载信息
	 *
	 */
	public interface OnDownloadListener {
		public void onStart(Audio audio, long fileSize);  // 回调开始下载
		public void onPublish(Audio audio, long size);	 // 回调更新进度
		public void onSuccess(Audio audio);				 // 回调下载成功
		public void onPause(Audio audio); 				 // 回调暂停
		public void onError(Audio audio);				 // 回调下载出错
		public void onCancel(Audio audio);			     // 回调取消下载
		public void onGoon(Audio audio, long localSize);  // 回调继续下载
	}
}