package com.example.duomimusic;

import java.util.ArrayList;
import java.util.List;

import activity.SideslipActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class LocalMusicActivity extends Activity implements OnItemClickListener{
	ListView localMusicListView;// listview对象
	public static ArrayList<Music> list = null;
	private Music music = null;
	private ScanCareRecriver receiver = null;
	private AlertDialog.Builder builder = null;
	private AlertDialog dialog = null;
	private Button bt_temp;
	private Intent exit;
	LinearLayout localMusicImage;
	ImageButton local_scan;
	/** local_music_back:local中的返回按钮 */
	ImageButton local_music_back;
	/**
	 * TITLE:标题 DURATION：时间 ARTIST：艺术家 _ID：地址 DISPLAY_NAME：显示名字 DATA：数据
	 * ALBUM_ID：唱片集地址
	 */
	String[] MPMessage = new String[] { MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
			MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME,
			MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local);
		
		findViewById(R.id.img_back_btn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(LocalMusicActivity.this,SideslipActivity.class);
				startActivity(intent);
			}
		});
		
		localMusicListView = (ListView) findViewById(R.id.lv_musicname_list);
		localMusicListView.addHeaderView(View.inflate(this, R.layout.headview,null));
		localMusicImage = (LinearLayout) findViewById(R.id.music_null);
		local_scan = (ImageButton) findViewById(R.id.btn_local_scan);
		local_scan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 点击扫面按钮时：
				ScanSDCard();
			}
		});
	}

	// listView的监听方法
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		playMusic(position);
	}
	// 扫描SD卡
	private void ScanSDCard() {
		IntentFilter filter = new IntentFilter(
				Intent.ACTION_MEDIA_SCANNER_STARTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		receiver = new ScanCareRecriver();
		filter.addDataScheme("file");
		registerReceiver(receiver, filter);// 把自定义的Receiver和编写好的IntentFilter注册到系统中
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
				Uri.parse("file://"
						+ Environment.getExternalStorageDirectory()
								.getAbsolutePath())));
	}

	/*
	 * private void ShowMp3List() {
	 * 
	 * Cursor cursor =
	 * getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
	 * media_info, null, null, null); list = new ArrayList<Music>();
	 * 
	 * while(cursor.moveToNext()){ music = new Music();
	 * music.set_id(cursor.getInt(3)); music.set_artists(cursor.getString(2));
	 * music.set_titles(cursor.getString(0)); list.add(music); }
	 * lv.setAdapter(new MusicListAdapter(this,cursor)); }
	 */

	private void ShowMp3List() {
		Cursor cursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MPMessage, null,
				null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		list = new ArrayList<Music>();
		while (cursor.moveToNext()) {
			music = new Music();
			music.setId(cursor.getInt(3));
			music.setArtists(cursor.getString(2));
			music.setTitles(cursor.getString(0));
			list.add(music);
		}
		localMusicListView.setAdapter(new LocalMusicAdapter(this, cursor));// 用setAdapter装载数据

			if (cursor.getCount() < 0) {
				localMusicListView.setVisibility(View.GONE);
				} else {
				localMusicImage.setVisibility(View.GONE);
				}
		}

	/** 播放音乐的方法 */
	public void playMusic(int position) {
		Log.i("log", "play________________music");
		Intent intent = new Intent(LocalMusicActivity.this,
				PlayMusicActivity.class);
		Bundle bundle = new Bundle();
		intent.putExtra("artists",list.get(position).getArtists());
		intent.putExtra("titles",list.get(position).getTitles());
		intent.putExtra("id",list.get(position).getId());
		intent.putExtra("position",list.get(position).getPosition());
		intent.putExtra("length",list.size());
		startActivity(intent);
		finish();
	}


	public class ScanCareRecriver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
				builder = new AlertDialog.Builder(context);
				builder.setMessage("请稍后，正在扫描SDCard...");
				dialog = builder.create();
				dialog.show();
			} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {

				dialog.cancel();
				ShowMp3List();
			}
		}
	}
}
