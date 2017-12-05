package com.xm.ib42.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.xm.ib42.constant.Constants;
import com.xm.ib42.entity.Album;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.entity.Column;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by long on 17-8-27.
 */

public class Utils {
    static Toast toast = null;

    public static void showToast(Context context, String str) {
        // Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();;
        if (toast == null) {
            toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        } else {
            toast.setText(str);
        }
        toast.show();
    }


    public static void logI(String str) {
        Log.i(Constants.TAG, str);
    }

    public static void logD(String str) {
        Log.d(Constants.TAG, str);
    }

    public static void logE(String str) {
        Log.e(Constants.TAG, str);
    }

    /**
     * 判断字符串是否为空 为空 返回 true
     *
     * @param cs
     * @return
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0 || cs.equals("null")) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    public static JSONObject parseResponse(HttpResponse response){
        HttpEntity httpEntity;
        InputStream stream = null;
        StringBuffer sb = new StringBuffer();
        JSONObject jsonObject = null;
        try{
            if (response != null && response.getStatusLine() != null &&
                    response.getStatusLine().getStatusCode() == 200) {
                // 响应的实体，代表接受http的消息，服务器返回的消息都在Entity
                httpEntity = response.getEntity();
                // 通过httpEntity可以得到流
                stream = httpEntity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(stream));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String json = sb.toString();
                if (isNotBlank(json)){
                    jsonObject = new JSONObject(json);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static List<Column> pressColumnJson(JSONObject jsonObject){
        if (jsonObject == null)return null;
        List<Column> list = new ArrayList<>();
        JSONArray jsonArray = jsonObject.optJSONArray("posts");
        for (int i = 0; jsonArray != null && i < jsonArray.length(); i++){
            JSONObject object = jsonArray.optJSONObject(i);
            Column column = new Column();
            column.setId(object.optInt("id"));
            column.setTitle(object.optString("title"));
            column.setUrl(object.optString("url"));
            list.add(column);
        }
        return list;
    }

    public static List<Album> pressAlbumJson(JSONObject jsonObject){
        if (jsonObject == null)return null;
        List<Album> list = new ArrayList<>();
        JSONArray jsonArray = jsonObject.optJSONArray("posts");
        for (int i = 0; jsonArray != null && i < jsonArray.length(); i++){
            JSONObject object = jsonArray.optJSONObject(i);
            Album album = new Album();
            album.setId(object.optInt("id"));
            album.setTitle(object.optString("title"));
            album.setImageUrl(object.optString("url"));
            album.setAudioNum(object.optInt("count_zt"));
            list.add(album);
        }
        return list;
    }

    public static List<Audio> pressAudioJson(JSONObject jsonObject, Album album){
        if (jsonObject == null)return null;
        List<Audio> list = new ArrayList<>();
        JSONArray jsonArray = jsonObject.optJSONArray("posts");
        for (int i = 0; jsonArray != null && i < jsonArray.length(); i++){
            JSONObject object = jsonArray.optJSONObject(i);
            Audio audio = new Audio();
            audio.setId(object.optInt("id"));
            audio.setTitle(object.optString("title"));
            audio.setNetUrl(object.optString("url"));
            audio.setAlbum(album);
            list.add(audio);
        }
        return list;
    }
    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean isUpdate(String newVersion, Context context){
        if (isBlank(newVersion))return false;
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
            int version = packInfo.versionCode;
            if (Integer.parseInt(newVersion) > version){
                return true;
            } else {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 返回当前程序版本名
     */
    public static int getAppVersionName(Context context) {
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            int versioncode = pi.versionCode;
            return versioncode;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return 0;
    }

    public static Map<String, String> parseVersionData(String data){
        if (data == null)return null;
        data = data.replaceAll("\\{", "");
        data = data.replaceAll("\\}", "");
        String d[] = data.split(",");
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < d.length; i++) {
            String s[] = d[i].split(":");
            if (s.length >= 2){
                map.put(d[i].split(":")[0], d[i].split(":")[1]);
            }
        }
        return map;
    }

    public static String parseResponseData(HttpResponse response){
        HttpEntity httpEntity;
        InputStream stream = null;
        StringBuffer sb = new StringBuffer();
        try{
            if (response != null && response.getStatusLine() != null &&
                    response.getStatusLine().getStatusCode() == 200) {
                // 响应的实体，代表接受http的消息，服务器返回的消息都在Entity
                httpEntity = response.getEntity();
                // 通过httpEntity可以得到流
                stream = httpEntity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(stream));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String json = sb.toString();
                return json;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteDown(List<Audio> list){
        if (list == null) return;
        for (int i = 0; i < list.size(); i++) {
            Audio audio = list.get(i);
            File file = new File(audio.getFilePath());
            deleteFile(file);
        }
    }

    public static void deleteFile(File file) {
        if (file.exists()) { // 判断文件是否存在
            if (file.isFile()) { // 判断是否是文件
                file.delete(); // delete()方法 你应该知道 是删除的意思;
            } else if (file.isDirectory()) { // 否则如果它是一个目录
                File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
                for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
                    deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
                }
            }
            file.delete();
        } else {
//            Constants.Logdada("文件不存在！"+"\n");
        }
    }


    public static String gettim(int durctions) {
//        int mintue = durctions / 1000 / 60;
//        int second = (durctions - mintue * 60000) / 1000;
//        if (second < 10) {
//            return "0" + mintue + ":0" + second;
//        } else {
//            return "0" + mintue + ":" + second;
//        }
        if (durctions == 0) {
            return "00:00";
        }
        durctions = durctions / 1000;
        int m = durctions / 60;
        int s = durctions % 60;
        return (m > 9 ? m : "0" + m) + ":" + (s > 9 ? s : "0" + s);
    }

    public static String getTime(String durction) {
        try {
            int durctions = Integer.parseInt(durction);
            int mintue = durctions / 1000 / 60;
            int second = (durctions - mintue * 60000) / 1000;
            if (second < 10) {
                return "0" + mintue + ":0" + second;
            } else {
                return "0" + mintue + ":" + second;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "00:00";
        }
    }

    public static void saveHome(SharedPreferences preferences, List<Column> list){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("columnNum", list.size());
        for (int i = 0; i < list.size(); i++) {
            Column column = list.get(i);
            editor.putInt("columnId"+i, column.getId());
            editor.putInt("columnCount"+i, column.getCount());
            editor.putString("columnTitle"+i, column.getTitle());
            editor.putString("columnUrl"+i, column.getUrl());
            List<Album> albumList = column.getAlbumList();
            editor.putInt("albumCount", albumList.size());
            for (int j = 0; j < albumList.size(); j++) {
                Album album = albumList.get(j);
                editor.putInt("albumId"+i, album.getId());
                editor.putInt("albumAudioId"+i, album.getAudioId());
                editor.putInt("albumAudioNum"+i, album.getAudioNum());
                editor.putString("albumTitle"+i, album.getTitle());
                editor.putString("albumImg"+i, album.getImageUrl());
                editor.putString("albumAudioName"+i, album.getAudioName());
            }
            editor.commit();
        }
    }

    public static List<Column> getColumn(SharedPreferences preferences){
        List<Column> list = new ArrayList<>();
        int columnNum = preferences.getInt("columNum", 0);
        for (int i = 0; i < columnNum; i++) {
            Column column = new Column();
            column.setId(preferences.getInt("columnId"+i, 0));
            column.setCount(preferences.getInt("columnCount"+i, 0));
            column.setTitle(preferences.getString("columnTitle"+i, ""));
            column.setUrl(preferences.getString("columnUrl"+i, ""));
            int albumCount = preferences.getInt("albumCount", 0);
            List<Album> albumList = new ArrayList<>();
            for (int j = 0; j < albumCount; j++) {
                Album album = new Album();
                album.setId(preferences.getInt("albumId"+i, 0));
                album.setAudioId(preferences.getInt("albumAudioId"+i, 0));
                album.setAudioNum(preferences.getInt("albumAudioNum"+i, 0));
                album.setAudioName(preferences.getString("albumAudioName"+i, ""));
                album.setTitle(preferences.getString("albumTitle"+i, ""));
                album.setImageUrl(preferences.getString("albumImg"+i, ""));
                albumList.add(album);
            }
            column.setAlbumList(albumList);
            list.add(column);
        }
        return list;
    }

}
