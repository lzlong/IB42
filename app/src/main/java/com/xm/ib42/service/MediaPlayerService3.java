package com.xm.ib42.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.xm.ib42.app.MyApplication;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.entity.Audio;

import java.util.ArrayList;
import java.util.Random;


public class MediaPlayerService3 extends Service {

	private MyReciever mReceiver;
	private apwReciver apReciver;
	private PhoneStatRec phoneStatRec;
	public static MediaPlayer mPlayer;

	private ArrayList<Audio> musicList;
	private int current;// 当前播放的歌曲下标
	private int nowcurr = 0;// 当前播放进度
	private int totalms = 0;// 当前歌曲总时长
	private int playmode = 0;// 播放模式 0 顺序播放 1 随机播放 2 单曲循环
	public static int status = 1;// 1 未播放 2 暂停 3 播放
	public Context mContext;

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
						sendBroadcast(new Intent("com.tarena.ispause"));
						status = 2;
						break;
				}
				status = 3;
				sendBroadcast(new Intent("com.tarena.isplay"));
			}
			// 暂停
			else if (Constants.ACTION_PAUSE.equals(intent.getAction())) {
				mPlayer.pause();
				status = 2;
				sendBroadcast(new Intent("com.tarena.ispause"));
			}
			// 停止
			else if (Constants.ACTION_STOP.equals(intent.getAction())) {
				mPlayer.stop();
				mPlayer.release();
				//保存播放状态
//				MyApplication.musicPreference
//						.savaPlayPosition(mContext, current);
				stopSelf();
			}
			// 上一首
			else if (Constants.ACTION_PREVIOUS.equals(intent.getAction())) {
				previous();
				status = 3;
			}
			// 下一首
			else if (Constants.ACTION_NEXT.equals(intent.getAction())) {
				next();
				status = 3;
			}
			// JUMP
			else if (Constants.ACTION_JUMR.equals(intent.getAction())) {
				int position = intent.getIntExtra("position", 0);
				if (position >= 0) {
					jump(position);
				}
				// JUMP_OTHER
			} else if (Constants.ACTION_JUMR_OTHER.equals(intent.getAction())) {
				String name = intent.getStringExtra("name");
				Log.i("test", musicList.size() + "--position" + "---" + name);
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
			}
			// seek
			else if (Constants.ACTION_SEEK.equals(intent.getAction())) {
				try {
					nowcurr = (intent.getIntExtra("seekcurr", 0)) * totalms
							/ 100;
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
				updataAllMusicInfo(false, nowplaymusic);

			} else // 设置播放模式
				if (Constants.ACTION_SET_PLAYMODE.equals(intent.getAction())) {
					int n = intent.getIntExtra("play_mode", -1);
					playmode = n;
					if (n == 2) {
						mode_current = current;
					}
					// 播放列表发生变化
				} else if (Constants.ACTION_LISTCHANGED.equals(intent.getAction())) {
					musicList.clear();
					musicList.addAll(MyApplication.musics);
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
			updataintent.putExtra("music", nowplaymusic);
			updataintent.putExtra("position", current);
			updataintent.putExtra("totalms", totalms);
		}
		sendBroadcast(updataintent);
//		MyNotiofation.getNotif(MusicPlayerService.this, nowplaymusic, manager);
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
		musicList = ((MyApplication) getApplication()).getMusics();
		// 当前播放音乐的索引
		current = MyApplication.musicPreference.getsaveposition(this);


	}

	private void initAllsongNames() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				SongNamekeywords = Musicdata.GetAll(musicList);
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
		musicList = ((MyApplication) getApplication()).getMusics();
		// 动态注册广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_LISTCHANGED);
		filter.addAction(Constants.ACTION_PLAY);
		filter.addAction(Constants.ACTION_PAUSE);
		filter.addAction(Constants.ACTION_PREVIOUS);
		filter.addAction(Constants.ACTION_NEXT);
		filter.addAction(Constants.ACTION_SEEK);
		filter.addAction(Constants.ACTION_STOP);
		filter.addAction(Constants.ACTION_JUMR);
		filter.addAction(Constants.ACTION_JUMR_OTHER);
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
//		playmode = MyApplication.musicPreference.getPlayMode(this);

		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			public void onCompletion(MediaPlayer mp) {

				Random random = new Random();
				current = random.nextInt(musicList.size() - 1);
				Intent inte = new Intent("com.tarena.nextone");
				inte.putExtra("position", current);
				sendBroadcast(inte);
				next();
			}
		});

	}

	@Override
	public void onDestroy() {
		Log.i("info", sp.getBoolean("isStart", false) + "");
		// 取消广播注册
		unregisterReceiver(mReceiver);
		unregisterReceiver(phoneStatRec);
		MyApplication.musicPreference.savaPlayPosition(mContext, current);
		super.onDestroy();
	}

	/**
	 *
	 * @param name
	 * @return 下标位置
	 */
	public int getindex(String name) {
		int index = 0;
		for (int i = 0; i < musicList.size(); i++) {
			if (musicList.get(i).getTitle().equals(name)) {
				index = i;
				break;
			}
		}
		return index;
	}

	/**
	 *
	 * @param savepath
	 * @return 下标位置
	 */
	public int getdataindex(String savepath) {
		int index = 0;
		if (musicList.size() > 0) {
			for (int i = 0; i < musicList.size(); i++) {
				if (musicList.get(i).getFilePath() != null
						&& musicList.get(i).getFilePath().equals(savepath)) {
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
		if (musicList != null && musicList.size() > 0) {
			Log.i("cpu", "" + playmode + isjump);
			if (playmode == 1) {// 随机
				if (!isjump) {
					current = new Random().nextInt(musicList.size());
				}
			} else if (playmode == 2) {// 单曲
				current = mode_current;
			}
			nowplaymusic = musicList.get(current);
			Log.i("music", current + "当前播放的歌曲");
			isjump = false;
			try {
				mPlayer.reset();
				mPlayer.setDataSource(nowplaymusic.getSavePath());
				mPlayer.prepare();
				mPlayer.start();
				status = 3;
				totalms = mPlayer.getDuration();
				updataAllMusicInfo(false, null);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
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
		if (musicList != null && musicList.size() > 0) {
			if (current == 0) {
				current = musicList.size() - 1;
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
		if (musicList != null && musicList.size() > 0) {
			if (current == musicList.size() - 1) {
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

	private void jump(int position) {
		Log.i("test", musicList.size() + "--position" + position);
		if (musicList != null && musicList.size() > 0) {
			current = position;
			isjump = true;
			mode_current = current;
			play();
		}
	}



	public class apwReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

		}
	}

}
