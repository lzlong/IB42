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
	private static final int PLAYING = 1;// �������ô�����ֲ����ĳ���,�粥����1
	private static final int PAUSE = 2;// ��ͣ�¼���2
	private static final int STOP = 3;// ֹͣ�¼���3
	private static final int PROGRESS_CHANGE = 4;// �������ı��¼���Ϊ4
	private static final String MUSIC_CURRENT = "com.music.currentTime";
	private static final String MUSIC_DURATION = "com.music.duration";
	private static final String MUSIC_NEXT = "com.music.next";
	private MediaPlayer mediaPlayer;// MediaPlayer����
	private Handler handler;// handler����
	private Uri uri = null;// ·����ַ
	private int id = 10000;
	private int currentTime;// ��ǰʱ��
	private int duration;// ��ʱ��

	@Override
	public void onCreate() {
		if (mediaPlayer != null) {
			mediaPlayer.reset();//����
			mediaPlayer.release();
		}
		mediaPlayer = new MediaPlayer();// ʵ����MediaPlayer����
		mediaPlayer.setOnCompletionListener(this);// ������һ�׵ļ���
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
	 * ��������ķ���
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		// ���ţ���ͣ��ǰ����һ��
		int _id = intent.getIntExtra("_id", -1);// ��ȡID������
		if (_id != -1) {
			if (id != _id) {
				id = _id;
				uri = Uri.withAppendedPath(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + _id);
				try {
					mediaPlayer.reset();// ý���������
					mediaPlayer.setDataSource(this, uri);// ����ý����Դ
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		setup();
		init();
		/**
		 * ��ʼ����/��ͣ��ֹͣ
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

	// ��������

	private void play() {
		if (mediaPlayer != null) {
			mediaPlayer.start();
		}
	}

	// ��ͣ����
	private void pause() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
		System.out.println("�����Ѿ�ֹͣ");
	}

	// ֹͣ����
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
	 * ��ʼ������
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
				handler.sendEmptyMessageDelayed(1, 600);// ���Ϳ���Ϣ����ʱ��
			}
		};
	}
	/**
	 * ׼������
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
		duration = mediaPlayer.getDuration();// ��ȡý�����ʱ��
		intent.putExtra("duration", duration);
		sendBroadcast(intent);// ��Intent������Ϣ�ù㲥���ͳ�ȥ

	}
	@Override
	public void onCompletion(MediaPlayer arg0) {
		Intent intent = new Intent();
		intent.setAction(MUSIC_NEXT);
		sendBroadcast(intent);//���ֲ�����һ��
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
