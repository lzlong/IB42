package com.xm.ib42.service;

import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.xm.ib42.entity.Audio;

import java.util.List;

public class MediaPlayerManager {
	
//	private MediaPlayerServiceO mMediaPlayerService2;
	private MediaPlayerService2 mMediaPlayerService2;
	private ContextWrapper mContextWrapper;
	
	//播放模式
	/**
	 * 顺序播放 0
	 * */
	public static final int MODE_CIRCLELIST=0;
//	/**
//	 * 随机播放 1
//	 * */
//	public static final int MODE_RANDOM=1;
	/**
	 * 单曲循环 2
	 * */
	public static final int MODE_CIRCLEONE=2;
//	/**
//	 * 列表循环 3
//	 * */
//	public static final int MODE_SEQUENCE=3;


	//播放Flag
	public static final int PLAYERFLAG_WEB=0;//网络
	public static final int PLAYERFLAG_DOWN=1;//全部

	//播放状态
	public static final int STATE_NULL=0;//空闲
	public static final int STATE_BUFFER=1;//缓冲
	public static final int STATE_PAUSE=2;//暂停
	public static final int STATE_PLAYER=3;//播放
	public static final int STATE_PREPARE=4;//准备
	public static final int STATE_OVER=5;//播放结束
	public static final int STATE_STOP=6;//停止
	
	//播放歌曲-广播动作类型
	public static final String BROADCASTRECEVIER_ACTON="com.xm.ib42.player.brocast";

	public static final int FLAG_CHANGED=0;//更新前台
	public static final int FLAG_PREPARE=1;//准备状态
	public static final int FLAG_INIT=2;//初始化数据
	public static final int FLAG_LIST=3;//自动播放时，更新前台列表状态
	public static final int FLAG_BUFFERING=4;//网络音乐-缓冲数据
	

	//MediaPlayerServiceO action
	public static final String SERVICE_ACTION="com.xm.ib42.service.meidaplayer";
		
	//MediaPlayerServiceO onStart flag
	public static final int SERVICE_RESET_PLAYLIST=0;//更新播放列表
	public static final int SERVICE_MUSIC_PAUSE=1;//暂停
	public static final int SERVICE_MUSIC_PLAY=2;//播放
	public static final int SERVICE_MUSIC_START=3;//开始
	public static final int SERVICE_MUSIC_PREV=4;//上一首
	public static final int SERVICE_MUSIC_NEXT=5;//下一首
	public static final int SERVICE_MUSIC_STOP=6;//停止播放
	public static final int SERVICE_MUSIC_NONE=-1;//

	private ServiceConnectionListener mConnectionListener;
	
	public MediaPlayerManager(ContextWrapper cw) {
		mContextWrapper = cw;
	}

	//private

	public interface ServiceConnectionListener{
    	public void onServiceConnected();
    	public void onServiceDisconnected();
	}
	
	public void setConnectionListener(ServiceConnectionListener listener){
    	mConnectionListener = listener;
    }
	
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mMediaPlayerService2 = ((MediaPlayerService2.MediaPlayerBinder) service)
					.getService();
			if(mConnectionListener!=null){
				mConnectionListener.onServiceConnected();
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			mMediaPlayerService2 = null;
			if(mConnectionListener!=null){
				mConnectionListener.onServiceDisconnected();
			}
		}
	};
	
	/**
	 * 初始化歌曲信息-播放界面进入时
	 * */
	public void initPlayerMain_SongInfo(){
		if(mMediaPlayerService2 !=null){
			mMediaPlayerService2.initPlayerMain_SongInfo();
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
//		mContextWrapper.bindService(lIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
	
	/**
	 * 停止播放
	 * */
	public void stop(){
        if(mMediaPlayerService2 != null){
        	mMediaPlayerService2.stop();
        }
    }
	
	/**
	 * 取消绑定
	 * */
	public void unbindService(){
        if(mMediaPlayerService2 != null){
            mContextWrapper.unbindService(mServiceConnection);
        }
    }
	
	/**
	 * 设置播放模式
	 * */
	public void setPlayerMode(int playerMode){
		if(mMediaPlayerService2 !=null){
			mMediaPlayerService2.setPlayerMode(playerMode);
		}
	}

    public int getPlayerFlag() {
        if(mMediaPlayerService2 !=null){
            return mMediaPlayerService2.getPlayerFlag();
        }
        return -1;
    }

    public void setPlayerFlag(int playerFlag) {
        if(mMediaPlayerService2 !=null){
            mMediaPlayerService2.setPlayerFlag(playerFlag);
        }
    }

	/**
	 * 获取专辑图片
	 * */
	public String getAlbumPic(){
		if(mMediaPlayerService2 !=null){
			mMediaPlayerService2.getAlbumPic();
		}
		return null;
	}
	
	/**
	 * 获取当前播放歌曲的Id
	 * */
	public int getSongId(){
		if(mMediaPlayerService2 !=null){
			return mMediaPlayerService2.getSongId();
		}
		return -1;
	}

	/**
	 * 获取当前播放歌曲的Id
	 * */
	public Audio getAudio(){
		if(mMediaPlayerService2 !=null){
			return mMediaPlayerService2.getAudio();
		}
		return null;
	}

	/**
	 * 获取当前播放状态
	 * */
	public int getPlayerState(){
		if(mMediaPlayerService2 !=null){
			return mMediaPlayerService2.getPlayerState();
		}
		return -1;
	}
	
	/**
	 * 指定位置播放
	 * */
	public void seekTo(int msec){
		if(mMediaPlayerService2 !=null){
			mMediaPlayerService2.seekTo(msec);
		}
	}
	
	/**
	 * 获取当前播放歌曲标题
	 * */
	public String getTitle(){
		if(mMediaPlayerService2 !=null){
			return mMediaPlayerService2.getTitle();
		}
		return null;
	}
	
	/**
	 * 获取当前播放歌曲的进度
	 * */
	public int getPlayerProgress(){
		if(mMediaPlayerService2 !=null){
			return mMediaPlayerService2.getPlayerProgress();
		}
		return -1;
	}
	
	/**
	 * 获取当前播放歌曲的时长
	 * */
	public int getPlayerDuration(){
		if(mMediaPlayerService2 !=null){
			return mMediaPlayerService2.getPlayerDuration();
		}
		return -1;
	}
	
	/**
	 * 播放下一首
	 * */
	public void nextPlayer(){
		if(mMediaPlayerService2 !=null){
//			mMediaPlayerService2.nextPlayer();
			mMediaPlayerService2.setPlayerFlag(MediaPlayerManager.SERVICE_MUSIC_NEXT);
		}
	}
	
	/**
	 * 播放上一首
	 * */
	public void previousPlayer(){
		if(mMediaPlayerService2 !=null){
//			mMediaPlayerService2.previousPlayer();
			mMediaPlayerService2.setPlayerFlag(MediaPlayerManager.SERVICE_MUSIC_PREV);
		}
	}
	
	/**
	 * 播放/暂停
	 * */
	public void pauseOrPlayer(){
		if(mMediaPlayerService2 !=null){
			mMediaPlayerService2.pauseOrPlayer();
		}
	}

	/**
	 * 根据指定条件播放
	 * */
	public void player(int albumId){
		if(mMediaPlayerService2 !=null){
			mMediaPlayerService2.player(albumId);
		}
	}
	
	/**
	 * 重置播放歌曲列表
	 * */
	public void setPlayerList(List<Audio> list){
		if(mMediaPlayerService2 !=null){
//			mMediaPlayerService2.setPlayerList(list);
		}
	}
	
	/**
	 * 获取当前播放模式
	 * */
	public int getPlayerMode(){
		if(mMediaPlayerService2 !=null){
			return mMediaPlayerService2.getPlayerMode();
		}
		return -1;
	}
	
	/**
	 * 初始化歌曲信息-扫描之后
	 * */
//	public void initScanner_SongInfo(){
//		if(mMediaPlayerService2!=null){
//			mMediaPlayerService2.initScanner_SongInfo();
//		}
//	}
	
	/**
	 * 删除歌曲时
	 * */
	public void delete(int songId){
		if(mMediaPlayerService2 !=null){
//			mMediaPlayerService2.delete(songId);
		}
	}
}
