package com.xm.ib42.dao;


public class DBData {
	//数据库名称
	public static final String DATABASE_NAME="musicknow.db";
	//数据库版本
	public static final int VERSION=1;
	
	//音频字段
	public static final String SONG_TABLENAME="audio";
	public static final String SONG_ID="_id";
	public static final String SONG_ALBUMID="albumid";
	public static final String SONG_NAME="name";
	public static final String SONG_DISPLAYNAME="displayName";
	public static final String SONG_NETURL="netUrl";
	public static final String SONG_DURATIONTIME="durationTime";
	public static final String SONG_CURRDURATIONTIME="currDurationTime";
	public static final String SONG_SIZE="size";
	public static final String SONG_FILEPATH="filePath";
	public static final String SONG_PLAYERLIST="playerList";
	public static final String SONG_ISNET="isNet";
	public static final String SONG_ISDOWNFINISH="isDownFinish";
	public static final String SONG_ISCACHEFINISH="isCacheFinish";
	public static final String SONG_CACHEPATH="cachePath";

	//专辑字段
	public static final String ALBUM_TABLENAME="album";
	public static final String ALBUM_ID="_id";
	public static final String ALBUM_NAME="title";
	public static final String ALBUM_IMAGEURL="imageUrl";
	public static final String ALBUM_TIME="playDate";
	public static final String ALBUM_AUDIO_ID="audioId";
	public static final String ALBUM_AUDIO_NAME="audioName";
	public static final String ALBUM_ISDELETE="isDelete";

	//下载信息字段
	public static final String DOWNLOADINFO_TABLENAME="downLoadInfo";
	public static final String DOWNLOADINFO_ID="_id";
	public static final String DOWNLOADINFO_URL="url";
	public static final String DOWNLOADINFO_FILESIZE="filesize";
	public static final String DOWNLOADINFO_NAME="name";
	public static final String DOWNLOADINFO_ALBUM="album";
	public static final String DOWNLOADINFO_DISPLAYNAME="displayName";
	public static final String DOWNLOADINFO_DURATIONTIME="durationTime";
	public static final String DOWNLOADINFO_COMPLETESIZE="completeSize";
	public static final String DOWNLOADINFO_FILEPATH="filePath";
	public static final String DOWNLOADINFO_IMAGEURL="imgPath";
	public static final String DOWNLOADINFO_STATUS="status";
	public static final String DOWNLOADINFO_AUDIOID="audioId";
//
//	//多线程下载-每个线程信息字段
//	public static final String THREADINFO_TABLENAME="threadInfo";
//	public static final String THREADINFO_ID="_id";
//	public static final String THREADINFO_STARTPOSITION="startPosition";
//	public static final String THREADINFO_ENDPOSITION="endPosition";
//	public static final String THREADINFO_COMPLETESIZE="completeSize";
//	public static final String THREADINFO_DOWNLOADINFOID="downLoadInfoId";
	
}
