package com.xm.ib42.service;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.SparseArray;
import android.widget.Toast;

import com.xm.ib42.constant.Constants;
import com.xm.ib42.dao.AudioDao;
import com.xm.ib42.dao.DownLoadInfoDao;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.entity.DownLoadInfo;
import com.xm.ib42.util.Download;
import com.xm.ib42.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.xm.ib42.app.MyApplication.context;


/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class DownloadService extends Service {
	private List<Download> mDownloads = new ArrayList<>();
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

	private DownBroadcastReceiver mReceiver;

	@Override
	public void onCreate() {
		super.onCreate();
        audioDao = new AudioDao(this);
		mDownLoadInfoDao = new DownLoadInfoDao(this);
        mReceiver = new DownBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_DOWN_PAUSE);
        filter.addAction(Constants.ACTION_DOWN_DOWN);
        filter.addAction(Constants.ACTION_DOWN_DELETE);
        registerReceiver(mReceiver, filter);

        List<DownLoadInfo> list = mDownLoadInfoDao.searchAll();
        for (int i = 0; i < list.size(); i++) {
            Audio audio = audioDao.searchById(list.get(i).getAudioId(), true);
            if (audio != null){
                Download d = new Download(audio, mDownLoadInfoDao, getApplicationContext());
                d.setOnDownloadListener(mDownloadListener);
                if (Build.VERSION.SDK_INT < 23){
                    if (checkState_21()){
                        d.pause(false);
                    }
                } else if(Build.VERSION.SDK_INT >= 23){
                    if (checkState_21orNew()){
                        d.pause(false);
                    }
                }
                mDownloads.add(d);
            }
        }


	}

    //检测当前的网络状态
    //API版本23以下时调用此方法进行检测
    //因为API23后getNetworkInfo(int networkType)方法被弃用
    public boolean checkState_21(){
        //步骤1：通过Context.getSystemService(Context.CONNECTIVITY_SERVICE)获得ConnectivityManager对象
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //步骤2：获取ConnectivityManager对象对应的NetworkInfo对象
        //NetworkInfo对象包含网络连接的所有信息
        //步骤3：根据需要取出网络连接信息
        //获取WIFI连接的信息
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        Boolean isWifiConn = networkInfo.isConnected();

        if (isWifiConn){
            return true;
        }
        return false;
    }

    //API版本23及以上时调用此方法进行网络的检测
    //步骤非常类似
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean checkState_21orNew(){
        //获得ConnectivityManager对象
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //获取所有网络连接的信息
        Network[] networks = connMgr.getAllNetworks();
        //用于存放网络连接信息
        StringBuilder sb = new StringBuilder();
        //通过循环将网络信息逐个取出来
        for (int i=0; i < networks.length; i++){
            //获取ConnectivityManager对象对应的NetworkInfo对象
            NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
            if (networkInfo.isConnected() && networkInfo.getTypeName().equals("WIFI")){
                return true;
            }
        }
        return false;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private int down_count = 0;

    public void download(final Audio audio) {
		if (audio == null)return;;
		Utils.logD("download"+audio.getNetUrl());
		Download d = new Download(audio, mDownLoadInfoDao, getApplicationContext());
		d.setOnDownloadListener(mDownloadListener);
		if (down_count < DownLoadManager.DOWN_COUNT){
			d.start(false);
			down_count++;
		}
		mDownloads.add(d);
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
			down_count--;
			if (mDownloads.size() > down_count){
				mDownloads.get(down_count).start(false);
			}
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
            down_count--;
            if (mDownloads.size() > down_count){
                mDownloads.get(down_count).start(false);
            }
		}
		
		@Override
		public void onCancel(Audio audio) {
			Utils.logD("download cancel");
			onDownloadComplete(audio.getId());
		}
	};

	private class DownBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			DownLoadInfo downLoadInfo = null;
			if (intent.getAction().equals(Constants.ACTION_DOWN_PAUSE)){
                downLoadInfo = (DownLoadInfo) intent.getSerializableExtra("downLoadInfo");
                if (downLoadInfo != null){
                    Download d = mDownloads.get(downLoadInfo.getAudioId());
                    d.pause(true);
                }
			} else if (intent.getAction().equals(Constants.ACTION_DOWN_DOWN)){
                downLoadInfo = (DownLoadInfo) intent.getSerializableExtra("downLoadInfo");
                if (downLoadInfo != null){
                    Download d = mDownloads.get(downLoadInfo.getAudioId());
                    d.pause(false);
                }
			} else if (intent.getAction().equals(Constants.ACTION_DOWN_DELETE)){
                downLoadInfo = (DownLoadInfo) intent.getSerializableExtra("downLoadInfo");
                if (downLoadInfo != null){
                    Download d = mDownloads.get(downLoadInfo.getAudioId());
                    d.cancel();
                }
            }
            if (downLoadInfo != null){
				mDownLoadInfoDao.update(downLoadInfo);
			}
		}
	}

	public int getIndex(){
        for (int i = 0; i < mDownloads.size(); i++) {
            Download download = mDownloads.get(i);
            if (download.isDown()){

            }
        }
    }

}