package com.example.mymusicplayer;

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
import android.util.Log;
import android.widget.SeekBar;

public class MusicService extends Service implements OnCompletionListener {
	private static final int PLAYING = 1;// 定义该怎么对音乐操作的常量,如播放是1
	private static final int PAUSE = 2;// 暂停事件是2
	private static final int STOP = 3;// 停止事件是3
	private static final int PROGRESS_CHANGE = 4;// 进度条改变事件设为4
	private static final String MUSIC_CURRENT = "com.music.currentTime";
	private static final String MUSIC_DURATION = "com.music.duration";
	private static final String MUSIC_NEXT = "com.music.next";
	private MediaPlayer mp;// MediaPlayer对象
	private Handler handler;// handler对象
	private Uri uri = null;// 路径地址
	private int id = 10000;
	private int currentTime;// 当前时间
	private int duration;// 总时间

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		if (mp != null) {
			mp.reset();
			mp.release();
		}
		mp = new MediaPlayer();
		mp.setOnCompletionListener(this);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		int _id = intent.getIntExtra("_id", -1);
		if (_id != -1) { // 当前服务启动时，播放列表中不为空
			if (id != _id) { // 列表中的数据没有超出限制
				id = _id;
				uri = Uri.withAppendedPath(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ""+_id);
				mp.reset(); // 媒体对象重置
				try { // 重置媒体资源
					mp.setDataSource(MusicService.this, uri);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		setup();
		init();
		int op = intent.getIntExtra("operate", -1);
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
				int progress = intent.getIntExtra("progress", -1);
				mp.seekTo(progress); // 记录上次的播放时间
				break;
			}
		}
	}

	private void play() {
		if (mp != null) {
			mp.start();
		}
	}

	private void pause() {
		if (mp != null) {
			mp.stop();
			Log.i("Log", "音乐已经暂停");
		}
	}

	private void stop() {
		if (mp != null) {
			mp.stop();
			try {
				mp.prepare();
				mp.seekTo(0);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			/**
			 * 1、这个方法使用的前提是之前调用过sendEmptyMessageDelayed(0, time)，
			 * 意思是延迟time执行handler中msg.what=0的方法；
				2、在延迟时间未到的前提下，执行removeMessages(0)，
				则上面的handler中msg.what=0的方法取消执行；
				3、在延迟时间已到，handler中msg.what=0的方法已执行，
				再执行removeMessages(0)，不起作用。
			 */
			handler.removeMessages(1);
		}
	}

	/**
	 * 准备工作, 给播放对象创建一个准备完成对象，当准备完成后给主线程发送一个消息。
	 * 获得所要播放的视频的时长，将时长加到intent中，然后发送广播，告诉进度条最长是多长
	 */
	private void setup() {
		Intent intent = new Intent();
		intent.setAction(MUSIC_DURATION);
		try {
			if (!(mp.isPlaying())) {
				mp.prepare();
				mp.start();
				mp.setOnPreparedListener(new OnPreparedListener() {
					
					@Override
					public void onPrepared(MediaPlayer mp) {
						handler.sendEmptyMessage(1);
					}
				});
			}
			
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("===============");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

		duration = mp.getDuration();
		intent.putExtra("duration", duration);

		sendBroadcast(intent);
	}

	/**
	 * 初始化服务, 通过不停地判断消息是否为1，来确定要不要发广播来通知系统更新seekbar 并且通过延迟发送消息，时seekbar能够不停地更新状态
	 */
	private void init() {
		final Intent intent = new Intent();
		intent.setAction(MUSIC_CURRENT);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == 1) {
					currentTime = mp.getCurrentPosition();
					intent.putExtra("currentTime", currentTime);
					sendBroadcast(intent);
				}
				handler.sendEmptyMessageDelayed(1, 600);
			}
		};
	}

	@Override
	public void onDestroy() {
		if(mp!=null){
			mp.stop();
			
		}
		if(handler!=null){
			handler.removeMessages(1);
			handler = null;
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Intent intent = new Intent();
		intent.setAction(MUSIC_NEXT);
		sendBroadcast(intent);
		Log.i("Log", "Service------播放下一首歌曲");
	}

}
