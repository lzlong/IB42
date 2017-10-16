package com.xm.ib42.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;

import com.xm.ib42.constant.Constants;
import com.xm.ib42.dao.AlbumDao;
import com.xm.ib42.dao.AudioDao;
import com.xm.ib42.entity.Album;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.util.CacheUtil;
import com.xm.ib42.util.Common;
import com.xm.ib42.util.HttpHelper;
import com.xm.ib42.util.SystemSetting;
import com.xm.ib42.util.Utils;

import org.apache.http.HttpResponse;
import org.json.JSONObject;

import java.io.IOException;

import static android.R.attr.id;


public class MediaPlayerService extends Service {
	private final IBinder mBinder = new MediaPlayerBinder();
	// 播放动作
	private static final int ACTION_NEXT = 1;// 下一首播放
	private static final int ACTION_PREVIOUS = 2;// 上一首播放
	private static final int ACTION_AUTO = 0;// 自动执行下一首


	private MediaPlayer mPlayer;
	private Audio audio;// 当前播放的歌曲
	private int audioId;
	private int albumId;
	private int playerFlag;// Flag
	private int playerState;// 播放状态
	private int playerMode;// 播放模式
	private AlbumDao albumDao;
	private AudioDao audioDao;
	public boolean isRun = true;// 控制更新线程的
	private int currentDuration = 0;// 已经播放时长
	private boolean isFirst = false;// 是否是启动后，第一次播放
	private boolean isDeleteStop=false;
	private boolean isPrepare=false;

	private String isStartup;


	@Override
	public void onCreate() {
		super.onCreate();
		Utils.logE("onCreate");
		mPlayer = new MediaPlayer();
		audioDao = new AudioDao(this);
		albumDao = new AlbumDao(this);
		isFirst = true;

//		init();

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
		String t_playerMode = setting.getValue(SystemSetting.KEY_PLAYER_MODE);

		Constants.playAlbum = albumDao.searchById(albumId);
		if (Constants.playAlbum == null)return;
		getData();
		playerState = MediaPlayerManager.STATE_PAUSE;
		if (TextUtils.isEmpty(t_playerMode)) {
			playerMode = MediaPlayerManager.MODE_CIRCLELIST;
		} else {
			playerMode = Integer.valueOf(t_playerMode);
			if (playerMode == MediaPlayerManager.MODE_CIRCLEONE) {
				mPlayer.setLooping(true);
			}
		}


	}

	private void getData(final int what){
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpHelper httpHelper = new HttpHelper();
				httpHelper.connect();
				HttpResponse httpResponse = httpHelper.doGet(Constants.ALBUMSUBURL +
						Constants.playAlbum.getId() + Constants.URLPAGE + Constants.playPage);
				JSONObject json = Utils.parseResponse(httpResponse);
				Constants.playList.addAll(Utils.pressAudioJson(json, Constants.playAlbum));
				if (Constants.playList != null || Constants.playList.size() <= 0){
					mHandler.sendMessage(mHandler.obtainMessage(what));
				}
			}
		}).start();
	}

	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 0){
				//			for (int i = 0; i < list.size(); i++) {
				//				if (audioId == list.get(i).getId()){
				//					audio = list.get(i);
				//					break;
				//				}
				//			}
				currentDuration = audio.getCurrDurationTime();
			} else if (msg.what == 1){
				for (int i = 0; i < Constants.playList.size(); i++) {

				}
			}


		}
	};

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
		new Thread(new MediaPlayerRunnable()).start();
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private class MediaPlayerRunnable implements Runnable {
		@Override
		public void run() {
			try {
				while (isRun) {
					switch (playerFlag) {
						case MediaPlayerManager.SERVICE_MUSIC_START:
							if (audio != null) {
								if (!mPlayer.isPlaying()) {
									if (audio.isCacheFinish()) {
										prepare(audio.getCachePath());
									} else if (audio.isDownFinish()) {
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
							break;
						case MediaPlayerManager.SERVICE_MUSIC_PLAY:
							mPlayer.start();
							break;
						case MediaPlayerManager.SERVICE_MUSIC_PAUSE:
							mPlayer.pause();
							break;
						case MediaPlayerManager.SERVICE_MUSIC_PREV:
							doPlayer(ACTION_PREVIOUS, true);
							break;
						case MediaPlayerManager.SERVICE_MUSIC_NEXT:
							doPlayer(ACTION_NEXT, true);
							break;
						case MediaPlayerManager.SERVICE_MUSIC_STOP:
							stop();
							break;
					}
				}
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
				if (Constants.playList.size() == 1) {
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
					for (int i = 0, len = Constants.playList.size(); i < len; i++) {
						// 在列表中查找播放歌曲的位置
						if (Constants.playList.get(i).getId() == audio.getId()) {
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
							if (index < (Constants.playList.size() - 1)) {
								audio = Constants.playList.get(index + 1);
								if(isPlayer){
									player();
								}else{
									showPrepare();
								}
							} else {
								if (action == ACTION_AUTO) {
									playerOver();
								} else {
									audio = Constants.playList.get(0);
									if(isPlayer){
										player();
									}else{
										showPrepare();
									}
								}
							}
						} else {// 上一首
							if (index > 0) {
								audio = Constants.playList.get(index - 1);
								if(isPlayer){
									player();
								}else{
									showPrepare();
								}
							} else {
								if (action == ACTION_AUTO) {
									playerOver();
								} else {
									audio = Constants.playList.get(Constants.playList.size() - 1);
									if(isPlayer){
										player();
									}else{
										showPrepare();
									}
								}
							}
						}
						if (audio != null
                                && Constants.playAlbum != null){
							Constants.playAlbum.setAudioName(audio.getTitle());
							Constants.playAlbum.setAudioId(audio.getId());
                            albumDao.update(Constants.playAlbum);
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
	public void getAudio(int audioId) {
		audio = null;
		if (Constants.playList != null){
			for (int i = 0; i < Constants.playList.size(); i++) {
				if (audioId == Constants.playList.get(i).getId()){
					audio = Constants.playList.get(i);
				}
			}
		}
		if (audio == null){
			getData();
		}

//		if (audio == null)
//			return null;
//		return audio;
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
	}


	/**
	 * 根据指定条件播放
	 * */
	public void player(int albumId, int audioId) {
        this.albumId = albumId;

		getAudio();

		for (int i = 0; i < Constants.playList.size(); i++) {
			if (audioId == Constants.playList.get(i).getId()){
				audio = Constants.playList.get(i);
				break;
			}
		}
        if (audio == null){
            audio = Constants.playList.get(0);
        }
		if (audio != null && !audio.isDownFinish()) {
			playerState = MediaPlayerManager.STATE_PLAYER;
			this.audioId = audio.getId();
		} else {
			for (Audio s : Constants.playList) {
				if (s.getId() == id) {
					audio = s;
					isFirst = false;
					this.audioId = audio.getId();
					break;
				}
			}
		}
		Constants.playAlbum.setAudioName(audio.getTitle());
		Constants.playAlbum.setAudioId(audio.getId());
        albumDao.update(Constants.playAlbum);
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
				.putExtra("duration", getPlayerDuration())
				.putExtra("albumPic", audio.getAlbum().getImageUrl()));

	}


	public class MediaPlayerBinder extends Binder {
		public MediaPlayerService getService() {
			return MediaPlayerService.this;
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
