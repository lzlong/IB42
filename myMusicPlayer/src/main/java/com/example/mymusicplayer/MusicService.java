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
	private static final int PLAYING = 1;// �������ô�����ֲ����ĳ���,�粥����1
	private static final int PAUSE = 2;// ��ͣ�¼���2
	private static final int STOP = 3;// ֹͣ�¼���3
	private static final int PROGRESS_CHANGE = 4;// �������ı��¼���Ϊ4
	private static final String MUSIC_CURRENT = "com.music.currentTime";
	private static final String MUSIC_DURATION = "com.music.duration";
	private static final String MUSIC_NEXT = "com.music.next";
	private MediaPlayer mp;// MediaPlayer����
	private Handler handler;// handler����
	private Uri uri = null;// ·����ַ
	private int id = 10000;
	private int currentTime;// ��ǰʱ��
	private int duration;// ��ʱ��

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
		if (_id != -1) { // ��ǰ��������ʱ�������б��в�Ϊ��
			if (id != _id) { // �б��е�����û�г�������
				id = _id;
				uri = Uri.withAppendedPath(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ""+_id);
				mp.reset(); // ý���������
				try { // ����ý����Դ
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
				mp.seekTo(progress); // ��¼�ϴεĲ���ʱ��
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
			Log.i("Log", "�����Ѿ���ͣ");
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
			 * 1���������ʹ�õ�ǰ����֮ǰ���ù�sendEmptyMessageDelayed(0, time)��
			 * ��˼���ӳ�timeִ��handler��msg.what=0�ķ�����
				2�����ӳ�ʱ��δ����ǰ���£�ִ��removeMessages(0)��
				�������handler��msg.what=0�ķ���ȡ��ִ�У�
				3�����ӳ�ʱ���ѵ���handler��msg.what=0�ķ�����ִ�У�
				��ִ��removeMessages(0)���������á�
			 */
			handler.removeMessages(1);
		}
	}

	/**
	 * ׼������, �����Ŷ��󴴽�һ��׼����ɶ��󣬵�׼����ɺ�����̷߳���һ����Ϣ��
	 * �����Ҫ���ŵ���Ƶ��ʱ������ʱ���ӵ�intent�У�Ȼ���͹㲥�����߽�������Ƕ೤
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
	 * ��ʼ������, ͨ����ͣ���ж���Ϣ�Ƿ�Ϊ1����ȷ��Ҫ��Ҫ���㲥��֪ͨϵͳ����seekbar ����ͨ���ӳٷ�����Ϣ��ʱseekbar�ܹ���ͣ�ظ���״̬
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
		Log.i("Log", "Service------������һ�׸���");
	}

}
