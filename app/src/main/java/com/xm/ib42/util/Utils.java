package com.xm.ib42.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.xm.ib42.constant.Constants;
import com.xm.ib42.entity.Album;
import com.xm.ib42.entity.Audio;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
        // Toast.makeText(context, str, Toast.LENGTH_SHORT).show();;
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

    public static boolean isUpdate(JSONObject jsonObject, Context context){
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
            int version = packInfo.versionCode;
//            jsonObject.optJSONObject()
            return true;
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
        data = data.replaceAll("\\{", "");
        data = data.replaceAll("\\}", "");
        String d[] = data.split(",");
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < d.length; i++) {
            map.put(d[i].split(":")[0], d[i].split(":")[1]);
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

}
