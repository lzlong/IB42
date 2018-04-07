package com.xm.ib42.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xm.ib42.app.MyApplication.context;


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
            if (column == null)continue;
            editor.putInt("columnId"+i, column.getId());
            editor.putInt("columnCount"+i, column.getCount());
            editor.putString("columnTitle"+i, column.getTitle());
            editor.putString("columnUrl"+i, column.getUrl());
            List<Album> albumList = column.getAlbumList();
            if (albumList == null){
                continue;
            }
            editor.putInt("albumCount"+i, albumList.size());
            for (int j = 0; j < albumList.size(); j++) {
                Album album = albumList.get(j);
                editor.putInt(i+"albumId"+j, album.getId());
                editor.putInt(i+"albumAudioNum"+j, album.getAudioNum());
                editor.putString(i+"albumTitle"+j, album.getTitle());
                editor.putString(i+"albumImg"+j, album.getImageUrl());
                editor.putInt(i+"albumYppx"+j, album.getYppx());
                editor.putInt(i+"albumAudioIdDesc"+j, album.getAudioIdDesc());
                editor.putString(i+"albumAudioNameDesc"+j, album.getAudioNameDesc());
                editor.putInt(i+"albumAudioIdAsc"+j, album.getAudioIdAsc());
                editor.putString(i+"albumAudioNameAsc"+j, album.getAudioNameAsc());
                if (album.getYppx() == 0){
                } else {
                }
            }
        }
        editor.commit();
    }

    public static List<Column> getColumn(SharedPreferences preferences){
        List<Column> list = new ArrayList<>();
        int columnNum = preferences.getInt("columnNum", 0);
        for (int i = 0; i < columnNum; i++) {
            Column column = new Column();
            column.setId(preferences.getInt("columnId"+i, 0));
            column.setCount(preferences.getInt("columnCount"+i, 0));
            column.setTitle(preferences.getString("columnTitle"+i, ""));
            column.setUrl(preferences.getString("columnUrl"+i, ""));
            int albumCount = preferences.getInt("albumCount"+i, 0);
            List<Album> albumList = new ArrayList<>();
            for (int j = 0; j < albumCount; j++) {
                Album album = new Album();
                album.setId(preferences.getInt(i+"albumId"+j, 0));
                album.setAudioNum(preferences.getInt(i+"albumAudioNum"+j, 0));
                album.setYppx(preferences.getInt(i+"albumYppx"+j, 0));
                album.setAudioIdDesc(preferences.getInt(i+"albumAudioIdDesc"+j, 0));
                album.setAudioNameDesc(preferences.getString(i+"albumAudioNameDesc"+j, ""));
                album.setAudioIdAsc(preferences.getInt(i+"albumAudioIdAsc"+j, 0));
                album.setAudioNameAsc(preferences.getString(i+"albumAudioNameAsc"+j, ""));
                if (album.getYppx() == 0){
                } else {
                }
                album.setTitle(preferences.getString(i+"albumTitle"+j, ""));
                album.setImageUrl(preferences.getString(i+"albumImg"+j, ""));
                albumList.add(album);
            }
            column.setAlbumList(albumList);
            list.add(column);
        }
        return list;
    }

    //检测当前的网络状态
    //API版本23以下时调用此方法进行检测
    //因为API23后getNetworkInfo(int networkType)方法被弃用
    public static boolean checkState_21(){
        //步骤1：通过Context.getSystemService(Context.CONNECTIVITY_SERVICE)获得ConnectivityManager对象
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //步骤2：获取ConnectivityManager对象对应的NetworkInfo对象
        //NetworkInfo对象包含网络连接的所有信息
        //步骤3：根据需要取出网络连接信息
        //获取WIFI连接的信息
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        Boolean isWifiConn = networkInfo.isConnected();

        //获取移动数据连接的信息
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        Boolean isMobileConn = networkInfo.isConnected();
        if (isWifiConn || isMobileConn){
            return true;
        }
        return false;
    }

    //API版本23及以上时调用此方法进行网络的检测
    //步骤非常类似
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean checkState_21orNew(){
        //获得ConnectivityManager对象
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //获取所有网络连接的信息
        Network[] networks = connMgr.getAllNetworks();
        //用于存放网络连接信息
        StringBuilder sb = new StringBuilder();
        //通过循环将网络信息逐个取出来
        for (int i=0; i < networks.length; i++){
            //获取ConnectivityManager对象对应的NetworkInfo对象
            NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
            if (networkInfo.isConnected()){
                return true;
            }
        }
        return false;
    }

    public static String pressUrl(String path){
        String name = path.substring(path.lastIndexOf("/")+1, path.length());
//        String[] names = name.split(" ");
        try {
//            Pattern p = Pattern.compile("[\\u4e00-\\u9fcc]+");
//            for (int i = 0; i < names.length; i++) {
////                Matcher m = p.matcher(name);
////                while (m.find()) {
////                    String n = m.group();
////                }
//                String n = names[i];
//                if (isNotBlank(n)){
//                    String s = URLEncoder.encode(n, "UTF-8");
//                    path = path.replace(n, s);
//                }
//            }
//            if (name.contains(" ")
//                    || name.contains(" ")
//                    || name.contains(" ")
//                    || name.contains("　")){
//            } else {
                path = path.replace(name, URLEncoder.encode(name, "UTF-8").replaceAll("\\+","%20"));
//            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
    }
    public static View getContentView(Activity ac){
        ViewGroup view = (ViewGroup)ac.getWindow().getDecorView();
        FrameLayout content = (FrameLayout)view.findViewById(android.R.id.content);
        return content.getChildAt(0);
    }


    public static void saveAudioList(SharedPreferences preferences, List<Audio> list, Album album){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioNum", list.size());
        editor.putInt("albumId", album.getId());
        editor.putInt("albumAudioNum", album.getAudioNum());
        editor.putString("albumTitle", album.getTitle());
        editor.putString("albumImg", album.getImageUrl());
        editor.putInt("albumYppx", album.getYppx());
        editor.putInt("albumAudioIdDesc", album.getAudioIdDesc());
        editor.putString("albumAudioNameDesc", album.getAudioNameDesc());
        editor.putInt("albumAudioIdAsc", album.getAudioIdAsc());
        editor.putString("albumAudioNameAsc", album.getAudioNameAsc());
        if (album.getYppx() == 0){
        } else {
        }
        for (int i = 0; i < list.size(); i++) {
            Audio audio = list.get(i);
            editor.putInt(album.getId()+"_"+album.getYppx()+"audioId"+i, audio.getId());
            editor.putString(album.getId()+"_"+album.getYppx()+"audioTitle"+i, audio.getTitle());
            editor.putString(album.getId()+"_"+album.getYppx()+"audioDisplayName"+i, audio.getDisplayName());
            editor.putString(album.getId()+"_"+album.getYppx()+"audioNetUrl"+i, audio.getNetUrl());
            editor.putInt(album.getId()+"_"+album.getYppx()+"audioDurationTime"+i, audio.getDurationTime());
            editor.putInt(album.getId()+"_"+album.getYppx()+"audioCurrDurationTime"+i, audio.getCurrDurationTime());
            editor.putInt(album.getId()+"_"+album.getYppx()+"audioSize"+i, audio.getSize());
            editor.putString(album.getId()+"_"+album.getYppx()+"audioFilePath"+i, audio.getFilePath());
            editor.putString(album.getId()+"_"+album.getYppx()+"audioCachePath"+i, audio.getCachePath());
            editor.putBoolean(album.getId()+"_"+album.getYppx()+"audioNet"+i, audio.isNet());
            editor.putBoolean(album.getId()+"_"+album.getYppx()+"audioDownFinish"+i, audio.isDownFinish());
            editor.putBoolean(album.getId()+"_"+album.getYppx()+"audioCacheFinish"+i, audio.isCacheFinish());
            editor.putInt(album.getId()+"_"+album.getYppx()+"audioState"+i, audio.getState());
        }
        editor.commit();
    }

    public static List<Audio> getAudioList(SharedPreferences preferences, Album album){
        List<Audio> list = new ArrayList<>();
        int audioNum = preferences.getInt("audioNum", 0);
        Album album1 = new Album();
        album1.setId(preferences.getInt("albumId", 0));
        album1.setAudioNum(preferences.getInt("albumAudioNum", 0));
        album1.setYppx(preferences.getInt("albumYppx", 0));
        album1.setAudioIdDesc(preferences.getInt("albumAudioIdDesc", 0));
        album1.setAudioNameDesc(preferences.getString("albumAudioNameDesc", ""));
        album1.setAudioIdAsc(preferences.getInt("albumAudioIdAsc", 0));
        album1.setAudioNameAsc(preferences.getString("albumAudioNameAsc", ""));
        if (album1.getYppx() == 0){
        } else {
        }
        album1.setTitle(preferences.getString("albumTitle", ""));
        album1.setImageUrl(preferences.getString("albumImg", ""));

        for (int i = 0; i < audioNum; i++) {
            Audio audio = new Audio();
            audio.setId(preferences.getInt(album.getId()+"_"+album.getYppx()+"audioId"+i, 0));
            audio.setTitle(preferences.getString(album.getId()+"_"+album.getYppx()+"audioTitle"+i, ""));
            audio.setDisplayName(preferences.getString(album.getId()+"_"+album.getYppx()+"audioDisplayName"+i, ""));
            audio.setNetUrl(preferences.getString(album.getId()+"_"+album.getYppx()+"audioNetUrl"+i, ""));
            audio.setDurationTime(preferences.getInt(album.getId()+"_"+album.getYppx()+"audioDurationTime"+i, 0));
            audio.setCurrDurationTime(preferences.getInt(album.getId()+"_"+album.getYppx()+"audioCurrDurationTime"+i, 0));
            audio.setSize(preferences.getInt(album.getId()+"_"+album.getYppx()+"audioSize"+i, 0));
            audio.setFilePath(preferences.getString(album.getId()+"_"+album.getYppx()+"audioFilePath"+i, ""));
            audio.setCachePath(preferences.getString(album.getId()+"_"+album.getYppx()+"audioCachePath"+i, ""));
            audio.setNet(preferences.getBoolean(album.getId()+"_"+album.getYppx()+"audioNet"+i, false));
            audio.setDownFinish(preferences.getBoolean(album.getId()+"_"+album.getYppx()+"audioDownFinish"+i, false));
            audio.setCacheFinish(preferences.getBoolean(album.getId()+"_"+album.getYppx()+"audioCacheFinish"+i, false));
            audio.setState(preferences.getInt(album.getId()+"_"+album.getYppx()+"audioState"+i, 0));
            audio.setAlbum(album1);
            list.add(audio);
        }
        return list;
    }

}
