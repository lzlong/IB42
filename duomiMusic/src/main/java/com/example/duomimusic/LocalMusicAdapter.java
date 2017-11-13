package com.example.duomimusic;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LocalMusicAdapter extends BaseAdapter {
	private Context context;
	private List<Music> list;
	private Cursor cursor;

	public LocalMusicAdapter(Context context, Cursor cursor) {
		this.context = context;
		this.cursor=cursor;
		//list = list;
	}
	public LocalMusicAdapter(Context context, List<Music>list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return cursor.getCount();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		cursor.moveToPosition(position);
		if(convertView==null){
			convertView=View.inflate(context, R.layout.localmusic_item, null);
			holder=new ViewHolder();
			holder.musicName=(TextView)convertView.findViewById(R.id.music_name);
			holder.musicArtist=(TextView)convertView.findViewById(R.id.music_artist);
			convertView.setTag(holder);
		}else{
			holder=(ViewHolder)convertView.getTag();
		}
		holder.musicName.setText(cursor.getString(0));
		holder.musicArtist.setText(cursor.getString(2));
		return convertView;
	}
}
class ViewHolder {
	TextView musicName,musicArtist;//∏Ë«˙√˚£¨∏Ë ÷√˚
	
}

