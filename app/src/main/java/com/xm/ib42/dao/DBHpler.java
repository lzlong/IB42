package com.xm.ib42.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHpler extends SQLiteOpenHelper {


	private final Context context;

	public DBHpler(Context context) {
		super(context, DBData.DATABASE_NAME, null, DBData.VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 创建专辑表
		db.execSQL("CREATE TABLE " + DBData.ALBUM_TABLENAME + "("
				+ DBData.ALBUM_ID + " INTEGER,"
				+ DBData.ALBUM_NAME + " NVARCHAR(100),"
				+ DBData.ALBUM_AUDIO_ID + " INTEGER,"
				+ DBData.ALBUM_TIME + " INTEGER,"
				+ DBData.ALBUM_AUDIO_NAME + " NVARCHAR(300),"
				+ DBData.ALBUM_IMAGEURL + " NVARCHAR(300))");
		// 创建歌曲表
		db.execSQL("CREATE TABLE " + DBData.SONG_TABLENAME + "("
				+ DBData.SONG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DBData.SONG_ALBUMID + " INTEGER,"
				+ DBData.SONG_NAME + " NVARCHAR(100),"
				+ DBData.SONG_DISPLAYNAME + " NVARCHAR(100),"
				+ DBData.SONG_NETURL + " NVARCHAR(500),"
				+ DBData.SONG_DURATIONTIME + " INTEGER,"
				+ DBData.SONG_CURRDURATIONTIME + " INTEGER,"
				+ DBData.SONG_SIZE + " INTEGER,"
				+ DBData.SONG_FILEPATH + " NVARCHAR(300),"
				+ DBData.SONG_CACHEPATH + " NVARCHAR(300),"
				+ DBData.SONG_PLAYERLIST + " NVARCHAR(500),"
				+ DBData.SONG_ISNET + " INTEGER,"
				+ DBData.SONG_ISDOWNFINISH + " INTEGER, "
				+ DBData.SONG_ISCACHEFINISH + " INTEGER)");

		// 下载信息表
		db.execSQL("CREATE TABLE " + DBData.DOWNLOADINFO_TABLENAME + "("
				+ DBData.DOWNLOADINFO_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DBData.DOWNLOADINFO_URL + " NVARCHAR(300),"
				+ DBData.DOWNLOADINFO_NAME + " NVARCHAR(300),"
				+ DBData.DOWNLOADINFO_ALBUM + " NVARCHAR(100),"
				+ DBData.DOWNLOADINFO_DISPLAYNAME + " NVARCHAR(100),"
				+ DBData.DOWNLOADINFO_FILEPATH + " NVARCHAR(300),"
				+ DBData.DOWNLOADINFO_DURATIONTIME + " INTEGER,"
				+ DBData.DOWNLOADINFO_COMPLETESIZE + " INTEGER,"
				+ DBData.DOWNLOADINFO_FILESIZE + " INTEGER)");
//
//		// 多线程下载-每个线程信息表
//		db.execSQL("CREATE TABLE " + DBData.THREADINFO_TABLENAME + "("
//				+ DBData.THREADINFO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//				+ DBData.THREADINFO_STARTPOSITION + " INTEGER,"
//				+ DBData.THREADINFO_ENDPOSITION + " INTEGER,"
//				+ DBData.THREADINFO_COMPLETESIZE + " INTEGER,"
//				+ DBData.THREADINFO_DOWNLOADINFOID + " INTEGER)");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 删除表
//		db.execSQL("DROP TABLE IF EXISTS " + DBData.ALBUM_TABLENAME);
//		db.execSQL("DROP TABLE IF EXISTS " + DBData.SONG_TABLENAME);
//		db.execSQL("DROP TABLE IF EXISTS " + DBData.THREADINFO_TABLENAME);
//		db.execSQL("DROP TABLE IF EXISTS " + DBData.DOWNLOADINFO_TABLENAME);
	}

}
