package com.example.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String data = null;
//                try {
//                    data = getData("http://app.webfuwu.com/up.asp?appid=1696");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                Log.d("Tag", data);
//            }
//        }).start();

        String data = "{{v:2},{mess:更新说明：当前版本V2.0 优化已知BUG},{urls:/uploadfile/image/20170710/20170710182813541354.apk}}";

        try {
            JSONObject json = new JSONObject(data);
            Log.d("Tag", json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    OkHttpClient client = new OkHttpClient();

    String getData(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }


}
