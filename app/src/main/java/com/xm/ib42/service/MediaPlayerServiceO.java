package com.xm.ib42.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;

import com.xm.ib42.dao.AlbumDao;
import com.xm.ib42.dao.AudioDao;
import com.xm.ib42.entity.Album;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.util.CacheUtil;
import com.xm.ib42.util.Common;
import com.xm.ib42.util.SystemSetting;
import com.xm.ib42.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static android.R.attr.id;


public class MediaPlayerServiceO extends Service {
	private final IBinder mBinder = new MediaPlayerBinder();
	private static final int NOTIFICATIONID = 0;
	// 播放动作
	private static final int ACTION_NEXT = 1;// 下一首播放
	private static final int ACTION_PREVIOUS = 2;// 上一首播放
	private static final int ACTION_AUTO = 0;// 自动执行下一首

	private static final int LATELY_COUNT = 15;// 最近播放的保存数量

	private MediaPlayer mPlayer;
	private List<Audio> list;// 播放歌曲列表
	private Audio audio;// 当前播放的歌曲
	private Album album;// 当前播放的专辑
	private int playerFlag;// Flag
	private int playerState;// 播放状态
	private int playerMode;// 播放模式
	private AlbumDao albumDao;
	private AudioDao audioDao;
	private int albumId;// 播放的专辑id
	private int audioId;// 播放的音频id
	private boolean isRun = true;// 控制更新线程的
	private int currentDuration = 0;// 已经播放时长
	private boolean isFirst = false;// 是否是启动后，第一次播放
	private ExecutorService mExecutorService =null;//线程池
	final Semaphore mSemaphore = new Semaphore(1);
	private boolean isDeleteStop=false;
	private boolean isPrepare=false;

	private String isStartup;


	@Override
	public void onCreate() {
		super.onCreate();
		Utils.logE("onCreate");
		mPlayer = new MediaPlayer();
		list = new ArrayList<>();
		mExecutorService = Executors.newCachedThreadPool();
		audioDao = new AudioDao(this);
		albumDao = new AlbumDao(this);
		isFirst = true;

		init();

		mPlayer.setOnCompletionListener(completionListener);
		mPlayer.setOnBufferingUpdateListener(bufferingUpdateListener);
		mPlayer.setOnErrorListener(errorListener);

		// 申请wake lock保证了CPU维持唤醒状态
		mPlayer.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);

	}

	/**
	 * 初始化信息
	 * */
	private void init() {
		// 获取保存信息
		SystemSetting setting = new SystemSetting(this, false);
		isStartup=setting.getValue(SystemSetting.KEY_ISSTARTUP);
		albumId = setting.getIntValue(SystemSetting.KEY_PLAYER_ALBUMID);
		audioId = setting.getIntValue(SystemSetting.KEY_PLAYER_AUDIOID);
		String t_currentDuration = setting
				.getValue(SystemSetting.KEY_PLAYER_CURRENTDURATION);
		String t_playerMode = setting.getValue(SystemSetting.KEY_PLAYER_MODE);

//		resetPlayerList();

		playerState = MediaPlayerManager.STATE_PAUSE;
        for (int i = 0; i < list.size(); i++) {
            if (audioId == list.get(i).getId()){
                audio = list.get(i);
                break;
            }
        }
		if (!TextUtils.isEmpty(t_currentDuration)) {
			currentDuration = Integer.valueOf(t_currentDuration);
		}
		if (TextUtils.isEmpty(t_playerMode)) {
			playerMode = MediaPlayerManager.MODE_CIRCLELIST;
		} else {
			playerMode = Integer.valueOf(t_playerMode);
			if (playerMode == MediaPlayerManager.MODE_CIRCLEONE) {
				mPlayer.setLooping(true);
			}
		}
	}

	/**
	 * 初始化歌曲信息-扫描之后
	 * */
//	public void initScanner_SongInfo() {
//		if (playerState != MediaPlayerManager.STATE_NULL) {
//			return;
//		}
////		resetPlayerList();
//		playerState = MediaPlayerManager.STATE_PAUSE;
//		if (list.size() != 0) {
//			audio = list.get(0);
//		} else {
//			playerState = MediaPlayerManager.STATE_NULL;
//		}
//		Intent it = new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON);
//		it.putExtra("flag", MediaPlayerManager.FLAG_INIT);
//		it.putExtra("currentPosition", currentDuration);
//		it.putExtra("duration", getPlayerDuration());
//		it.putExtra("title", getTitle());
//		it.putExtra("albumPic", getAlbumPic());
//		sendBroadcast(it);
//
//	}

	/**
	 * 初始化歌曲信息-播放界面进入时
	 * */
	public void initPlayerMain_SongInfo() {
		Intent it = new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON);
		it.putExtra("flag", MediaPlayerManager.FLAG_INIT);
		it.putExtra("playerState", playerState);
		it.putExtra("currentPosition", currentDuration);
		it.putExtra("duration", getPlayerDuration());
		it.putExtra("title", getTitle());
		it.putExtra("albumPic", getAlbumPic());
		it.putExtra("playerMode", playerMode);
		sendBroadcast(it);
	}

	// 播放完成时
	private OnCompletionListener completionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			isRun = false;
			playerState = MediaPlayerManager.STATE_PAUSE;
			doPlayer(ACTION_AUTO,true);
			sendBroadcast(new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON)
					.putExtra("flag", MediaPlayerManager.FLAG_LIST));
		}
	};

	// 缓冲时
	private OnBufferingUpdateListener bufferingUpdateListener = new OnBufferingUpdateListener() {
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			sendBroadcast(new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON)
					.putExtra("flag", MediaPlayerManager.FLAG_BUFFERING)
					.putExtra("percent", percent));
		}
	};

	// 播放发生错误时
	private OnErrorListener errorListener = new OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			doPlayer(ACTION_AUTO, true);
			return true;
		}

	};

	//onStartCommand
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Utils.logE("onStart");
		if(intent.getAction()!=null&&intent.getAction().equals(MediaPlayerManager.SERVICE_ACTION)){
			int flag=intent.getIntExtra("flag", -1);
			if(flag==MediaPlayerManager.SERVICE_RESET_PLAYLIST){
//				resetPlayerList();
			}else if(flag==MediaPlayerManager.SERVICE_MUSIC_PAUSE){
				if(playerState==MediaPlayerManager.STATE_PLAYER){
					pauseOrPlayer();
					sendBroadcast(new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON).putExtra("flag", MediaPlayerManager.FLAG_LIST));
				}
			}else if(flag==MediaPlayerManager.SERVICE_MUSIC_STOP){
				//停止音乐
				stop();
			}else if(flag==MediaPlayerManager.SERVICE_MUSIC_START){

				if(isStartup==null||isStartup.equals("true")){
					isStartup="false";
					new SystemSetting(this, true).setValue(SystemSetting.KEY_ISSTARTUP, "false");
				}
				if(playerState==MediaPlayerManager.STATE_NULL){
					return;
				}
				//顺序列表播放结束
				if(playerState==MediaPlayerManager.STATE_OVER){
					return;
				}
				pauseOrPlayer();
			}else if(flag==MediaPlayerManager.SERVICE_MUSIC_NEXT){

				if(isStartup==null||isStartup.equals("true")){
					isStartup="false";
					new SystemSetting(this, true).setValue(SystemSetting.KEY_ISSTARTUP, "false");
				}

				nextPlayer();
			}else if(flag==MediaPlayerManager.SERVICE_MUSIC_PREV){

				if(isStartup==null||isStartup.equals("true")){
					isStartup="false";
					new SystemSetting(this, true).setValue(SystemSetting.KEY_ISSTARTUP, "false");
				}

				previousPlayer();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Utils.logE("onDestroy");
	}

	/**
	 * 停止服务时
	 * */
	public void stop() {
		// 保存数据[0:歌曲Id 1:已经播放时长 2:播放模式3:播放列表Flag4:播放列表查询参数 5:最近播放的]
		SystemSetting setting = new SystemSetting(this, true);
		String[] playerInfos = new String[6];
        if (!audio.isDownFinish()){
            currentDuration = 0;
        }
        playerInfos[0] = String.valueOf(currentDuration);
		playerInfos[1] = String.valueOf(playerMode);
		playerInfos[2] = albumId+"";
		playerInfos[3] = audio.getId()+"";
		setting.setPlayerInfo(playerInfos);
		setting.setValue(SystemSetting.KEY_ISSTARTUP, "true");
		playerState = MediaPlayerManager.STATE_STOP;
		isRun = false;

		if(mPlayer!=null){
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			mPlayer.release();
		}
		mPlayer = null;
		mExecutorService.shutdown();

		//停止服务
		stopSelf();
	}

	/**
	 * 列表播放完毕时
	 * */
	private void playerOver() {
		playerState = MediaPlayerManager.STATE_OVER;
		audio = null;
		currentDuration = 0;
		Intent it = new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON);
		it.putExtra("flag", MediaPlayerManager.FLAG_INIT);
		it.putExtra("currentPosition", 0);
		it.putExtra("duration", 0);
		it.putExtra("title", getTitle());
		it.putExtra("albumPic", getAlbumPic());
		sendBroadcast(it);
	}

	// 准备
	private void prepare(String path) {
		try {
			mPlayer.setDataSource(path);
			mPlayer.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private class MediaPlayerRunnable implements Runnable {
		@Override
		public void run() {
			try {
				mSemaphore.acquire();
				if(audio ==null){
					mSemaphore.release();
					return;
				}
				mPlayer.reset();
				if (audio.isCacheFinish()) {
                    prepare(audio.getCachePath());
                } else if (audio.isDownFinish()){
                    prepare(audio.getFilePath());
                } else {
                    prepare(audio.getNetUrl());
                    CacheUtil cacheUtil = new CacheUtil(audio, getApplicationContext());
                    cacheUtil.start(false, getApplicationContext());
                }
				// 是否是启动后，第一次播放
				if (isFirst) {
					mPlayer.seekTo(currentDuration);
				} else {
                    mPlayer.seekTo(audio.getCurrDurationTime());
                }
				isFirst = false;
				mPlayer.start();
				isRun = true;
				isDeleteStop=false;
				isPrepare=false;
				playerState = MediaPlayerManager.STATE_PLAYER;
				while (isRun) {
					if (playerState == MediaPlayerManager.STATE_PLAYER) {
						currentDuration = mPlayer.getCurrentPosition();
						Intent intent = new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON);
						intent.putExtra("flag",
								MediaPlayerManager.FLAG_CHANGED);
						intent.putExtra("currentPosition", currentDuration);
						intent.putExtra("audioName", audio.getTitle());
						intent.putExtra("duration", mPlayer.getDuration());
						sendBroadcast(intent);
						Thread.sleep(1000);
					}
				}
				if (mPlayer != null
						&& playerState != MediaPlayerManager.STATE_OVER
						&& playerState != MediaPlayerManager.STATE_STOP) {
					sendBroadcast(new Intent(
							MediaPlayerManager.BROADCASTRECEVIER_ACTON)
							.putExtra("flag", MediaPlayerManager.FLAG_CHANGED)
							.putExtra("currentPosition",
									mPlayer.getCurrentPosition())
                            .putExtra("audioName", audio.getTitle())
							.putExtra("duration", mPlayer.getDuration()));
				}
				if(isPrepare){
					sendBroadcast(new Intent(
							MediaPlayerManager.BROADCASTRECEVIER_ACTON)
							.putExtra("flag",
									MediaPlayerManager.FLAG_CHANGED)
							.putExtra("currentPosition", 0)
                            .putExtra("audioName", audio.getTitle())
							.putExtra("duration", getPlayerDuration()));
				}
				currentDuration=0;
				if(isDeleteStop){
					playerOver();
				}
				mSemaphore.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 播放
	 * */
	private void doPlayer(int action,boolean isPlayer) {
		switch (playerMode) {
			case MediaPlayerManager.MODE_CIRCLELIST:// 顺序播放
				if (list.size() == 1) {
					// 播放完毕后，就没有了
					if (action != ACTION_AUTO) {
						if(isPlayer){
							player();
						}else{
							showPrepare();
						}
					} else {
						playerOver();
					}
				} else {
					int index = -1;
					for (int i = 0, len = list.size(); i < len; i++) {
						// 在列表中查找播放歌曲的位置
						if (list.get(i).getId() == audio.getId()) {
							index = i;
							break;
						}
					}
					if (index != -1) {
						if (audio.getDurationTime() != currentDuration){
							audio.setCurrDurationTime(currentDuration);
							audioDao.updateByDuration(audio.getId(), currentDuration);
						}
						if (action == ACTION_AUTO || action == ACTION_NEXT) {
							// 有下一首
							if (index < (list.size() - 1)) {
								audio = list.get(index + 1);
								if(isPlayer){
									player();
								}else{
									showPrepare();
								}
							} else {
								if (action == ACTION_AUTO) {
									playerOver();
								} else {
									audio = list.get(0);
									if(isPlayer){
										player();
									}else{
										showPrepare();
									}
								}
							}
						} else {// 上一首
							if (index > 0) {
								audio = list.get(index - 1);
								if(isPlayer){
									player();
								}else{
									showPrepare();
								}
							} else {
								if (action == ACTION_AUTO) {
									playerOver();
								} else {
									audio = list.get(list.size() - 1);
									if(isPlayer){
										player();
									}else{
										showPrepare();
									}
								}
							}
						}
						if (audio != null
                                && album != null){
                            album.setAudioName(audio.getTitle());
                            album.setAudioId(audio.getId());
                            albumDao.update(album);
                        }
					}
				}
				break;
		}
	}


	/**
	 * 指定位置播放
	 * */
	public void seekTo(int msec) {
		mPlayer.seekTo(msec);
	}

	/**
	 * 设置播放模式
	 * */
	public void setPlayerMode(int playerMode) {
		this.playerMode = playerMode;
		// 单曲循环
		if (playerMode == MediaPlayerManager.MODE_CIRCLEONE) {
			mPlayer.setLooping(true);
		} else {
			mPlayer.setLooping(false);
		}
	}

	/**
	 * 获取当前播放歌曲的Id
	 * */
	public int getSongId() {
		if (audio == null)
			return -1;
		return audio.getId();
	}

    /**
	 * 获取当前播放歌曲
	 * */
	public Audio getAudio() {
		if (audio == null)
			return null;
		return audio;
	}

	/**
	 * 获取当前播放状态
	 * */
	public int getPlayerState() {
		return playerState;
	}

	/**
	 * 获取当前播放歌曲标题
	 * */
	public String getTitle() {
		if (audio == null) {
			return "印心讲堂";
		}
		// 判断标题是否存在，不存在则显示文件名
		if (TextUtils.isEmpty(audio.getTitle())) {
			return Common.clearSuffix(audio.getDisplayName());
		}
		return audio.getTitle();
	}

	/**
	 * 获取当前播放歌曲的进度
	 * */
	public int getPlayerProgress() {
		return currentDuration;
	}

	/**
	 * 获取当前播放歌曲的时长
	 * */
	public int getPlayerDuration() {
		if (audio == null) {
			return 0;
		}
		int durationTime= audio.getDurationTime();
		//判断扫描文件是否已经获取了歌曲时长
		if(durationTime==-1){
			audio.setDurationTime(getSongDuratonTime(audio.getId(), audio.getDurationTime()));
		}
		return durationTime;
	}

	private int getSongDuratonTime(int id,int durationTime){
		int rs=durationTime;
		MediaPlayer player=MediaPlayer.create(this, Uri.parse(audio.getFilePath()));
		try {
			player.prepare();
			rs=player.getDuration();
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}finally{
			player.release();
			player=null;
		}
		if(rs!=-1){
			audioDao.updateByDuration(id, rs);
		}
		return rs;
	}

	/**
	 * 获取专辑图片
	 * */
	public String getAlbumPic() {
		if (audio == null) {
			return null;
		}
		if (audio.getAlbum() != null){
			Album album = audio.getAlbum();
			return album.getImageUrl();
		}
		return null;
	}


	/**
	 * 获取当前播放模式
	 * */
	public int getPlayerMode() {
		return playerMode;
	}

	/**
	 * 播放下一首
	 * */
	public void nextPlayer() {
		doPlayer(ACTION_NEXT,true);
//		EventBus.getDefault().post(new CommonMessage(Constants.MSG_SONG_PLAY_OVER,null));
	}

	/**
	 * 播放上一首
	 * */
	public void previousPlayer() {
		doPlayer(ACTION_PREVIOUS,true);
	}

	/**
	 * 播放/暂停
	 * */
	public void pauseOrPlayer() {
		if (mPlayer.isPlaying()) {
			mPlayer.pause();
			currentDuration = mPlayer.getCurrentPosition();
			playerState = MediaPlayerManager.STATE_PAUSE;
		} else {
			// 是否是启动后，第一次播放
			if (isFirst) {
				if (audio != null) {
					player(albumId, audio.getId());
				} else {
					currentDuration = 0;
				}
			} else {
				if(isPrepare){
					player();
				}else{
					mPlayer.start();
				}
			}
			playerState = MediaPlayerManager.STATE_PLAYER;
		}
	}

	public int getPlayerFlag() {
		return playerFlag;
	}

	public void setPlayerFlag(int playerFlag) {
		this.playerFlag = playerFlag;
	}

	/**
	 * 播放
	 * */
	private void player() {
		isRun = false;

		if (audio != null && !audio.isDownFinish()) {
			// 准备状态
			playerState = MediaPlayerManager.STATE_PREPARE;
		} else {// 网络音乐-缓冲状态
			playerState = MediaPlayerManager.STATE_BUFFER;
		}
		if (mPlayer.isPlaying()) {
			mPlayer.stop();
		}

		if(audio !=null){
			showPrepare();
		}
		mExecutorService.execute(new MediaPlayerRunnable());
	}


	/**
	 * 根据指定条件播放
	 * */
	public void player(int albumId, int audioId) {
        this.albumId = albumId;
        album = albumDao.searchById(albumId);
//        resetPlayerList();
//		audio = list.get(audioId);
		for (int i = 0; i < list.size(); i++) {
			if (audioId == list.get(i).getId()){
				audio = list.get(i);
				break;
			}
		}
        if (audio == null){
            audio = list.get(0);
        }
		if (audio != null && !audio.isDownFinish()) {
			playerState = MediaPlayerManager.STATE_PLAYER;
			this.audioId = audio.getId();
		} else {
			for (Audio s : list) {
				if (s.getId() == id) {
					audio = s;
					isFirst = false;
					this.audioId = audio.getId();
					break;
				}
			}
		}
        album.setAudioName(audio.getTitle());
        album.setAudioId(audio.getId());
        albumDao.update(album);
		player();
	}

	/**
	 * 显示播放准备信息
	 * */
	private  void showPrepare(){
		sendBroadcast(new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON)
				.putExtra("flag", MediaPlayerManager.FLAG_PREPARE)
				.putExtra("title",getTitle())
				.putExtra("currentPosition", isFirst ? currentDuration : 0)
				.putExtra("duration", getPlayerDuration()));
//				.putExtra("albumPic", audio.getAlbum().getPicPath()));

	}

	/**
	 * 删除歌曲时
	 * */
	public void delete(int songId){
		isFirst=false;
		//删除'播放列表'，就播放全部歌曲
		if(songId==-1){
			isPrepare=true;
			isRun=false;
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			playerState=MediaPlayerManager.STATE_PAUSE;
            albumId = -1;
            audioId = -1;
//			resetPlayerList();
			audio =list.size()>0?list.get(0):null;
			currentDuration=0;
			showPrepare();
			return;
		}
		//单曲模式下，删除当前歌曲
		if(playerMode==MediaPlayerManager.MODE_CIRCLEONE){
			isDeleteStop=true;
			isRun=false;
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
		}else{
			//只有一首歌曲
			if(list.size()<=1){
				isDeleteStop=true;
				isRun=false;
				if (mPlayer.isPlaying()) {
					mPlayer.stop();
				}
			}else{
				//下一首
				if(playerState==MediaPlayerManager.STATE_PAUSE){
					isPrepare=true;
					isRun = false;
					if (mPlayer.isPlaying()) {
						mPlayer.stop();
					}
					doPlayer(ACTION_NEXT, false);
				}else{
					nextPlayer();
				}
				//在列表中删除
				for(int i=0,len=list.size();i<len;i++){
					if(songId==list.get(i).getId()){
						list.remove(i);
						break;
					}
				}
			}
		}
	}


	/**
	 * 重置播放歌曲列表
	 * */
	public void setPlayerList(List<Audio> list) {
        this.list = list;
//        switch (playerFlag){
//            case MediaPlayerManager.PLAYERFLAG_WEB:
//                list = audioDao.searchByAlbum(albumId+"");
//                break;
//            case MediaPlayerManager.PLAYERFLAG_DOWN:
//                list = audioDao.searchDownLoad(albumId+"");
//                break;
//        }
	}

	public class MediaPlayerBinder extends Binder {
		public MediaPlayerServiceO getService() {
			return MediaPlayerServiceO.this;
		}
	}

	public IBinder getBinder() {
		return mBinder;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Utils.logE("onBind");
		return getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return true;
	}

}
