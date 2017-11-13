package com.example.mymusicplayer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MusicActivity extends Activity implements OnItemClickListener{
	private static final int SCAN=1;
	private static final int ABOUT=2;
	private ScanSDCardReceiver receiver = null;
	private ListView lv;
	public static  ArrayList<Music> list = null;
	private Music music = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.music_list);
		lv = (ListView) findViewById(R.id.lv);
		ShowMp3List();
		lv.setOnItemClickListener(this);
	}
	
	/**
	 * 要查询的数据的列名1.标题，2音乐时间,3.艺术家,4.音乐id，5.显示名字,6.数据。
	 */
	String[] media_info = new String[] { MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
			MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME,
			MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID };
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==SCAN){
			ScanSDCard();
		}
		return true;
	}
	
	/**
	 * 创建menu菜单
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SCAN, 0, "扫描SD卡");
		menu.add(0,ABOUT,0,"关于");
		return true;
	}
	
	/**
	 * 扫描SD卡的方法
	 */
	private void ScanSDCard(){
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		receiver = new ScanSDCardReceiver();
		//使程序能够监听到SD卡的插拔事件
		intentFilter.addDataScheme("file");
		registerReceiver(receiver, intentFilter);
		//Intent.ACTION_MEDIA_MOUNTED	插入SD卡并且已正确安装（识别）时发出的广播
		//广播：扩展介质被插入，而且已经被挂载。   与filter.addDataScheme("file")一起使用监听SD卡的插拔事件
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
				Uri.parse("file://"+
		Environment.getExternalStorageDirectory().getAbsolutePath())));
		Log.i("Log", "file://"+
				Environment.getExternalStorageDirectory().getAbsolutePath());
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		playMusic(position);
	}
	
	/**
	 * 播放音乐的方法
	 * @param position
	 */
	public void playMusic(int position){
		Intent intent = new Intent(MusicActivity.this,PlayMusicActivity.class);
		Bundle bundle = new Bundle();
		intent.putExtra("_artists",list.get(position).get_artists());
		intent.putExtra("_titles", list.get(position).get_titles());
		intent.putExtra("position", position);
		Log.i("Log", position+"----------jj");
		intent.putExtra("_id", list.get(position).get_id());
		intent.putExtra("length", list.size());
		Log.i("Log", list.size()+"--------");
		startActivity(intent);   //启动PlayMusicActivity页面
		finish(); //关闭当前的Activity
	}
	/**
	 * 显示播放页面1.标题，2音乐时间,3.艺术家,4.音乐id，5.显示名字,6.数据。
	 */
	private void ShowMp3List() {
		
		Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				media_info, null, null, null);
		list = new ArrayList<Music>();
//		music.set_id(cursor.getColumnCount());
//		cursor.moveToFirst();
		while(cursor.moveToNext()){
			music = new Music();
			Log.i("Log", cursor.getInt(3)+"music——id");
			music.set_id(cursor.getInt(3));
			Log.i("Log", cursor.getString(2)+"music——artists");
			music.set_artists(cursor.getString(2));
			Log.i("Log", cursor.getString(0)+"music——titles");
			music.set_titles(cursor.getString(0));
			list.add(music);
//			Log.i("Log", music+"music");
		}
//		Log.i("Log", list+"cursor22");
//		lv.setAdapter(new MusicListAdapter(this,cursor));
		lv.setAdapter(new MusicListAdapter(this,cursor));
	}

	/**
	 * 按返回键时的效果
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(receiver!=null){
				unregisterReceiver(receiver);
				AlertDialog.Builder builer = new AlertDialog.Builder(this);
				builer.setTitle("退出软件提示");
				builer.setIcon(R.drawable.dialog_alert_icon);
				builer.setMessage("确定要退出吗？");
				builer.setPositiveButton("确定", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).setNegativeButton("取消", null);
			}
		}
		return false;     //???为什么要返回false
	}
	
}
