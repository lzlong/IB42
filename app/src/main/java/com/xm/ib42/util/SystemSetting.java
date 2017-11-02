package com.xm.ib42.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SystemSetting {
	/**
	 * 系统设置的保存的文件名
	 * */
	public static final String PREFERENCE_NAME = "com.xm.ib42.system";
	/**
	 * SD卡下载歌曲目录
	 * */
	public static final String DOWNLOAD_MUSIC_DIRECTORY="/IB42/download_music/";
	public static final String CACHE_MUSIC_DIRECTORY="/IB42/cache_music/";
	public static final String APK_DIRECTORY="/IB42/apk/";
	/**
	 * SD卡下载专辑图片目录
	 * */
	public static final String DOWNLOAD_ALBUM_DIRECTORY="/IB42/download_album/";

	public static final String KEY_SKINID = "skin_id";
	
	public static final String KEY_PLAYER_CURRENTDURATION="player_currentduration";//已经播放时长
	public static final String KEY_PLAYER_MODE="player_mode";//播放模式
	public static final String KEY_PLAYER_ALBUMID="albumid";//播放列表查询参数
	public static final String KEY_PLAYER_AUDIOID="audioid";//播放列表查询参数

	public static final String KEY_ISSTARTUP="isStartup";//是否是刚启动
	
	public static final String KEY_ISSCANNERTIP="isScannerTip";//是否显示要扫描提示

	public static final String KEY_GENERAL_SCANNERTIP="generalScannerTip";//常规扫描

	public static final String KEY_AUTO_SLEEP="sleep";//定时关闭时间
	
	public static final String KEY_BRIGHTNESS="brightness";//屏幕模式->1:正常模式 0:夜间模式
	public static final float KEY_DARKNESS=0.1f;//夜间模式值level
	

	private SharedPreferences settingPreference;
	
	public SystemSetting(Context context, boolean isWrite) {
		settingPreference = context.getSharedPreferences(PREFERENCE_NAME,
				isWrite?Context.MODE_WORLD_READABLE:Context.MODE_WORLD_WRITEABLE);
	}
	
	/**
	 * 获取数据
	 * */
	public String getValue(String key){
		return settingPreference.getString(key, null);
	}
	/**
	 * 获取数据
	 * */
	public int getIntValue(String key){
		return settingPreference.getInt(key, 0);
	}
	/**
	 * 获取数据
	 * */
	public boolean getBooleanValue(String key){
		return settingPreference.getBoolean(key, false);
	}

	/**
	 * 保存播放信息[0:歌曲Id 1:已经播放时长 2:播放模式3:播放列表Flag4:播放列表查询参数 5:最近播放的]
	 * */
	public void setPlayerInfo(String[] playerInfos){
		Editor it = settingPreference.edit();
		it.putString(KEY_PLAYER_CURRENTDURATION, playerInfos[0]);
		it.putString(KEY_PLAYER_MODE, playerInfos[1]);
		it.putString(KEY_PLAYER_ALBUMID, playerInfos[2]);
		it.putString(KEY_PLAYER_AUDIOID, playerInfos[3]);
		it.commit();
	}
	

	/**
	 * 设置键值
	 * */
	public void setValue(String key,String value){
		Editor it = settingPreference.edit();
		it.putString(key, value);
		it.commit();
	}
	/**
	 * 设置键值
	 * */
	public void setValue(String key,boolean value){
		Editor it = settingPreference.edit();
		it.putBoolean(key, value);
		it.commit();
	}
}
