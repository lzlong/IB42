package com.xm.ib42.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xm.ib42.entity.Album;

import java.util.ArrayList;
import java.util.List;

/**
 * 专辑DAO
 * */
public class AlbumDao {
	private DBHpler dbHpler;
	
	public AlbumDao(Context context){
		dbHpler=new DBHpler(context);
	}
	
	/**
	 * 全部查询
	 * */
	public List<Album> searchAll(){
		List<Album> list=new ArrayList<>();
		SQLiteDatabase db=dbHpler.getReadableDatabase();
		Cursor cr=db.rawQuery("SELECT * FROM "+DBData.ALBUM_TABLENAME+" ORDER BY "+DBData.ALBUM_NAME+" DESC",null);
		while(cr.moveToNext()){
			Album album = new Album();
			album.setId(cr.getInt(cr.getColumnIndex(DBData.ALBUM_ID)));
			album.setTitle(cr.getString(cr.getColumnIndex(DBData.ALBUM_NAME)));
			album.setImageUrl(cr.getString(cr.getColumnIndex(DBData.ALBUM_IMAGEURL)));
			album.setAudioNameDesc(cr.getString(cr.getColumnIndex(DBData.ALBUM_AUDIO_NAME_DESC)));
			album.setAudioIdDesc(cr.getInt(cr.getColumnIndex(DBData.ALBUM_AUDIO_ID_DESC)));
			album.setAudioNameAsc(cr.getString(cr.getColumnIndex(DBData.ALBUM_AUDIO_NAME_ASC)));
			album.setAudioIdAsc(cr.getInt(cr.getColumnIndex(DBData.ALBUM_AUDIO_ID_ASC)));
			album.setYppx(cr.getInt(cr.getColumnIndex(DBData.ALBUM_YPPX)));
			int isDelete = cr.getInt(cr.getColumnIndex(DBData.ALBUM_ISDELETE));
			if (isDelete == 0){
				album.setDelete(false);
			} else {
				album.setDelete(true);
			}
			list.add(album);
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * 根据id查询
	 * */
	public Album searchById(int id){
		SQLiteDatabase db=dbHpler.getReadableDatabase();
		Cursor cr=db.rawQuery("SELECT * FROM "+DBData.ALBUM_TABLENAME+" WHERE "+DBData.ALBUM_ID+" =?", new String[]{id+""});
		if(cr.moveToNext()){
			Album album = new Album();
			album.setId(cr.getInt(cr.getColumnIndex(DBData.ALBUM_ID)));
			album.setTitle(cr.getString(cr.getColumnIndex(DBData.ALBUM_NAME)));
			album.setImageUrl(cr.getString(cr.getColumnIndex(DBData.ALBUM_IMAGEURL)));
			album.setAudioNameDesc(cr.getString(cr.getColumnIndex(DBData.ALBUM_AUDIO_NAME_DESC)));
			album.setAudioIdDesc(cr.getInt(cr.getColumnIndex(DBData.ALBUM_AUDIO_ID_DESC)));
			album.setAudioNameAsc(cr.getString(cr.getColumnIndex(DBData.ALBUM_AUDIO_NAME_ASC)));
			album.setAudioIdAsc(cr.getInt(cr.getColumnIndex(DBData.ALBUM_AUDIO_ID_ASC)));
			album.setYppx(cr.getInt(cr.getColumnIndex(DBData.ALBUM_YPPX)));
			int d = cr.getInt(cr.getColumnIndex(DBData.ALBUM_ISDELETE));
			if (d == 0){
				album.setDelete(false);
			} else {
				album.setDelete(true);
			}
            return album;
		}
		cr.close();
		db.close();
		return null;
	}
	
	/**
	 * 判断专辑是否存在，存在返回id
	 * */
	public int isExist(String name){
		int id=-1;
		SQLiteDatabase db=dbHpler.getReadableDatabase();
		Cursor cr=db.rawQuery("SELECT "+DBData.ALBUM_ID+" FROM "+DBData.ALBUM_TABLENAME+" WHERE "+DBData.ALBUM_NAME+"=?", new String[]{name});
		if(cr.moveToNext()){
			id=cr.getInt(0);
		}
		cr.close();
		db.close();
		return id;
	}
	
	/**
	 * 获取记录总数
	 * */
	public int getCount(){
		int count=0;
		SQLiteDatabase db=dbHpler.getReadableDatabase();
		Cursor cr=db.rawQuery("SELECT COUNT(*) FROM "+DBData.ALBUM_TABLENAME, null);
		if(cr.moveToNext()){
			count=cr.getInt(0);
		}
		cr.close();
		db.close();
		return count;
	}
	
	/**
	 * 添加
	 * */
	public long add(Album album){
		if (isExist(album.getTitle()) != -1){
			update(album);
			return -1;
		}
		SQLiteDatabase db=dbHpler.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(DBData.ALBUM_ID, album.getId());
		values.put(DBData.ALBUM_NAME, album.getTitle());
		values.put(DBData.ALBUM_AUDIO_ID_DESC, album.getAudioIdDesc());
		values.put(DBData.ALBUM_AUDIO_NAME_DESC, album.getAudioNameDesc());
		values.put(DBData.ALBUM_AUDIO_ID_ASC, album.getAudioIdAsc());
		values.put(DBData.ALBUM_AUDIO_NAME_ASC, album.getAudioNameAsc());
		values.put(DBData.ALBUM_IMAGEURL, album.getImageUrl());
		values.put(DBData.ALBUM_TIME, System.currentTimeMillis());
		values.put(DBData.ALBUM_YPPX, album.getYppx());
		values.put(DBData.ALBUM_ISDELETE, 0);
		long rs=db.insert(DBData.ALBUM_TABLENAME, DBData.ALBUM_NAME, values);
		db.close();
		return rs;
	}
	
	/**
	 * 删除
	 * */
	public int delete(int id){
		SQLiteDatabase db=dbHpler.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(DBData.ALBUM_ISDELETE, 1);
		int rs=db.update(DBData.ALBUM_TABLENAME, values, DBData.ALBUM_ID+"=?",new String[]{String.valueOf(id)});
		db.close();
		return rs;
	}
	
	/**
	 * 更新
	 * */
	public int update(Album album){
		SQLiteDatabase db=dbHpler.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(DBData.ALBUM_ID, album.getId());
		values.put(DBData.ALBUM_NAME, album.getTitle());
		values.put(DBData.ALBUM_AUDIO_ID_DESC, album.getAudioIdDesc());
		values.put(DBData.ALBUM_AUDIO_NAME_DESC, album.getAudioNameDesc());
		values.put(DBData.ALBUM_AUDIO_ID_ASC, album.getAudioIdAsc());
		values.put(DBData.ALBUM_AUDIO_NAME_ASC, album.getAudioNameAsc());
		values.put(DBData.ALBUM_IMAGEURL, album.getImageUrl());
		values.put(DBData.ALBUM_TIME, System.currentTimeMillis());
		values.put(DBData.ALBUM_YPPX, album.getYppx());
		values.put(DBData.ALBUM_ISDELETE, 0);
		int rs=db.update(DBData.ALBUM_TABLENAME, values, DBData.ALBUM_ID+"=?", new String[]{String.valueOf(album.getId())});
		db.close();
		return rs;
	}

}
