package com.xm.ib42.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.xm.ib42.entity.Audio;

import java.util.List;

public class DownLoadManager {
	// 下载状态
	public static final int STATE_DOWNLOADING = 0;// 下载中
	public static final int STATE_PAUSE = 1;// 暂停
	public static final int STATE_WAIT = 2;// 等待
	public static final int STATE_DELETE = 3;// 删除
	public static final int STATE_FAILED = 4;//连接失败
	public static final int STATE_PAUSEING = 5;// 正在暂停
	public static final int STATE_CONNECTION = 6;//连接中
	public static final int STATE_ERROR = 7;//错误
	
	//下载任务-广播动作类型
	public static final String BROADCASTRECEVIER_ACTON="com.xm.ib42.download.brocast";
	
	public static final int FLAG_CHANGED=0;//更新前台
	public static final int FLAG_COMPLETED=1;//下载完成
	public static final int FLAG_FAILED=2;//失败
	public static final int FLAG_WAIT=3;//等待下载
	public static final int FLAG_TIMEOUT=4;//下载超时
	public static final int FLAG_ERROR=5;//发生错误
	public static final int FLAG_COMMON=6;//删除

	//最大下载任务数
	public static final int DOWN_COUNT=3;

	//DownLoadService action
	public static final String SERVICE_ACTION="com.xm.ib42.service.download";
	
	//MediaPlayerServiceO onStart flag
	public static final int SERVICE_DOWNLOAD_STOP=0;//停止下载
	
//	private DownLoadService mDownLoadService;
	private DownloadService mDownLoadService;
	private ContextWrapper mContextWrapper;
	
	public DownLoadManager(ContextWrapper cw) {
		mContextWrapper = cw;
	}
	
	/**
	 * 启动某个下载任务
	 * */
	public void start(Audio audio){
		if(mDownLoadService!=null){
			mDownLoadService.start(audio);
		}
	}
	
	/**
	 * 添加某个下载任务
	 * */
	public void add(Audio audio){
		if(mDownLoadService!=null){
			mDownLoadService.download(audio);
		}
	}

	/**
	 * 添加多个下载任务
	 * */
	public void add(List<Audio> list){
		if(mDownLoadService!=null){
            for (int i = 0; i < list.size(); i++) {
                mDownLoadService.download(list.get(i));
            }
		}
	}

	/**
	 * 删除某个下载任务
	 * */
	public void delete(Audio audio){
		if(mDownLoadService!=null){
			mDownLoadService.delete(audio);
		}
	}
	
	/**
	 * 暂停某个下载任务
	 * */
	public void pause(Audio audio){
		if(mDownLoadService!=null){
			mDownLoadService.pause(audio);
		}
	}
	
	/**
	 * 获取下载数据
	 * */
//	public List<DownLoadInfo> getDownLoadData(){
//		if(mDownLoadService!=null){
//			return mDownLoadService.getDownLoadData();
//		}
//		return null;
//	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mDownLoadService = ((DownloadService.DownloadBinder) service)
					.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			mDownLoadService = null;
		}
	};
	
	/**
	 * 停止下载
	 * */
	public void stop(Audio audio){
		if(mDownLoadService != null){
			mDownLoadService.stop(audio);
        }
	}
	
	/**
	 * 开始服务并绑定服务
	 * */
	public void startAndBindService(){
		Intent lIntent = new Intent(SERVICE_ACTION);
		lIntent.setPackage(mContextWrapper.getPackageName());
		mContextWrapper.startService(lIntent);
        //mContextWrapper.bindService(new Intent(SERVICE_ACTION), mServiceConnection, Context.BIND_AUTO_CREATE);
		mContextWrapper.bindService(lIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

	/**
	 * 取消绑定
	 * */
	public void unbindService(){
        if(mDownLoadService != null){
            mContextWrapper.unbindService(mServiceConnection);
        }
    }
}