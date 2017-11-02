package com.xm.ib42.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xm.ib42.entity.Album;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.util.Common;

import java.util.ArrayList;
import java.util.List;

/**
 * 音频DAO
 * */
public class AudioDao {
	private DBHpler dbHpler;

	public AudioDao(Context context) {
		dbHpler = new DBHpler(context);
	}

    /**
	 * 查询所有目录
	 * */
	public List<String[]> searchByDirectory() {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<String[]> list = new ArrayList<String[]>();
		StringBuffer sb = new StringBuffer();
		Cursor cr = db.rawQuery("SELECT " + DBData.SONG_FILEPATH + ","
				+ DBData.SONG_ID + " FROM " + DBData.SONG_TABLENAME
				+ " ORDER BY " + DBData.SONG_FILEPATH + " DESC", null);
		while (cr.moveToNext()) {
			String filepath = Common.clearFileName(
					cr.getString(cr.getColumnIndex(DBData.SONG_FILEPATH)))
					.toLowerCase();
			if (!sb.toString().contains("$" + filepath + "$")) {
				sb.append("$").append(filepath).append("$");
				String[] s = new String[3];
				s[0] = filepath;
				s[1] = filepath;
				s[2] = "";
				list.add(s);
			}
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * 添加
	 * */
	public long add(Audio audio) {
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBData.SONG_DISPLAYNAME, audio.getDisplayName());
		values.put(DBData.SONG_FILEPATH, audio.getFilePath());
		values.put(DBData.SONG_NAME, audio.getTitle());
		values.put(DBData.SONG_ALBUMID, audio.getAlbum().getId());
		values.put(DBData.SONG_NETURL, audio.getNetUrl());
		values.put(DBData.SONG_DURATIONTIME, audio.getDurationTime());
		values.put(DBData.SONG_SIZE, audio.getSize());
		values.put(DBData.SONG_PLAYERLIST, audio.getPlayerList());
		values.put(DBData.SONG_ISDOWNFINISH, audio.isDownFinish());
		values.put(DBData.SONG_ISCACHEFINISH, audio.isCacheFinish());
		values.put(DBData.SONG_CACHEPATH, audio.getCachePath());
		values.put(DBData.SONG_ISNET, audio.isNet());
		long rs = 0;
        if (searchById(audio.getId(), false) != null){
            rs += db.update(DBData.SONG_TABLENAME, values, DBData.SONG_ID + " = ?", new String[]{audio.getId()+""});
        } else {
            rs += db.insert(DBData.SONG_TABLENAME, DBData.SONG_NAME, values);
        }
		db.close();
		return rs;
	}
	public long add(List<Audio> audioList) {
		if (audioList == null)return -1;
		long rs = 0;
        SQLiteDatabase db = dbHpler.getWritableDatabase();
        for (int i = 0; i < audioList.size(); i++) {
            Audio audio = audioList.get(i);
			ContentValues values = new ContentValues();
			values.put(DBData.SONG_DISPLAYNAME, audio.getDisplayName());
			values.put(DBData.SONG_FILEPATH, audio.getFilePath());
			values.put(DBData.SONG_NAME, audio.getTitle());
			values.put(DBData.SONG_ALBUMID, audio.getAlbum().getId());
			values.put(DBData.SONG_NETURL, audio.getNetUrl());
			values.put(DBData.SONG_DURATIONTIME, audio.getDurationTime());
			values.put(DBData.SONG_SIZE, audio.getSize());
			values.put(DBData.SONG_PLAYERLIST, audio.getPlayerList());
			values.put(DBData.SONG_ISDOWNFINISH, audio.isDownFinish());
			values.put(DBData.SONG_ISCACHEFINISH, audio.isCacheFinish());
            values.put(DBData.SONG_CACHEPATH, audio.getCachePath());
			values.put(DBData.SONG_ISNET, audio.isNet());
            if (searchById(audio.getId(), false) != null){
                rs += db.update(DBData.SONG_TABLENAME, values, DBData.SONG_ID + " = ?", new String[]{audio.getId()+""});
            } else {
                rs += db.insert(DBData.SONG_TABLENAME, DBData.SONG_NAME, values);
            }
		}
		db.close();
		return rs;
	}

	/**
	 * 获取记录总数
	 * */
	public int getCount() {
		int count = 0;
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		Cursor cr = db.rawQuery(
				"SELECT COUNT(*) FROM " + DBData.SONG_TABLENAME, null);
		if (cr.moveToNext()) {
			count = cr.getInt(0);
		}
		cr.close();
		db.close();
		return count;
	}

	/**
	 * 某首音频从播放列表中删除
	 * */
	public int deleteByPlayerList(int id, int pid) {
		int rs = 0;
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		Cursor cr = db.rawQuery("SELECT " + DBData.SONG_PLAYERLIST + " FROM "
				+ DBData.SONG_TABLENAME + " WHERE " + DBData.SONG_ID + "=?",
				new String[] { String.valueOf(id) });
		String temp_pl = null;
		if (cr.moveToNext()) {
			temp_pl = cr.getString(0);
		}
		cr.close();
		if (temp_pl != null) {
			ContentValues values = new ContentValues();
			values.put(DBData.SONG_PLAYERLIST,
					temp_pl.replaceAll("$" + pid + "$", ""));
			rs = db.update(DBData.SONG_TABLENAME, values,
					DBData.SONG_ID + "=?", new String[] { String.valueOf(id) });
		}
		db.close();
		return rs;
	}
	/**
	 * 删除专辑所有音频
	 * */
	public int deleteByAlbum(int albumId){
		SQLiteDatabase db=dbHpler.getWritableDatabase();
		int rs=db.delete(DBData.SONG_TABLENAME, DBData.SONG_ALBUMID+"=?",new String[]{String.valueOf(albumId)});
		db.close();
		return rs;
	}

	/**
	 * 删除专辑所有下载音频
	 * */
	public int updateDownByAlbum(int albumId){
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		int rs = 0;
		ContentValues values = new ContentValues();
		values.put(DBData.SONG_ISDOWNFINISH, 0);
		values.put(DBData.SONG_FILEPATH, "");
		rs = db.update(DBData.SONG_TABLENAME, values,
				DBData.SONG_ALBUMID + "=?", new String[] { String.valueOf(albumId) });
		db.close();
		return rs;
	}

	/**
	 * 删除
	 * */
	public int delete(Integer... ids) {
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		StringBuilder sb = new StringBuilder();
		String[] idstr = new String[ids.length];
		for (int i = 0; i < ids.length; i++) {
			sb.append("?,");
			idstr[i] = String.valueOf(ids[i]);
			Cursor cr = db.query(DBData.SONG_TABLENAME, new String[] {
					DBData.SONG_ALBUMID }, DBData.SONG_ID
					+ "=?", new String[] { idstr[i] }, null, null, null);
			if (cr.moveToNext()) {
				final int artistid = cr.getInt(0);
				final int albumid = cr.getInt(1);
				// 删除专辑
				Cursor album_cr = db.rawQuery("SELECT COUNT(*) FROM "
						+ DBData.SONG_TABLENAME + " WHERE "
						+ DBData.SONG_ALBUMID + "=?",
						new String[] { String.valueOf(albumid) });
				if (album_cr.getCount() == 1) {
					db.delete(DBData.ALBUM_TABLENAME, DBData.ALBUM_ID + "=?",
							new String[] { String.valueOf(albumid) });
				}
			}
			cr.close();
		}
		sb.deleteCharAt(sb.length() - 1);
		int rs = db.delete(DBData.SONG_TABLENAME,
				DBData.SONG_ID + " in(" + sb.toString() + ")", idstr);
		db.close();
		return rs;
	}

	/**
	 * 查询全部音频
	 * */
	public List<Audio> searchAll() {
		return commonSearch("", null);
	}

	/**
	 * 通用查询
	 * */
	private List<Audio> commonSearch(String whereString, String[] params) {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<Audio> list = new ArrayList<Audio>();
		Audio audio = null;
		Cursor cr = db.rawQuery("SELECT A." + DBData.SONG_ID + ", A."
				+ DBData.SONG_DISPLAYNAME + ",A."
				+ DBData.SONG_NAME + " AS Aname" + ",A." + DBData.SONG_ALBUMID
				+ ",A." + DBData.SONG_FILEPATH + ",A."
				+ DBData.SONG_DURATIONTIME + " FROM " + DBData.SONG_TABLENAME
				+ " AS A " + whereString, params);

		while (cr.moveToNext()) {
			audio = new Audio();
			audio.setId(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
			audio.setDisplayName(cr.getString(cr
					.getColumnIndex(DBData.SONG_DISPLAYNAME)));
//			audio.setAlbum(new Album(cr.getInt(cr
//					.getColumnIndex(DBData.SONG_ALBUMID)), cr.getString(cr
//					.getColumnIndex("Cname")), cr.getString(cr
//					.getColumnIndex("Cpicpath"))));
			audio.setTitle(cr.getString(cr.getColumnIndex("Aname")));
			audio.setFilePath(cr.getString(cr
					.getColumnIndex(DBData.SONG_FILEPATH)));
			audio.setDurationTime(cr.getInt(cr
					.getColumnIndex(DBData.SONG_DURATIONTIME)));
			list.add(audio);
		}
		cr.close();
		db.close();
		return list;
	}

	public List<String> searchByName(){
		List<String> lNameList = new ArrayList<String>();
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		List<String[]> list = new ArrayList<String[]>();
		Cursor cr = db.rawQuery("select " + DBData.SONG_NAME + " from " + DBData.SONG_TABLENAME, null);
		while (cr.moveToNext()) {
			String lString = cr.getString(cr.getColumnIndex(DBData.SONG_NAME));
			lNameList.add(lString);
		}
		cr.close();
		db.close();
		return lNameList;
	}

	public int searchSongIdByName(String name){
		int id = 0;
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		Cursor cr = db.query(DBData.SONG_TABLENAME, new String[]{DBData.SONG_ID}, DBData.SONG_NAME+ "=?", new String[]{name}, null, null, null);
		if (cr.moveToFirst()) {
			id = cr.getInt(cr.getColumnIndex(DBData.SONG_ID));
		}
		cr.close();
		db.close();
		return id;
	}

	/**
	 * 根据专辑查询
	 * */
	public List<Audio> searchByAlbum(String albumId) {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<Audio> list = new ArrayList<Audio>();
		Cursor cr = db.rawQuery("SELECT * FROM "
				+ DBData.SONG_TABLENAME + " WHERE "
				+ DBData.SONG_ALBUMID + "=? ", new String[] { albumId });
		while (cr.moveToNext()) {
			Audio audio = new Audio();
            audio.setId(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
            Album album = new Album();
            album.setId(cr.getInt(cr.getColumnIndex(DBData.SONG_ALBUMID)));
            audio.setAlbum(album);
            audio.setTitle(cr.getString(cr.getColumnIndex(DBData.SONG_NAME)));
            audio.setDisplayName(Common.clearSuffix(cr.getString(cr
                    .getColumnIndex(DBData.SONG_DISPLAYNAME))));
			audio.setNetUrl(cr.getString(cr.getColumnIndex(DBData.SONG_NETURL)));
			audio.setDurationTime(cr.getColumnIndex(DBData.SONG_DURATIONTIME));
			audio.setSize(cr.getInt(cr.getColumnIndex(DBData.SONG_SIZE)));
			audio.setFilePath(cr.getString(cr.getColumnIndex(DBData.SONG_FILEPATH)));
//			audio.setNet(cr.getString(cr.getColumnIndex(DBData.SONG_ISNET)));
            if (cr.getColumnIndex(DBData.SONG_ISDOWNFINISH) == 1){
                audio.setDownFinish(true);
            } else {
                audio.setDownFinish(false);
            }
            audio.setCachePath(cr.getString(cr.getColumnIndex(DBData.SONG_CACHEPATH)));
            if (cr.getColumnIndex(DBData.SONG_ISCACHEFINISH) == 1){
                audio.setCacheFinish(true);
            } else {
                audio.setCacheFinish(false);
            }
			list.add(audio);
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * 根据专辑查询
	 * */
	public List<Audio> searchAlbum(String albumId) {
		return commonSearch(" WHERE A." + DBData.SONG_ALBUMID + "=?",
				new String[] { albumId });
	}

	/**
	 * 返回所有音频路径用'$[string]$'分隔
	 * */
	public String getFilePathALL() {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		StringBuffer sb = new StringBuffer();
		Cursor cr = db.rawQuery("SELECT " + DBData.SONG_FILEPATH + " FROM "
				+ DBData.SONG_TABLENAME + " ORDER BY " + DBData.SONG_ID
				+ " DESC", null);
		while (cr.moveToNext()) {
			sb.append("$")
					.append(cr.getString(cr
							.getColumnIndex(DBData.SONG_FILEPATH))).append("$");
		}
		cr.close();
		db.close();
		return sb.toString();
	}

	private List<Audio> addList(Cursor cr) {
		List<Audio> list = new ArrayList<Audio>();
		Audio audio = null;
		while (cr.moveToNext()) {
			audio = new Audio();
			audio.setId(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
//			audio.setAlbum(new Album(cr.getInt(cr
//					.getColumnIndex(DBData.SONG_ALBUMID)), null, null));
			audio.setDisplayName(cr.getString(cr
					.getColumnIndex(DBData.SONG_DISPLAYNAME)));
			audio.setDownFinish(cr.getInt(cr
					.getColumnIndex(DBData.SONG_ISDOWNFINISH)) == 1 ? true
					: false);
			audio.setDurationTime(cr.getInt(cr
					.getColumnIndex(DBData.SONG_DURATIONTIME)));
			audio.setFilePath(cr.getString(cr
					.getColumnIndex(DBData.SONG_FILEPATH)));
			audio.setTitle(cr.getString(cr.getColumnIndex(DBData.SONG_NAME)));
			audio.setNet(cr.getInt(cr.getColumnIndex(DBData.SONG_ISNET)) == 1 ? true
					: false);
			audio.setNetUrl(cr.getString(cr.getColumnIndex(DBData.SONG_NETURL)));
			audio.setPlayerList(cr.getString(cr
					.getColumnIndex(DBData.SONG_PLAYERLIST)));
			audio.setSize(cr.getInt(cr.getColumnIndex(DBData.SONG_SIZE)));
			list.add(audio);
		}
		return list;
	}

	/**
	 * 分页查询
	 * */
	public List<Audio> searchByPage(int pageindex, int pagesize) {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<Audio> list;
		Cursor cr = db.rawQuery("SELECT * FROM " + DBData.SONG_TABLENAME
				+ " LIMIT ?,? ORDER BY " + DBData.SONG_NAME + " DESC",
				new String[] { String.valueOf((pageindex - 1) * pagesize),
						String.valueOf(pagesize) });
		list = addList(cr);
		cr.close();
		db.close();
		return list;
	}

	/**
	 * 更新播放列表---添加音频到播放列表
	 * */
	public void updateByPlayerList(int id, int pid) {
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		Cursor cr = db.rawQuery("SELECT " + DBData.SONG_PLAYERLIST + " FROM "
				+ DBData.SONG_TABLENAME + " WHERE " + DBData.SONG_ID + "=?",
				new String[] { String.valueOf(id) });
		String temp_pl = null;
		if (cr.moveToNext()) {
			temp_pl = cr.getString(0);
		}
		cr.close();
		if (!temp_pl.contains("$" + pid + "$")) {
			db.execSQL("UPDATE " + DBData.SONG_TABLENAME + " SET "
					+ DBData.SONG_PLAYERLIST + "=? WHERE " + DBData.SONG_ID
					+ "=? ", new Object[] { temp_pl + "$" + pid + "$", id });
		}
		db.close();
	}

	/**
	 * 根据id查询音频信息
	 * */
	public Audio searchById(int id, boolean isClose) {
		Audio audio = null;
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		Cursor cr = db.rawQuery("SELECT  A." + DBData.SONG_DISPLAYNAME
                + ",C." + DBData.ALBUM_NAME+ " AS Cname"
                + ",A." + DBData.SONG_NAME + " AS Aname"
                + ",A." + DBData.SONG_ALBUMID
                + ",A." + DBData.SONG_FILEPATH
                + ",A." + DBData.SONG_DURATIONTIME
				+ ",A." + DBData.SONG_SIZE
                + " FROM " + DBData.SONG_TABLENAME
				+ " AS A INNER JOIN "  + DBData.ALBUM_TABLENAME
				+ " AS C ON A." + DBData.SONG_ALBUMID + "=C." + DBData.ALBUM_ID
				+ " WHERE A." + DBData.SONG_ID + "=?",
				new String[] { String.valueOf(id) });
		if (cr.moveToNext()) {
			audio = new Audio();
			audio.setId(id);
			audio.setDisplayName(cr.getString(cr
					.getColumnIndex(DBData.SONG_DISPLAYNAME)));
//			audio.setAlbum(new Album(cr.getInt(cr
//					.getColumnIndex(DBData.SONG_ALBUMID)), cr.getString(cr
//					.getColumnIndex("Cname")), cr.getString(cr
//					.getColumnIndex("Cpicpath"))));
			audio.setTitle(cr.getString(cr.getColumnIndex("Aname")));
			audio.setFilePath(cr.getString(cr
					.getColumnIndex(DBData.SONG_FILEPATH)));
			audio.setDurationTime(cr.getInt(cr
					.getColumnIndex(DBData.SONG_DURATIONTIME)));
			audio.setSize(cr.getInt(cr.getColumnIndex(DBData.SONG_SIZE)));
		}
		if (isClose){
			cr.close();
			db.close();
		}
		return audio;
	}

	/**
	 * 更新文件大小
	 * */
	public void updateBySize(int id, int size) {
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		db.execSQL("UPDATE " + DBData.SONG_TABLENAME + " SET "
				+ DBData.SONG_SIZE + "=" + size + " WHERE "
				+ DBData.SONG_ID + "=" + id);
	}
	
	/**
	 * 更新播放时长
	 * */
	public void updateByDuration(int id, int duration) {
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		db.execSQL("UPDATE " + DBData.SONG_TABLENAME + " SET "
				+ DBData.SONG_CURRDURATIONTIME + "=" + duration + "  WHERE "
				+ DBData.SONG_ID + "=" + id);
	}
	
	/**
	 * 查询完成下载的音频
	 * */
	public List<Audio> searchByDownLoad() {
		List<Audio> list = new ArrayList<Audio>();
		Audio audio = null;
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		Cursor cr = db.rawQuery("SELECT A." + DBData.SONG_ID
                + ",A." + DBData.SONG_FILEPATH
                + ",A." + DBData.SONG_ALBUMID
				+ ",A." + DBData.SONG_NAME
				+ " AS Aname" + " FROM "
				+ DBData.SONG_TABLENAME + " AS A "
                + " WHERE A." + DBData.SONG_ISDOWNFINISH + "=1", null);
		while (cr.moveToNext()) {
			audio = new Audio();
			audio.setId(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
			audio.setTitle(cr.getString(cr.getColumnIndex("Aname")));
			audio.setFilePath(cr.getString(cr
					.getColumnIndex(DBData.SONG_FILEPATH)));
			Album album = new Album();
			album.setId(cr.getInt(cr.getColumnIndex(DBData.SONG_ALBUMID)));
			audio.setAlbum(album);
			list.add(audio);
		}
		cr.close();
		db.close();
		return list;
	}
	
	/**
	 * 查询完成下载的音频
	 * */
	public List<Audio> searchDownLoad() {
		return commonSearch(" WHERE A." + DBData.SONG_ISDOWNFINISH + "=1", null);
	}
	/**
	 * 查询完成下载的音频
	 * */
	public List<Audio> searchDownLoad(String albumId) {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<Audio> list = new ArrayList<>();
		Cursor cr = db.rawQuery("SELECT * FROM "
				+ DBData.SONG_TABLENAME + " WHERE "
				+ DBData.SONG_ALBUMID + "=? AND "
				+ DBData.SONG_ISDOWNFINISH + "=? ", new String[] {albumId, "1"});
		while (cr.moveToNext()) {
			Audio audio = new Audio();
			audio.setId(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
			Album album = new Album();
			album.setId(cr.getInt(cr.getColumnIndex(DBData.SONG_ALBUMID)));
			audio.setAlbum(album);
			audio.setTitle(cr.getString(cr.getColumnIndex(DBData.SONG_NAME)));
			audio.setDisplayName(Common.clearSuffix(cr.getString(cr
					.getColumnIndex(DBData.SONG_DISPLAYNAME))));
			audio.setNetUrl(cr.getString(cr.getColumnIndex(DBData.SONG_NETURL)));
			audio.setDurationTime(cr.getColumnIndex(DBData.SONG_DURATIONTIME));
			audio.setSize(cr.getInt(cr.getColumnIndex(DBData.SONG_SIZE)));
			audio.setFilePath(cr.getString(cr.getColumnIndex(DBData.SONG_FILEPATH)));
//			audio.setPlayerList();cr.getString(cr.getColumnIndex(DBData.SONG_PLAYERLIST));
//			audio.setNet(cr.getString(cr.getColumnIndex(DBData.SONG_ISNET)));
			if (cr.getColumnIndex(DBData.SONG_ISDOWNFINISH) == 1){
				audio.setDownFinish(true);
			} else {
				audio.setDownFinish(false);
			}
            audio.setCachePath(cr.getString(cr.getColumnIndex(DBData.SONG_CACHEPATH)));
            if (cr.getColumnIndex(DBData.SONG_ISCACHEFINISH) == 1){
                audio.setCacheFinish(true);
            } else {
                audio.setCacheFinish(false);
            }
			list.add(audio);
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * 更新下载完成状态： id为-1表示全部
	 * */
	public int updateByDownLoadState(int id) {
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		int rs = 0;
		ContentValues values = new ContentValues();
		values.put(DBData.SONG_ISDOWNFINISH, 1);
		if (id == -1) {
			rs = db.update(DBData.SONG_TABLENAME, values, null, null);
		} else {
			rs = db.update(DBData.SONG_TABLENAME, values,
					DBData.SONG_ID + "=?", new String[] { String.valueOf(id) });
		}
		db.close();
		return rs;
	}

    /**
     * 更新缓存完成状态： id为-1表示全部
     * */
    public int updateByCacheState(int id, String cachePath) {
        SQLiteDatabase db = dbHpler.getWritableDatabase();
        int rs = 0;
        ContentValues values = new ContentValues();
        values.put(DBData.SONG_CACHEPATH, cachePath);
        values.put(DBData.SONG_ISCACHEFINISH, 1);
        if (id == -1) {
            rs = db.update(DBData.SONG_TABLENAME, values, null, null);
        } else {
            rs = db.update(DBData.SONG_TABLENAME, values,
                    DBData.SONG_ID + "=?", new String[] { String.valueOf(id) });
        }
        db.close();
        return rs;
    }

	/**
	 * 判断是否存在
	 * */
	public boolean isExist(int id) {
		int rs = 0;
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		Cursor cr = db.rawQuery("SELECT COUNT(*) FROM " + DBData.SONG_TABLENAME
				+ " WHERE " + DBData.SONG_ID + "=?", new String[] {id+""});
		while (cr.moveToNext()) {
			rs = cr.getInt(0);
		}
		cr.close();
		db.close();
		return rs > 0;
	}

	/**
	 * 判断下载任务是否存在
	 * */
	public boolean isExist(String url) {
		int rs = 0;
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		Cursor cr = db.rawQuery("SELECT COUNT(*) FROM " + DBData.SONG_TABLENAME
				+ " WHERE " + DBData.SONG_NETURL + "=?", new String[] { url });
		while (cr.moveToNext()) {
			rs = cr.getInt(0);
		}
		cr.close();
		db.close();
		return rs > 0;
	}

    /**
     * 判断下载任务是否存在
     * */
    public boolean isDownFinish(int audioId) {
        SQLiteDatabase db = dbHpler.getReadableDatabase();
        Cursor cr = db.rawQuery("SELECT "+DBData.SONG_ISDOWNFINISH+" FROM " + DBData.SONG_TABLENAME
                + " WHERE " + DBData.SONG_ID + "=?", new String[] { audioId+"" });
        while (cr.moveToNext()) {
            if (cr.getInt(cr.getColumnIndex(DBData.SONG_ISDOWNFINISH)) == 0){
                return false;
            } else {
                return true;
            }
        }
        cr.close();
        db.close();
        return false;
    }
    /**
     * 判断缓存任务是否存在
     * */
    public boolean isCacheFinish(int audioId) {
        SQLiteDatabase db = dbHpler.getReadableDatabase();
        Cursor cr = db.rawQuery("SELECT "+DBData.SONG_ISCACHEFINISH+" FROM " + DBData.SONG_TABLENAME
                + " WHERE " + DBData.SONG_ID + "=?", new String[] { audioId+"" });
        while (cr.moveToNext()) {
            if (cr.getInt(cr.getColumnIndex(DBData.SONG_ISCACHEFINISH)) == 0){
                return false;
            } else {
                return true;
            }
        }
        cr.close();
        db.close();
        return false;
    }
}
