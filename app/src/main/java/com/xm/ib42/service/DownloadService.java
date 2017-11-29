package com.xm.ib42.service;


import android.app.Service;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.SparseArray;
import android.widget.Toast;

import com.xm.ib42.dao.AudioDao;
import com.xm.ib42.dao.DownLoadInfoDao;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.util.Download;
import com.xm.ib42.util.Utils;


/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class DownloadService extends Service {
	private SparseArray<Download> mDownloads = new SparseArray<Download>();
    private AudioDao audioDao;
	private DownLoadInfoDao mDownLoadInfoDao;

	public void start(Audio audio) {
		Download d = mDownloads.get(audio.getId());
		d.pause(false);
	}

	public void delete(Audio audio) {
		Download d = mDownloads.get(audio.getId());
		d.cancel();
	}

	public void pause(Audio audio) {
		Download d = mDownloads.get(audio.getId());
		d.pause(true);
	}

	public void stop(Audio audio) {
		Download d = mDownloads.get(audio.getId());
		d.cancel();
	}

	public class DownloadBinder extends Binder {
		public DownloadService getService() {
			return DownloadService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new DownloadBinder();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
        audioDao = new AudioDao(this);
		mDownLoadInfoDao = new DownLoadInfoDao(this);
	}

	public void download(final Audio audio) {
		if (audio == null)return;;
		Utils.logD("download"+audio.getNetUrl());
		Download d = new Download(audio, mDownLoadInfoDao, getApplicationContext());
		d.setOnDownloadListener(mDownloadListener).start(false);
		mDownloads.put(audio.getId(), d);
	}
	

	private void onDownloadComplete(int downloadId) {
		mDownloads.remove(downloadId);
		if(mDownloads.size() == 0) {
			stopForeground(true);
			return;
		}
		
	}
	/**
	 * 发送广播，通知系统扫描指定的文件
	 * 请参考我的博文：
	 * http://blog.csdn.net/u010156024/article/details/47681851
	 * 
	 */
	private void scanSDCard() {
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// 判断SDK版本是不是4.4或者高于4.4
			String[] paths = new String[]{
					Environment.getExternalStorageDirectory().toString()};
			MediaScannerConnection.scanFile(this, paths, null, null);
		} else {
			Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
			intent.setClassName("com.android.providers.media",
					"com.android.providers.media.MediaScannerReceiver");
//			intent.setData(Uri.parse("file://"+ MusicUtils.getMusicDir()));
			sendBroadcast(intent);
		}
	}


	private Download.OnDownloadListener mDownloadListener = 
			new Download.OnDownloadListener() {
		
		@Override
		public void onSuccess(Audio audio) {
			Utils.logD("download success");
			Toast.makeText(DownloadService.this, 
					mDownloads.get(audio.getId()).getLocalFileName() + "下载完成",
					Toast.LENGTH_SHORT).show();
			onDownloadComplete(audio.getId());
            audioDao.updateByDownLoadState(audio);
		}
		
		@Override
		public void onStart(Audio audio, long fileSize) {
			Utils.logD("download start");
			Toast.makeText(DownloadService.this, "开始下载" +
					mDownloads.get(audio.getId()).getLocalFileName(),
					Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onPublish(Audio audio, long size) {
//			Utils.logD("download", "publish" + size);
		}
		
		@Override
		public void onPause(Audio audio) {
			Utils.logD("download pause");
		}
		
		@Override
		public void onGoon(Audio audio, long localSize) {
			Utils.logD("download goon");
		}
		
		@Override
		public void onError(Audio audio) {
			Utils.logD("download error");
			Toast.makeText(DownloadService.this, 
					mDownloads.get(audio.getId()).getLocalFileName() + "下载失败",
					Toast.LENGTH_SHORT).show();
			onDownloadComplete(audio.getId());
		}
		
		@Override
		public void onCancel(Audio audio) {
			Utils.logD("download cancel");
			onDownloadComplete(audio.getId());
		}
	};
}