package com.xm.ib42;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
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
        PullToRefreshBase.OnRefreshListener, TextWatcher, View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audiolist);
        initView();
    }

    private TextView title_back;
    private PullToRefreshListView audio_lv;
//    private int albumId;
    private Album album;
    private int page = 1;
    private int pageNum = 20;
    private List<Audio> audioList;
    private AudioListAdapter adapter;
    private AudioListAdapter searchAdapter;
    private Intent intent;
    private Dialog loadDialog;
    private EditText audio_search;
    private ImageButton audio_mkf_button;
    private PopupWindow searchPop;
    private PullToRefreshListView home_search_lv;

    private void initView() {
        title_back = (TextView) findViewById(R.id.title_back);
        title_back.setVisibility(View.VISIBLE);
        audio_lv = (PullToRefreshListView) findViewById(R.id.audio_lv);

        audio_search = (EditText) findViewById(R.id.audio_search);
        audio_mkf_button = (ImageButton) findViewById(R.id.audio_mkf_button);

        loadDialog = DialogUtils.createLoadingDialog(this, "加载中...");

        View view = getLayoutInflater().inflate(R.layout.home_search, null);
        home_search_lv = (PullToRefreshListView) view.findViewById(R.id.home_search_lv);
        searchPop = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        searchPop.setContentView(view);

        audioList = new ArrayList<>();
        searchList = new ArrayList<>();
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
        audio_search.addTextChangedListener(this);
        title_back.setOnClickListener(this);

        itemListener();
    }

    private void itemListener() {
        home_search_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Constants.playAlbum = album;
                Constants.playList.clear();
                Constants.playList.addAll(audioList);
                Audio audio = (Audio) adapterView.getAdapter().getItem(i);
                Constants.playAlbum.setAudioId(audio.getId());
                Constants.playAlbum.setAudioName(audio.getTitle());
                Constants.playPage = Constants.playList.size() / 10;
                intent.putExtra("title", audio.getTitle());
                setResult(1, intent);
                finish();
            }
        });
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
                    if (page == 1){
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
            } else if (msg.what == 2){
                if (loadDialog.isShowing()){
                    DialogUtils.closeDialog(loadDialog);
                }
                List<Audio> list = (List<Audio>) msg.obj;
                if (list != null){
                    if (page == 1){
                        searchList.clear();
                        searchList.addAll(list);
                    } else {
                        searchList.addAll(list);
                    }
                    if (searchAdapter == null){
                        searchAdapter = new AudioListAdapter(AudioListActivity.this, searchList);
                        home_search_lv.setAdapter(searchAdapter);
                        if (!searchPop.isShowing()){
                            searchPop.showAsDropDown(audio_search, 0, 20);
                        }
                    } else {
                        searchAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    private List<Audio> searchList;

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
//        if (Constants.playAlbum != null){
//            if (Constants.playAlbum.getId() != album.getId()){
//                Constants.playAlbum = album;
//            }
//        } else {
//        }
        Constants.playAlbum = album;
        Constants.playList.clear();
        Constants.playList.addAll(audioList);
        Audio audio = (Audio) adapterView.getAdapter().getItem(i);
        Constants.playAlbum.setAudioId(audio.getId());
        Constants.playAlbum.setAudioName(audio.getTitle());
        Constants.playPage = Constants.playList.size() / 10;
        intent.putExtra("position", i-1);
        setResult(0, intent);
        finish();
    }

    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
        page++;
        getdata();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    private String searchName = "";
    private int searchPage = 1;

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (Utils.isBlank(charSequence.toString()))return;
        if (!searchName.equals(charSequence.toString())){
            searchName = charSequence.toString();
            getSearchData();
        }
    }

    private void getSearchData() {
        if (!loadDialog.isShowing()){
            loadDialog.show();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpHelper httpHelper = new HttpHelper();
                httpHelper.connect();
                List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
                list.add(new BasicNameValuePair(Constants.VALUES[0], "1"));
                list.add(new BasicNameValuePair(Constants.VALUES[1], album.getId()+""));
//                list.add(new BasicNameValuePair(Constants.VALUES[2], searchPage+""));
                list.add(new BasicNameValuePair(Constants.VALUES[3], searchName));
                HttpResponse httpResponse = httpHelper.doGet(Constants.HTTPURL, list);
                JSONObject json = Utils.parseResponse(httpResponse);
                List<Audio> l = Utils.pressAudioJson(json, album);
                hander.sendMessage(hander.obtainMessage(2, l));
            }
        }).start();
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onClick(View view) {
        if (view == title_back){
            finish();
        }
    }
}
