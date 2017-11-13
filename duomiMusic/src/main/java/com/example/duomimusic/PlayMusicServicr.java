package com.example.duomimusic;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;


public class PlayMusicServicr extends Service implements OnCompletionListener{
	private static final int PLAYING = 1;// 定义该怎么对音乐操作的常量,如播放是1
	private static final int PAUSE = 2;// 暂停事件是2
	private static final int STOP = 3;// 停止事件是3
	private static final int PROGRESS_CHANGE = 4;// 进度条改变事件设为4
	private static final String MUSIC_CURRENT = "com.music.currentTime";
	private static final String MUSIC_DURATION = "com.music.duration";
	private static final String MUSIC_NEXT = "com.music.next";
	private MediaPlayer mediaPlayer;// MediaPlayer对象
	private Handler handler;// handler对象
	private Uri uri = null;// 路径地址
	private int id = 10000;
	private int currentTime;// 当前时间
	private int duration;// 总时间

	@Override
	public void onCreate() {
		if (mediaPlayer != null) {
			mediaPlayer.reset();//重置
			mediaPlayer.release();
		}
		mediaPlayer = new MediaPlayer();// 实例化MediaPlayer对象
		mediaPlayer.setOnCompletionListener(this);// 设置下一首的监听
	}

	@Override
	public void onDestroy() {
		if (mediaPlayer != null) {
			stop();
		}
		if (handler != null) {
			handler.removeMessages(1);
			handler = null;
		}
	}

	/**
	 * 开启服务的方法
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		// 播放，暂停，前，后一首
		int _id = intent.getIntExtra("_id", -1);// 获取ID的数据
		if (_id != -1) {
			if (id != _id) {
				id = _id;
				uri = Uri.withAppendedPath(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + _id);
				try {
					mediaPlayer.reset();// 媒体对象重置
					mediaPlayer.setDataSource(this, uri);// 设置媒体资源
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		setup();
		init();
		/**
		 * 开始播放/暂停、停止
		 */
		int op = intent.getIntExtra("op", -1);
		if (op != -1) {
			switch (op) {
			case PLAYING:
				play();
				break;
			case PAUSE:
				pause();
				break;
			case STOP:
				stop();
				break;
			case PROGRESS_CHANGE:
				int progress = intent.getExtras().getInt("progress");
				mediaPlayer.seekTo(progress);
				break;
			}
		}

	}

	// 播放音乐

	private void play() {
		if (mediaPlayer != null) {
			mediaPlayer.start();
		}
	}

	// 暂停音乐
	private void pause() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
		System.out.println("音乐已经停止");
	}

	// 停止音乐
	private void stop() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			try {
				mediaPlayer.prepare();
				mediaPlayer.seekTo(0);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			handler.removeMessages(1);
		}
	}

	/**
	 * 初始化服务
	 */
	private void init() {
		final Intent intent = new Intent();
		intent.setAction(MUSIC_CURRENT);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					currentTime = mediaPlayer.getCurrentPosition();
					intent.putExtra("currentTime", currentTime);
					sendBroadcast(intent);
				}
				handler.sendEmptyMessageDelayed(1, 600);// 发送空消息持续时间
			}
		};
	}
	/**
	 * 准备工作
	 */
	private void setup() {
		final Intent intent = new Intent();
		intent.setAction(MUSIC_DURATION);
		try {
			if (!mediaPlayer.isPlaying()) {
				mediaPlayer.prepare();
				mediaPlayer.start();
			}
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				public void onPrepared(MediaPlayer mp) {
					handler.sendEmptyMessage(1);

				}
			});
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		duration = mediaPlayer.getDuration();// 获取媒体的总时间
		intent.putExtra("duration", duration);
		sendBroadcast(intent);// 将Intent对象信息用广播发送出去

	}
	@Override
	public void onCompletion(MediaPlayer arg0) {
		Intent intent = new Intent();
		intent.setAction(MUSIC_NEXT);
		sendBroadcast(intent);//音乐播放下一首
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
