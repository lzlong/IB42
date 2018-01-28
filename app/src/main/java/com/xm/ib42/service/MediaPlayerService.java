package com.xm.ib42.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.xm.ib42.app.MyApplication;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.dao.AlbumDao;
import com.xm.ib42.dao.AudioDao;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.util.CacheUtil;
import com.xm.ib42.util.HttpHelper;
import com.xm.ib42.util.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.xm.ib42.app.MyApplication.context;
import static com.xm.ib42.app.MyApplication.mediaPlayer;
import static com.xm.ib42.constant.Constants.ACTION_NEXT;


public class MediaPlayerService extends Service {

	private MyReciever mReceiver;
	private apwReciver apReciver;
	private PhoneStatRec phoneStatRec;
	public static MediaPlayer mPlayer;

//	private ArrayList<Audio> musicList;
	private int current;// 当前播放的歌曲下标
	private int nowcurr = 0;// 当前播放进度
	private int totalms = 0;// 当前歌曲总时长
	private int playmode = 0;// 播放模式 0 顺序播放 1 单曲循环
	public static int status = 1;// 1 未播放 2 暂停 3 播放
	private int mode_current = 0;
	public Context mContext;
	public Audio nowPlayAudio;
	public AlbumDao mAlbumDao;
	public AudioDao mAudioDao;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public class PhoneStatRec extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			TelephonyManager mTelManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			boolean isringpause = false;
			switch (mTelManager.getCallState()) {
				case TelephonyManager.CALL_STATE_RINGING:// 响铃
					if (mPlayer != null && mPlayer.isPlaying()) {
						mPlayer.pause();
						isringpause = true;
					}
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:// 通话
					if (mPlayer != null && mPlayer.isPlaying()) {
						mPlayer.pause();
						isringpause = true;
					}
					break;
				case TelephonyManager.CALL_STATE_IDLE:// 通话结束
					if (mPlayer != null && isringpause == true) {
						mPlayer.start();
						isringpause = false;
					}
					break;
			}
		}
	}

	/* 处理歌曲播放控制的广播 */
	private class MyReciever extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Constants.ACTION_PLAY.equals(intent.getAction())) {
				switch (status) {
					case 1:
						play();
						break;
					case 2:
						mPlayer.start();
						break;
					case 3:
						mPlayer.pause();
                        if (mPlayer.isPlaying()){
                            nowPlayAudio.setCurrDurationTime(mediaPlayer.getCurrentPosition());
                            mAudioDao.updateByDuration(nowPlayAudio.getId(), nowPlayAudio.getCurrDurationTime());
                        }
//						sendBroadcast(new Intent("com.tarena.ispause"));
						status = 2;
						break;
				}
				status = 3;
//				sendBroadcast(new Intent("com.tarena.isplay"));
			}
			//
			else if (Constants.ACTION_HISTORY.equals(intent.getAction())) {
				int position = intent.getIntExtra("position", 0);
				current = position;
				mode_current = current;
			}
			// 暂停
			else if (Constants.ACTION_PAUSE.equals(intent.getAction())) {
				mPlayer.pause();
				status = 2;
//				sendBroadcast(new Intent("com.tarena.ispause"));
			}
			// 停止
			else if (Constants.ACTION_STOP.equals(intent.getAction())) {
				mPlayer.stop();
				mPlayer.release();
				//保存播放状态
//				MyApplication.musicPreference
//						.savePlayPosition(mContext, current);
				stopSelf();
			}
			// 上一首
			else if (Constants.ACTION_PREVIOUS.equals(intent.getAction())) {
				if (playmode == 1){
					mode_current--;
				}
				previous();
				status = 3;
			}
			// 下一首
			else if (ACTION_NEXT.equals(intent.getAction())) {
				if (playmode == 1){
					mode_current++;
				}
				next();
				status = 3;
			}
			// JUMP
			else if (Constants.ACTION_JUMP.equals(intent.getAction())) {
				int position = intent.getIntExtra("position", 0);
				if (position >= 0) {
					jump(position);
				}
				// JUMP_OTHER
			} else if (Constants.ACTION_JUMP_OTHER.equals(intent.getAction())) {
				String name = intent.getStringExtra("name");
				Log.i("test", Constants.playList.size() + "--position" + "---" + name);
				int position = getdataindex(name);
				if (position >= 0) {
					jump(position);
				}
			} else if (Constants.ACTION_FIND.equals(intent.getAction())) {
				String name = intent.getStringExtra("name");
				int position = getindex(name);
				if (position >= 0) {
					jump(position);
				}
			} else if (Constants.ACTION_JUMP_MYPAGE.equals(intent.getAction())){
				String name = intent.getStringExtra("title");
				int position = getindex(name);
				if (position == -1){
					if (Constants.playList != null && Constants.playList.size() > 0){
						Constants.playPage++;
					}
					getData(0, name);
				} else if (position >= 0) {
					jump(position);
				}
			}
			// seek
			else if (Constants.ACTION_SEEK.equals(intent.getAction())) {
				try {
					nowcurr = (intent.getIntExtra("seekcurr", 0));// * totalms / 100;
					mPlayer.seekTo(nowcurr);
					if (status == 2) {
						mPlayer.start();
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (Constants.ACTION_UPDATE_ALL.equals(intent.getAction())) {
				updataAllMusicInfo(false, nowPlayAudio);

			} else // 设置播放模式
				if (Constants.ACTION_SET_PLAYMODE.equals(intent.getAction())) {
					int n = intent.getIntExtra("play_mode", -1);
					playmode = n;
					if (n == 1) {
						mode_current = current;
					}
					// 播放列表发生变化
				} else if (Constants.ACTION_LISTCHANGED.equals(intent.getAction())) {
					Constants.playList.clear();
					Constants.playList.addAll(Constants.playList);
					initAllsongNames();
				}
		}
	}

	/**
	 * 更新当前播放音频的详细信息
	 *
	 * @param isnet
	 * @param music
	 */
	private Intent updataintent;

	private void updataAllMusicInfo(boolean isnet, Audio audio) {
		if (updataintent == null) {
			updataintent = new Intent(Constants.ACTION_UPDATE);
		}
		if (isnet) {
			updataintent.putExtra("status", status);
			updataintent.putExtra("music", audio);
			updataintent.putExtra("isnet", true);
		} else {
			updataintent.putExtra("status", status);
			updataintent.putExtra("music", nowPlayAudio);
			updataintent.putExtra("position", current);
			updataintent.putExtra("totalms", totalms);
		}
		sendBroadcast(updataintent);
//		MyNotiofation.getNotif(MusicPlayerService.this, nowPlayAudio, manager);
	}

	SharedPreferences sp;

	@Override
	public void onCreate() {
		super.onCreate();
		sp = getSharedPreferences("service", 0);
		sp.edit().putBoolean("isStart", true).commit();
		// 广播接收器
		mReceiver = new MyReciever();
		apReciver = new apwReciver();
		phoneStatRec = new PhoneStatRec();
		updataintent = new Intent(Constants.ACTION_UPDATE);
		mContext = this;
		mPlayer = MyApplication.mediaPlayer;

		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			/**
			 * 音乐播放完成的处理方法
			 */
			@Override
			public void onCompletion(MediaPlayer mp) {
				next();// 播放下一首
			}
		});
		// 当前播放的音乐列表
//		musicList = (ArrayList<Audio>) Constants.playList;
		// 当前播放音乐的索引
		current = MyApplication.musicPreference.getSavePosition(this);

		mAlbumDao = new AlbumDao(context);
		mAudioDao = new AudioDao(context);

	}

	private void initAllsongNames() {
		new Thread(new Runnable() {
			@Override
			public void run() {
//				SongNamekeywords = Musicdata.GetAll(musicList);
			}
		}).start();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		initAllsongNames();
		if (mPlayer == null) {
			mPlayer = MyApplication.mediaPlayer;
		}
//		musicList = (ArrayList<Audio>) Constants.playList;
		// 动态注册广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_LISTCHANGED);
		filter.addAction(Constants.ACTION_PLAY);
		filter.addAction(Constants.ACTION_PAUSE);
		filter.addAction(Constants.ACTION_PREVIOUS);
		filter.addAction(ACTION_NEXT);
		filter.addAction(Constants.ACTION_SEEK);
		filter.addAction(Constants.ACTION_HISTORY);
		filter.addAction(Constants.ACTION_STOP);
		filter.addAction(Constants.ACTION_JUMP);
		filter.addAction(Constants.ACTION_JUMP_OTHER);
		filter.addAction(Constants.ACTION_JUMP_MYPAGE);
		filter.addAction(Constants.ACTION_UPDATE_ALL);
		filter.addAction(Constants.ACTION_FIND);
		filter.addAction(Constants.ACTION_NET_PLAY);
		filter.addAction(Constants.ACTION_SET_PLAYMODE);
		filter.addAction(Constants.ACTION_STAR_THREAD);
		registerReceiver(mReceiver, filter);
		/* 注册appwidget的广播 */
//		IntentFilter intentFilter = new IntentFilter();
//		intentFilter.addAction(AppWidget.PRIVOICE_ACTION);
//		intentFilter.addAction(AppWidget.NEXT_ACTION);
//		intentFilter.addAction(AppWidget.PLAY_ACTION);
//		intentFilter.addAction(AppWidget.START_APP);
//		registerReceiver(apReciver, intentFilter);

		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction("android.intent.action.PHONE_STATE");
		registerReceiver(phoneStatRec, mIntentFilter);
		//获取播放状态
		playmode = MyApplication.musicPreference.getPlayMode(this);

		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			public void onCompletion(MediaPlayer mp) {

//				Random random = new Random();
//				current = random.nextInt(Constants.playList.size() - 1);
//				Intent inte = new Intent("com.tarena.nextone");
//				inte.putExtra("position", current);
//				sendBroadcast(inte);

				next();
			}
		});

	}

	@Override
	public void onDestroy() {
		Log.i("Tag", "onDestroy==========");
		// 取消广播注册
		unregisterReceiver(mReceiver);
		unregisterReceiver(phoneStatRec);
        if (mPlayer.isPlaying()){
            nowPlayAudio.setCurrDurationTime(mediaPlayer.getCurrentPosition());
            mAudioDao.updateByDuration(nowPlayAudio.getId(), nowPlayAudio.getCurrDurationTime());
        }
		MyApplication.musicPreference.savePlayPosition(mContext, current);
		MyApplication.musicPreference.savePlayAlbum(mContext, Constants.playAlbum);
		super.onDestroy();
	}

	/**
	 *
	 * @param name
	 * @return 下标位置
	 */
	public int getindex(String name) {
		int index = -1;
		for (int i = 0; i < Constants.playList.size(); i++) {
			if (Constants.playList.get(i).getTitle().equals(name)) {
				index = i;
				break;
			}
		}
		return index;
	}

	//自动加载列表
	private void getData(final int what, final String name){
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpHelper httpHelper = new HttpHelper();
				httpHelper.connect();
				List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
				list.add(new BasicNameValuePair(Constants.VALUES[0], "1"));
				list.add(new BasicNameValuePair(Constants.VALUES[1], Constants.playAlbum.getId()+""));
				list.add(new BasicNameValuePair(Constants.VALUES[2], String.valueOf(Constants.playPage++)));
                if (Constants.playAlbum.getYppx() == 0){
                    list.add(new BasicNameValuePair(Constants.VALUES[6], Constants.YPPXDESC));
                } else {
                    list.add(new BasicNameValuePair(Constants.VALUES[6], Constants.YPPXASC));
                }
				HttpResponse httpResponse = httpHelper.doGet(Constants.HTTPURL, list);
				JSONObject json = Utils.parseResponse(httpResponse);
				List<Audio> l = Utils.pressAudioJson(json, Constants.playAlbum);
				if (l != null){
					Constants.playList.addAll(l);
					if (Constants.playList != null || Constants.playList.size() <= 0){
						mHandler.sendMessage(mHandler.obtainMessage(what, name));
					}
				} else {
					getData(0, name);
				}
			}
		}).start();
	}
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String name = (String) msg.obj;
			int index = getindex(name);
			if (index == -1){
				getData(0, name);
			} else {
				Intent intent = new Intent(Constants.ACTION_JUMP_MYPAGE);
				intent.putExtra("title", name);
				context.sendBroadcast(intent);
			}
		}
	};
	/**
	 *
	 * @param savepath
	 * @return 下标位置
	 */
	public int getdataindex(String savepath) {
		int index = 0;
		if (Constants.playList.size() > 0) {
			for (int i = 0; i < Constants.playList.size(); i++) {
				if (Constants.playList.get(i).getFilePath() != null
						&& Constants.playList.get(i).getFilePath().equals(savepath)) {
					index = i;
					break;
				}
			}
		}
		return index;
	}

	/**
	 * 播放当前音乐
	 */
	private void play() {

		if (Constants.playList != null && Constants.playList.size() > 0) {
			Log.i("cpu", "" + playmode + isjump);
			if (playmode == 1) {// 单曲
				current = mode_current;
			}
			nowPlayAudio = Constants.playList.get(current);
			if (mAudioDao.isExist(nowPlayAudio.getId())){
				Audio audio = mAudioDao.searchById(nowPlayAudio.getId(), true);
				nowPlayAudio.setCurrDurationTime(audio.getCurrDurationTime());
			}

			Log.i("music", current + "当前播放的歌曲");
			isjump = false;
			try {
				mPlayer.reset();
//				mPlayer.setDataSource(nowPlayAudio.getFilePath());
				if (nowPlayAudio.isCacheFinish()) {
					mPlayer.setDataSource(nowPlayAudio.getCachePath());
					mPlayer.prepare();
				} else if (nowPlayAudio.isDownFinish()) {
					mPlayer.setDataSource(nowPlayAudio.getFilePath());
					mPlayer.prepare();
				} else {
					String path = nowPlayAudio.getNetUrl();
					if (Utils.isBlank(path)){
						next();
						return;
					}
//					String name = path.substring(path.lastIndexOf("/"), path.length());
					path = Utils.pressUrl(path);
					mPlayer.setDataSource(path);
//					Uri uri = Uri.parse(path);
//					mPlayer.setDataSource(getApplicationContext(), uri);
					mPlayer.prepare();
					CacheUtil cacheUtil = new CacheUtil(nowPlayAudio, getApplicationContext());
					cacheUtil.start(false, getApplicationContext());
				}
//				mPlayer.prepare();
				mPlayer.seekTo(nowPlayAudio.getCurrDurationTime());
				mPlayer.start();
				status = 3;
				totalms = mPlayer.getDuration();
				updataAllMusicInfo(false, null);
				if (Constants.playAlbum.getYppx() == 0){
					Constants.playAlbum.setAudioIdDesc(nowPlayAudio.getId());
					Constants.playAlbum.setAudioNameDesc(nowPlayAudio.getTitle());
				} else {
					Constants.playAlbum.setAudioIdAsc(nowPlayAudio.getId());
					Constants.playAlbum.setAudioNameAsc(nowPlayAudio.getTitle());
				}
				if (mAlbumDao.isExist(Constants.playAlbum.getTitle()) == 1){
					mAlbumDao.update(Constants.playAlbum);
				} else {
					mAlbumDao.add(Constants.playAlbum);
				}
				mAudioDao.add(nowPlayAudio);
                MyApplication.musicPreference.savePlayPosition(mContext, current);
                MyApplication.musicPreference.savePlayAlbum(mContext, Constants.playAlbum);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				play();
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 播放上一首音乐
	 */
	private void previous() {
		if (Constants.playList != null && Constants.playList.size() > 0) {
			if (current == 0) {
				current = Constants.playList.size() - 1;
			} else {
				current--;
			}
			play();
		}
	}

	/**
	 * 播放下一首音乐
	 */
	private void next() {
		if (Constants.playList != null && Constants.playList.size() > 0) {
			if (current == Constants.playList.size() - 1) {
				current = 0;
			} else {
				current++;
			}
			play();
		}
	}

	/**
	 * 播放点击的某一位置歌曲
	 *
	 * @param position
	 */
	boolean isjump = false;

	private void jump(final int position) {
		Log.i("test", Constants.playList.size() + "--position" + position);
		if (mPlayer.isPlaying()){
			nowPlayAudio.setCurrDurationTime(mediaPlayer.getCurrentPosition());
			mAudioDao.updateByDuration(nowPlayAudio.getId(), nowPlayAudio.getCurrDurationTime());
		}
		new Thread(new Runnable() {
            @Override
            public void run() {
                if (Constants.playList != null && Constants.playList.size() > 0) {
                    current = position;
                    isjump = true;
                    mode_current = current;
                    play();
                }
            }
        }).start();
	}



	public class apwReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

		}
	}

}
