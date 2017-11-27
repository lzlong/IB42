package com.xm.ib42.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.xm.ib42.entity.Album;

public class MusicPreference {

	SharedPreferences sharedPreferences;

	public MusicPreference(Context context) {
		sharedPreferences = context.getSharedPreferences("music_preference",
				Context.MODE_PRIVATE);
	}

	/**
	 * 保存歌曲退出时的 播放位置
	 * 
	 * @param context
	 * @param position
	 */
	public void savaPlayPosition(Context context, int position) {
		sharedPreferences.edit().putInt("position", position).commit();
	}

	/**
	 * 获取退出时的播放位置
	 * 
	 * @param context
	 * @return
	 */
	public int getsaveposition(Context context) {
		return sharedPreferences.getInt("position", 0);
	}

	/**
	 * 保存 播放模式
	 * 
	 * @param context
	 * @param playmode
	 *            0 顺序播放 1 随机播放 2 单曲循环
	 */
	public void savaPlayMode(Context context, int playmode) {
		sharedPreferences.edit().putInt("playmode", playmode).commit();
	}

	/**
	 * 获取播放模式
	 * 
	 * @param context
	 * @return int playmode 0 顺序播放 1 随机播放 2 单曲循环
	 */
	public int getPlayMode(Context context) {
		return sharedPreferences.getInt("playmode", 0);
	}

	public void savePlayAlbum(Context context, Album album){
		sharedPreferences.edit().putInt("albumId", album.getId());
	}

}
