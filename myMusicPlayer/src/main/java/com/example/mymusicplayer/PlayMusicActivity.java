package com.example.mymusicplayer;

import java.util.List;
import java.util.TreeMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayMusicActivity extends Activity{
//	private int[] _ids;// 保存音乐ID临时数组
//	private String[] _artists;// 保存艺术家
//	private String[] _titles;// 标题临时数组
	private ImageButton ib_Pre = null ;
	private ImageButton ib_Play = null;
	private ImageButton ib_Next = null;
	private ImageView iv_pic = null;
	private TextView tv_song = null;
	private TextView tv_singer = null;
	private TextView tv_start = null;
	private TextView tv_end = null;
	private TextView tv_lrc = null;
	private SeekBar seekbar = null;
	private List<Music> list;
	private Music music;
	private Intent intentRe;
	private int currentPosition;
	private int duration;// 总时间
	
	private int id_count;
	private int position;// 位置
	private int flag ;// 标记
	private static final int STATE_PLAY = 1;// 播放状态设为1,表示播放状态
	private static final int STATE_PAUSE = 2;// 播放状态设为2，表示暂停状态
	private static final int PLAY = 1;// 定义播放状态
	private static final int PAUSE = 2;// 暂停状态
	private static final int STOP = 3;
	private static final int PROGRESS_CHANGE = 4;
	private static final String MUSIC_CURRENT = "com.music.currentTime";
	private static final String MUSIC_DURATION = "com.music.duration";
	private static final String MUSIC_NEXT = "com.music.next";
	private static final String MUSIC_UPDATE = "com.music.update";
	
	
	private TreeMap<Integer, LRCbean> lrc_map = new TreeMap<Integer, LRCbean>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.playmusic);
		
		
		intentRe = this.getIntent();
		List<Music> list = MusicActivity.list;
		position = intentRe.getIntExtra("position", -1);
		Log.i("Log", position+"");
		
		id_count = list.size();
		
		iv_pic = (ImageView) findViewById(R.id.iv_pic);
		tv_song = (TextView) findViewById(R.id.tv_song);
		tv_singer = (TextView) findViewById(R.id.tv_singer);
		tv_start = (TextView) findViewById(R.id.tv_start);
		tv_end = (TextView) findViewById(R.id.tv_end);
		tv_lrc = (TextView) findViewById(R.id.tv_lrc);
		
		showPlayBtn();// 显示或者说监视播放按钮事件
		showLastBtn();// 上一首
		showNextBtn();// 下一首
		showSeekBar();// 进度条
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		setup();//初始化
		play();
	}
	
	private void showPlayBtn(){
		Log.i("Log", "播放按钮");
		ib_Play = (ImageButton) findViewById(R.id.imgbtn_pause);
		ib_Play.setOnClickListener(new OnClickListener() {
			
			@Override
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
	
	protected void play(){
		flag = PLAY;
		ib_Play.setImageResource(R.drawable.pause_button);
		Intent intent = new Intent();
		intent.setAction("com.example.mymusicplayer.MusicService");
		intent.putExtra("operate", PLAY);
		startService(intent);
	}
	
	protected void pause(){
		flag = PAUSE;
		ib_Play.setImageResource(R.drawable.play_button);
		Intent intent = new Intent();
		intent.setAction("com.example.mymusicplayer.MusicService");
		intent.putExtra("operate", PAUSE);
		startService(intent);
	}
	
	private void showLastBtn(){
		Log.i("Log", "播放上一首");
		ib_Pre = (ImageButton) findViewById(R.id.imgbtn_pre);
		ib_Pre.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				latestOne();
			}
		});
	}
	
	private void latestOne(){
		Log.i("Log", position+"");
		if (position == 0) {
			position = id_count - 1;
		} else if (position > 0) {
			position--;
		}
		stop();
		setup();
		play();
	}
	
	private void showNextBtn(){
		Log.i("Log", "播放下一首");
		ib_Next = (ImageButton) findViewById(R.id.imgbtn_next);
		ib_Next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				nextOne();
			}
		});
	}
	
	protected void nextOne(){
		if(position==id_count-1){
			position = 0;
		}else if(position<id_count-1){
			position++;
		}
		stop();
		setup();
		play();
	}
	
	protected void stop(){
		Intent intent = new Intent();
		intent.setAction("com.example.mymusicplayer.MusicService");
		intent.putExtra("operate", STOP);
		startService(intent);
	}
	//准备
	private void setup(){
		Log.i("Log", "setup初始化准备工作");
		loadclip();
		init();
		ReadSDLrc();
	}
	//截取歌词，歌名等信息
	private void loadclip(){
		seekbar.setProgress(0);
//		int pos = position-1;
//		tv_song.setText(list.get(pos).get_titles());
//		tv_singer.setText(list.get(pos).get_artists());
		int pos = intentRe.getIntExtra("_id", 0);
		Log.i("Log", intentRe.getStringExtra("_titles"));
		tv_song.setText(intentRe.getStringExtra("_titles"));
		tv_singer.setText(intentRe.getStringExtra("_artists"));
		Intent intent = new Intent();
		intent.putExtra("_id", pos);
		intent.putExtra("_titles", intentRe.getStringExtra("_titles"));
		intent.putExtra("position", intentRe.getStringExtra("position"));
		intent.setAction("com.example.mymusicplayer.MusicService");
		startService(intent);
	}
	//初始化广播
	private void init(){
		IntentFilter intentfilter = new IntentFilter();
		intentfilter.addAction(MUSIC_CURRENT);
		intentfilter.addAction(MUSIC_DURATION);
		intentfilter.addAction(MUSIC_NEXT);
		intentfilter.addAction(MUSIC_UPDATE);
		registerReceiver(receiver, intentfilter);
	}
	private BroadcastReceiver receiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(MUSIC_CURRENT)){
				currentPosition = intent.getExtras().getInt("currentTime");
				tv_start.setText(toTime(currentPosition));
				seekbar.setProgress(currentPosition);
				
			}else if(action.equals(MUSIC_DURATION)){
				duration = intent.getExtras().getInt("duration");
				seekbar.setMax(duration);
				tv_end.setText(toTime(duration));
			}else if (action.equals(MUSIC_NEXT)) {
				Log.i("Log", "音乐继续播放下一首");
				nextOne();
			} else if (action.equals(MUSIC_UPDATE)) {
				position = intent.getExtras().getInt("position");
				setup();
			}
			
		}
		
	};
	
	/**
	 * 时间的转换
	 * 
	 * @param time
	 * @return
	 */
	public String toTime(int time) {

		time /= 1000;
		int minute = time / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}
	private void ReadSDLrc(){
	
}
	
	private void showSeekBar(){
		seekbar = (SeekBar) findViewById(R.id.seekBar);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int po = seekbar.getProgress();
				Log.i("Log", po+"");
				play();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				pause();
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					seekbar_change(progress);
				}
			}
		});
	}
	
	@Override
		protected void onStop() {
			super.onStop();
			unregisterReceiver(receiver);
		}
	protected void seekbar_change(int progress){
		flag = PROGRESS_CHANGE;
		Intent intent = new Intent();
		intent.setAction("com.example.mymusicplayer.MusicService");
		intent.putExtra("progress", progress);
		intent.putExtra("operate", PROGRESS_CHANGE);
		startService(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			Intent intent = new Intent(this,MainActivity.class);
			startActivity(intent);
			finish();
		}
		return true;
	}
	
}
