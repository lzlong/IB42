package com.example.duomimusic;

import java.util.List;
import activity.SideslipActivity;
import android.R.integer;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PlayMusicActivity extends Activity {

	ImageButton btn_play_back;// 返回主界面
	ImageButton btn_lastOne;// 上一首
	ImageButton btn_nextOne;// 下一首
	ImageButton btn_play_pause;// 播放暂停
	TextView playMusicName,playMusicSinger;
	TextView leftTime,rightTime;
	private int playtime, duration;// 播放时间，总时间
	private TextView name = null;// 歌名
	private TextView artist = null;// 歌手
	private TextView lrcText = null;// 歌词
	private static final String MUSIC_CURRENT = "com.music.currentTime";
	private static final String MUSIC_DURATION = "com.music.duration";
	private static final String MUSIC_NEXT = "com.music.next";
	private static final String MUSIC_UPDATE = "com.music.update";
	private static final int PLAY = 1;// 定义播放状态
	private static final int PAUSE = 2;// 暂停状态
	private static final int STOP = 3;// 停止
	private static final int PROGRESS_CHANGE = 4;// 进度条改变
	private static final int STATE_PLAY = 1;// 播放状态设为1,表示播放状态
	private static final int STATE_PAUSE = 2;// 播放状态设为2，表示暂停状态
	private int id_count;
	private int position;// 位置
	private int flag;// 标记
	private SeekBar seekbar = null;
	private List<Music> list;
	private Music music;
	private Intent intent;
	private int currentPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_music_lrc);

		intent = this.getIntent();// 获取列表的Intent对象
		position = intent.getIntExtra("position", -1);// Bundle存取数据，那么在播放界面提取数据
		id_count = LocalMusicActivity.list.size();
		
		playMusicName=(TextView) findViewById(R.id.tv_music_name);
		playMusicSinger=(TextView) findViewById(R.id.tv_music_sing);

		btn_play_back=(ImageButton) findViewById(R.id.iv_lrc_bck);
		btn_play_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(PlayMusicActivity.this,SideslipActivity.class));
			}
		});// 返回主页面
		leftTime=(TextView) findViewById(R.id.tv_lrc_lefttime);
		rightTime=(TextView) findViewById(R.id.tv_lrc_righttime);
		findViewById(R.id.tv_lrc_righttime);
		

		ShowPlayBtn();// 显示或者说监视播放按钮事件
		ShowLastBtn();// 上一首
		ShowNextBtn();// 下一首
		ShowSeekBar();// 进度条
	}

	// 显示各个按钮并做监视
	private void ShowPlayBtn() {
		btn_play_pause = (ImageButton) findViewById(R.id.play_pause);
		btn_play_pause.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				switch (flag) {
				case STATE_PLAY:
					pause();
					break;
				case STATE_PAUSE:
					play();
					break;
				}
			}
		});
	}

	private void ShowLastBtn() {
		btn_lastOne = (ImageButton) findViewById(R.id.play_last_one);
		btn_lastOne.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				latestOne();
			}
		});
	}

	private void ShowNextBtn() {
		btn_nextOne = (ImageButton) findViewById(R.id.btn_next_one);
		btn_nextOne.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				nextOne();
			}
		});
	}

	private void ShowSeekBar() {
		seekbar = (SeekBar) findViewById(R.id.seekbar);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {// 结束拖动
				play();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {// 开始
				pause();
			}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {// 数值改变
				if (fromUser) {
					seekbar_change(progress);
				}
			}
		});
	}

	
	protected void onStart() {
		super.onStart();
		setup();
		play();
	}

	private void onstop() {
		// TODO Auto-generated method stub
		super.onStop();
		unregisterReceiver(musicRecriver);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			Intent intent=new Intent(this, LocalMusicActivity.class);
			startActivity(intent);
			finish();
		}
		return true;
	}

	private void play() {
		// TODO Auto-generated method stub
		flag=PLAY;
		btn_play_pause.setImageResource(R.drawable.player_pause);
		Intent intent=new Intent();
		intent.setAction("com.example.duomimusic.PlayMusicServicr");
		intent.putExtra("op", PLAY);
		Log.d("Tag", "PlayMusicActivity-->play");
		startService(intent); 
	}
	private void pause() {
		// TODO Auto-generated method stub
		flag=PAUSE;
		Log.i("Log", flag+"     flag");
		btn_play_pause.setImageResource(R.drawable.player_pause);
		Intent intent=new Intent();
		intent.setAction("com.example.duomimusic.PlayMusicServicr");
		intent.putExtra("op", PAUSE);
		startService(intent);
	}
	private void setup() {
		loadclip(position);
		init();
	}

	private void stop() {
		// TODO Auto-generated method stub
		Intent intent=new Intent();
		intent.setAction("com.example.duomimusic.PlayMusicServicr");
		intent.putExtra("op", STOP);
		startService(intent);
	}


	// 截取标题，歌词，歌名
		private void loadclip(int pos) {
			seekbar.setProgress(0);
			playMusicName.setText(LocalMusicActivity.list.get(pos).getTitles());
			playMusicSinger.setText(LocalMusicActivity.list.get(pos).getArtists());

			Intent intent=new Intent();
			intent.putExtra("id", LocalMusicActivity.list.get(pos).getId());
			intent.putExtra("titles", LocalMusicActivity.list.get(pos).getTitles());
			intent.putExtra("position", pos);
			intent.setAction("com.example.duomimusic.PlayMusicServicr");
			startService(intent);

		}

	private void seekbar_change(int progress) {
		// TODO Auto-generated method stub
		Intent intent=new Intent();
		intent.setAction("com.example.duomimusic.PlayMusicServicr");
		intent.putExtra("op", PROGRESS_CHANGE);
		intent.putExtra("progress", progress);
		startService(intent);
	}

	private void latestOne() {
		// TODO Auto-generated method stub
		if(position==0){
			position=id_count-1;
		}else if(position>0){
			position--;
		}
		stop();
		setup();
		play();
	}

	private void nextOne() {
		// TODO Auto-generated method stub
		Log.i("Log", position+"position");
		if(position==id_count-1){
			position=0;
		}else if(position<id_count-1){
			position++;
		}
		stop();
		setup();
		play();
	}
	private void init() {
		// TODO Auto-generated method stub
		IntentFilter filter=new IntentFilter();
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(MUSIC_DURATION);
		filter.addAction(MUSIC_NEXT);
		filter.addAction(MUSIC_UPDATE);
		registerReceiver(musicRecriver, filter);
	}
	private BroadcastReceiver musicRecriver=new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if(action.equals(MUSIC_CURRENT)){
				currentPosition=intent.getExtras().getInt("currentTime");
				leftTime.setText(toTime(currentPosition));
				seekbar.setProgress(currentPosition);
			}else if(action.equals(MUSIC_DURATION)){
				duration=intent.getExtras().getInt("duration");
				seekbar.setMax(duration);
				rightTime.setText(toTime(duration));
			}else if(action.equals(MUSIC_NEXT)){
				nextOne();
			}else if(action.equals(MUSIC_UPDATE)){
				position=intent.getExtras().getInt("position");
				setup();
			}
		}
		
	};
	private String toTime(int time) {
		// TODO Auto-generated method stub
		time /= 1000;
		int minute = time / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}

}
