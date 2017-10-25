package com.xm.ib42;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.xm.ib42.adapter.AudioListAdapter;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.entity.Album;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.util.DialogUtils;
import com.xm.ib42.util.HttpHelper;
import com.xm.ib42.util.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by long on 17-10-24.
 */

public class AudioListActivity extends Activity implements AdapterView.OnItemClickListener,
        PullToRefreshBase.OnRefreshListener{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audiolist);
        initView();
    }

    private TextView title_name;
    private PullToRefreshListView audio_lv;
//    private int albumId;
    private Album album;
    private int page = 1;
    private int pageNum = 20;
    private List<Audio> audioList;
    private AudioListAdapter adapter;
    private Intent intent;
    private Dialog loadDialog;

    private void initView() {
        title_name = (TextView) findViewById(R.id.title_name);
        audio_lv = (PullToRefreshListView) findViewById(R.id.audio_lv);

        loadDialog = DialogUtils.createLoadingDialog(this, "加载中...");

        audioList = new ArrayList<>();
        intent = getIntent();
        album = (Album) intent.getSerializableExtra("album");
        if (album != null) {
            if (!loadDialog.isShowing()){
                loadDialog.show();
            }
            getdata();
        }
        audio_lv.setMode(PullToRefreshBase.Mode.PULL_FROM_END);//上拉刷新
        audio_lv.setOnRefreshListener(this);
        audio_lv.setOnItemClickListener(this);
    }

    Handler hander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0){
                if (loadDialog.isShowing()){
                    DialogUtils.closeDialog(loadDialog);
                }
                List<Audio> list = (List<Audio>) msg.obj;
                if (list != null){
                    if (page == 0){
                        audioList.clear();
                        audioList.addAll(list);
                    } else {
                        audioList.addAll(list);
                    }
                    if (adapter == null){
                        adapter = new AudioListAdapter(AudioListActivity.this, audioList);
                        audio_lv.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    audio_lv.onRefreshComplete();
                }
            }
        }
    };

    private void getdata() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpHelper httpHelper = new HttpHelper();
                httpHelper.connect();
                List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
                list.add(new BasicNameValuePair(Constants.VALUES[0], "1"));
                list.add(new BasicNameValuePair(Constants.VALUES[1], album.getId()+""));
                list.add(new BasicNameValuePair(Constants.VALUES[2], page+""));
                list.add(new BasicNameValuePair(Constants.VALUES[5], pageNum+""));
                HttpResponse httpResponse = httpHelper.doGet(Constants.HTTPURL, list);
                JSONObject json = Utils.parseResponse(httpResponse);
                List<Audio> audioList = Utils.pressAudioJson(json, album);
                hander.sendMessage(hander.obtainMessage(0, audioList));
            }
        }).start();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Constants.playAlbum = album;
        Constants.playList.addAll(audioList);
        Audio audio = (Audio) adapterView.getAdapter().getItem(i);
        if (audio != null) {
            intent.putExtra("audio", audio);
            setResult(0, intent);
            finish();
        }
    }

    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
        page++;
        getdata();
    }
}
