package com.xm.ib42.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.xm.ib42.entity.Audio;

import java.util.List;

public class MediaPlayerManager {
	
//	private MediaPlayerServiceO mMediaPlayerService;
	private MediaPlayerService mMediaPlayerService;
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
			mMediaPlayerService = ((MediaPlayerService.MediaPlayerBinder) service)
					.getService();
			if(mConnectionListener!=null){
				mConnectionListener.onServiceConnected();
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			mMediaPlayerService = null;
			if(mConnectionListener!=null){
				mConnectionListener.onServiceDisconnected();
			}
		}
	};
	
	/**
	 * 初始化歌曲信息-播放界面进入时
	 * */
	public void initPlayerMain_SongInfo(){
		if(mMediaPlayerService !=null){
			mMediaPlayerService.initPlayerMain_SongInfo();
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
	 * 停止播放
	 * */
	public void stop(){
        if(mMediaPlayerService != null){
        	mMediaPlayerService.stop();
        }
    }
	
	/**
	 * 取消绑定
	 * */
	public void unbindService(){
        if(mMediaPlayerService != null){
            mContextWrapper.unbindService(mServiceConnection);
        }
    }
	
	/**
	 * 设置播放模式
	 * */
	public void setPlayerMode(int playerMode){
		if(mMediaPlayerService !=null){
			mMediaPlayerService.setPlayerMode(playerMode);
		}
	}

    public int getPlayerFlag() {
        if(mMediaPlayerService !=null){
            return mMediaPlayerService.getPlayerFlag();
        }
        return -1;
    }

    public void setPlayerFlag(int playerFlag) {
        if(mMediaPlayerService !=null){
            mMediaPlayerService.setPlayerFlag(playerFlag);
        }
    }

	/**
	 * 获取专辑图片
	 * */
	public String getAlbumPic(){
		if(mMediaPlayerService !=null){
			mMediaPlayerService.getAlbumPic();
		}
		return null;
	}
	
	/**
	 * 获取当前播放歌曲的Id
	 * */
	public int getSongId(){
		if(mMediaPlayerService !=null){
			return mMediaPlayerService.getSongId();
		}
		return -1;
	}

	/**
	 * 获取当前播放歌曲的Id
	 * */
	public Audio getAudio(){
		if(mMediaPlayerService !=null){
			return mMediaPlayerService.getAudio();
		}
		return null;
	}

	/**
	 * 获取当前播放状态
	 * */
	public int getPlayerState(){
		if(mMediaPlayerService !=null){
			return mMediaPlayerService.getPlayerState();
		}
		return -1;
	}
	
	/**
	 * 指定位置播放
	 * */
	public void seekTo(int msec){
		if(mMediaPlayerService !=null){
			mMediaPlayerService.seekTo(msec);
		}
	}
	
	/**
	 * 获取当前播放歌曲标题
	 * */
	public String getTitle(){
		if(mMediaPlayerService !=null){
			return mMediaPlayerService.getTitle();
		}
		return null;
	}
	
	/**
	 * 获取当前播放歌曲的进度
	 * */
	public int getPlayerProgress(){
		if(mMediaPlayerService !=null){
			return mMediaPlayerService.getPlayerProgress();
		}
		return -1;
	}
	
	/**
	 * 获取当前播放歌曲的时长
	 * */
	public int getPlayerDuration(){
		if(mMediaPlayerService !=null){
			return mMediaPlayerService.getPlayerDuration();
		}
		return -1;
	}
	
	/**
	 * 播放下一首
	 * */
	public void nextPlayer(){
		if(mMediaPlayerService !=null){
//			mMediaPlayerService.nextPlayer();
			mMediaPlayerService.setPlayerFlag(MediaPlayerManager.SERVICE_MUSIC_NEXT);
		}
	}
	
	/**
	 * 播放上一首
	 * */
	public void previousPlayer(){
		if(mMediaPlayerService !=null){
//			mMediaPlayerService.previousPlayer();
			mMediaPlayerService.setPlayerFlag(MediaPlayerManager.SERVICE_MUSIC_PREV);
		}
	}
	
	/**
	 * 播放/暂停
	 * */
	public void pauseOrPlayer(){
		if(mMediaPlayerService !=null){
			mMediaPlayerService.pauseOrPlayer();
		}
	}

	/**
	 * 根据指定条件播放
	 * */
	public void player(int albumId, int audioId){
		if(mMediaPlayerService !=null){
			mMediaPlayerService.player(albumId, audioId);
		}
	}
	
	/**
	 * 重置播放歌曲列表
	 * */
	public void setPlayerList(List<Audio> list){
		if(mMediaPlayerService !=null){
//			mMediaPlayerService.setPlayerList(list);
		}
	}
	
	/**
	 * 获取当前播放模式
	 * */
	public int getPlayerMode(){
		if(mMediaPlayerService !=null){
			return mMediaPlayerService.getPlayerMode();
		}
		return -1;
	}
	
	/**
	 * 初始化歌曲信息-扫描之后
	 * */
//	public void initScanner_SongInfo(){
//		if(mMediaPlayerService!=null){
//			mMediaPlayerService.initScanner_SongInfo();
//		}
//	}
	
	/**
	 * 删除歌曲时
	 * */
	public void delete(int songId){
		if(mMediaPlayerService !=null){
//			mMediaPlayerService.delete(songId);
		}
	}
}
