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
	 * Ҫ��ѯ�����ݵ�����1.���⣬2����ʱ��,3.������,4.����id��5.��ʾ����,6.���ݡ�
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
	 * ����menu�˵�
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SCAN, 0, "ɨ��SD��");
		menu.add(0,ABOUT,0,"����");
		return true;
	}
	
	/**
	 * ɨ��SD���ķ���
	 */
	private void ScanSDCard(){
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		receiver = new ScanSDCardReceiver();
		//ʹ�����ܹ�������SD���Ĳ���¼�
		intentFilter.addDataScheme("file");
		registerReceiver(receiver, intentFilter);
		//Intent.ACTION_MEDIA_MOUNTED	����SD����������ȷ��װ��ʶ��ʱ�����Ĺ㲥
		//�㲥����չ���ʱ����룬�����Ѿ������ء�   ��filter.addDataScheme("file")һ��ʹ�ü���SD���Ĳ���¼�
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
	 * �������ֵķ���
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
		startActivity(intent);   //����PlayMusicActivityҳ��
		finish(); //�رյ�ǰ��Activity
	}
	/**
	 * ��ʾ����ҳ��1.���⣬2����ʱ��,3.������,4.����id��5.��ʾ����,6.���ݡ�
	 */
	private void ShowMp3List() {
		
		Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				media_info, null, null, null);
		list = new ArrayList<Music>();
//		music.set_id(cursor.getColumnCount());
//		cursor.moveToFirst();
		while(cursor.moveToNext()){
			music = new Music();
			Log.i("Log", cursor.getInt(3)+"music����id");
			music.set_id(cursor.getInt(3));
			Log.i("Log", cursor.getString(2)+"music����artists");
			music.set_artists(cursor.getString(2));
			Log.i("Log", cursor.getString(0)+"music����titles");
			music.set_titles(cursor.getString(0));
			list.add(music);
//			Log.i("Log", music+"music");
		}
//		Log.i("Log", list+"cursor22");
//		lv.setAdapter(new MusicListAdapter(this,cursor));
		lv.setAdapter(new MusicListAdapter(this,cursor));
	}

	/**
	 * �����ؼ�ʱ��Ч��
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(receiver!=null){
				unregisterReceiver(receiver);
				AlertDialog.Builder builer = new AlertDialog.Builder(this);
				builer.setTitle("�˳������ʾ");
				builer.setIcon(R.drawable.dialog_alert_icon);
				builer.setMessage("ȷ��Ҫ�˳���");
				builer.setPositiveButton("ȷ��", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).setNegativeButton("ȡ��", null);
			}
		}
		return false;     //???ΪʲôҪ����false
	}
	
}
