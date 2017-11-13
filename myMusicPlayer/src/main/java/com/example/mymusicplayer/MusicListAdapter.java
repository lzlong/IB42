package com.example.mymusicplayer;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MusicListAdapter extends BaseAdapter{
	private Context con;
	private List<Music> list;
	private Cursor cursor;
	public MusicListAdapter(Context con,Cursor cursor){
		this.con = con;
		this.cursor = cursor;
	}
 
	public MusicListAdapter(Context con,List<Music> list){
		this.con = con;
		this.list = list;
	}
	@Override
	public int getCount() {
		return cursor.getCount();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		cursor.moveToPosition(position);
//		Music music = list.get(position);
		if(convertView==null){
			convertView = View.inflate(con, R.layout.musiclist_item, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
//		holder.iv_item.setImageResource();
//		Log.i("Tag", music.get_titles()+"iv_item111");
//		holder.iv_item.setImageResource(R.drawable.music);
//		holder.tv_musicName.setText(music.get_titles());
//		holder.tv_singer.setText(music.get_artists());
//		holder.tv_time.setText(toTime(music.getPosition()));
		holder.iv_item.setImageResource(R.drawable.music);
		holder.tv_musicName.setText(cursor.getString(0));
		holder.tv_singer.setText(cursor.getString(2));
		//设置音乐时常
		holder.tv_time.setText(toTime(cursor.getInt(1)));
		return convertView;
	}
	
	static class ViewHolder{
		ImageView iv_item;
		TextView tv_musicName,tv_singer,tv_time;
		public ViewHolder(View convertView){
			iv_item = (ImageView) convertView.findViewById(R.id.listitem);
			tv_musicName = (TextView) convertView.findViewById(R.id.musicname);
			tv_singer = (TextView) convertView.findViewById(R.id.singer);
			tv_time = (TextView) convertView.findViewById(R.id.time);
		}
	}

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
}
