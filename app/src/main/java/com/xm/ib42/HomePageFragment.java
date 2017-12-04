package com.xm.ib42;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.sunflower.FlowerCollector;
import com.xm.ib42.adapter.HomeAdapter;
import com.xm.ib42.adapter.HomeSearchAdapter;
import com.xm.ib42.app.MyApplication;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.entity.Album;
import com.xm.ib42.entity.Column;
import com.xm.ib42.util.HttpHelper;
import com.xm.ib42.util.JsonParser;
import com.xm.ib42.util.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * home1
 * 
 * @author andye
 * 
 */
public class HomePageFragment extends Fragment implements OnClickListener,
        ExpandableListView.OnChildClickListener, AdapterView.OnItemClickListener, TextWatcher,
        PullToRefreshBase.OnRefreshListener {

    private MainActivity aty;
    private View convertView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        convertView = inflater.inflate(R.layout.home_page, null);
		return convertView;
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        aty = (MainActivity) getActivity();
        init(convertView);
    }

    private EditText home_search;
    private TextView title_name;
    private ExpandableListView home_lv;
    private LinearLayout home_play;
    private TextView home_play_name;
    private PullToRefreshListView home_search_lv;
    private PopupWindow searchPop;
    private ImageButton mkf_button;

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private int searchPage = 1;

	private void init(View v) {
        home_search = (EditText) v.findViewById(R.id.home_search);
        title_name = (TextView) v.findViewById(R.id.title_name);
        home_lv = (ExpandableListView) v.findViewById(R.id.home_lv);
        home_play = (LinearLayout) v.findViewById(R.id.home_play);
        home_play_name = (TextView) v.findViewById(R.id.home_play_name);
        mkf_button = (ImageButton) convertView.findViewById(R.id.mkf_button);

        View view = aty.getLayoutInflater().inflate(R.layout.home_search, null);
        home_search_lv = (PullToRefreshListView) view.findViewById(R.id.home_search_lv);
        searchPop = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        searchPop.setContentView(view);

        click();
        if (aty.homeList != null && aty.homeList.size() > 0){
            if (adapter == null){
                adapter = new HomeAdapter(aty, aty.homeList);
                home_lv.setAdapter(adapter);

            } else {
                adapter.notifyDataSetChanged();
            }
            int groupCount = home_lv.getCount();
            for (int i=0; i<groupCount; i++) {
                home_lv.expandGroup(i);
            }

        } else {
            aty.showLoadDialog(true);
            getColumnData();
        }
        //
        if (!MyApplication.mediaPlayer.isPlaying() && aty.albumId != -1 && aty.isShow){
            Album album = aty.albumDao.searchById(aty.albumId);
            if (album != null){
                home_play.setVisibility(View.VISIBLE);
                home_play_name.setText(album.getTitle());
            } else {
                home_play.setVisibility(View.GONE);
            }
        } else {
            home_play.setVisibility(View.GONE);
        }
//        if (aty.setting.getValue(SystemSetting.KEY_PLAYER_ALBUMID) != null){
//            int audioId = Integer.parseInt(aty.setting.getValue(SystemSetting.KEY_PLAYER_AUDIOID));
//            if (aty.mediaPlayerManager.getPlayerState() != MediaPlayerManager.STATE_PLAYER){
//                for (int i = 0; i < Constants.playList.size(); i++) {
//                    if (audioId == Constants.playList.get(i).getId()){
//                        home_play_name.setText(Constants.playList.get(i).getTitle());
//                    }
//                }
//            } else {
//                home_play.setVisibility(View.GONE);
//            }
//        } else {
//            home_play.setVisibility(View.GONE);
//        }

        setParam();
    }

    private JSONObject json;
    private List<Album> searchList;

    private void getColumnData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpHelper httpHelper = new HttpHelper();
                httpHelper.connect();
                List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
                list.add(new BasicNameValuePair(Constants.VALUES[0], "2"));
                HttpResponse httpResponse = httpHelper.doGet(Constants.HTTPURL, list);
                json = Utils.parseResponse(httpResponse);
                aty.homeList = Utils.pressColumnJson(json);
                if (aty.homeList != null){
                    for (int i = 0; i < aty.homeList.size(); i++) {
                        Column column = aty.homeList.get(i);
                        list = new ArrayList<BasicNameValuePair>();
                        list.add(new BasicNameValuePair(Constants.VALUES[0], "1"));
                        list.add(new BasicNameValuePair(Constants.VALUES[2], column.getPage()+""));
                        list.add(new BasicNameValuePair(Constants.VALUES[4], column.getId()+""));
                        list.add(new BasicNameValuePair(Constants.VALUES[5], "3"));
                        HttpResponse albumResponse = httpHelper.doGet(Constants.HTTPURL, list);
                        JSONObject j = Utils.parseResponse(albumResponse);
                        column.setCount(j.optInt("count_total"));
                        column.setAlbumList(Utils.pressAlbumJson(j));
                    }
                }
                handler.sendMessage(handler.obtainMessage(0));
            }
        }).start();
    }

    private HomeAdapter adapter;
    private HomeSearchAdapter searchAdapter;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0){
                aty.showLoadDialog(false);
                if (aty.homeList != null){
                    if (adapter == null){
                        adapter = new HomeAdapter(aty, aty.homeList);
                        home_lv.setAdapter(adapter);
                        int groupCount = home_lv.getCount();
                        for (int i=0; i<groupCount; i++) {
                            home_lv.expandGroup(i);
                        }
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                }
            } else if (msg.what == 2){
                aty.showLoadDialog(false);
                List<Album> list = (List<Album>) msg.obj;
                if (list != null){
                    if (searchList == null){
                        searchList = new ArrayList<>();
                    }
                    searchList.addAll(list);
                    if (searchAdapter == null){
                        searchAdapter = new HomeSearchAdapter(aty, searchList);
                        home_search_lv.setAdapter(searchAdapter);
                        if (!searchPop.isShowing()){
                            searchPop.showAsDropDown(home_search, 0, 20);
                        }
                    } else {
                        searchAdapter.notifyDataSetChanged();
                    }
                }
            } else if (msg.what == 3){
                if (adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void click() {
        home_play.setOnClickListener(this);
//        home_lv.setOnItemClickListener(this);
        home_lv.setOnChildClickListener(this);
        home_search_lv.setOnItemClickListener(this);
        home_search_lv.setOnRefreshListener(this);
        home_search.addTextChangedListener(this);
        mkf_button.setOnClickListener(this);

    }

    @Override
	public void onClick(View v) {
		if (v == home_play){
//            Fragment f = new PlayPageFragment();
//            FragmentTransaction fTransaction = getFragmentManager().beginTransaction();
//            fTransaction.replace(R.id.content_container, f, "PlayPageFragment");
//            fTransaction.commit();
//            aty.updateButtonLayout(1);
            Album album = aty.albumDao.searchById(aty.albumId);
            Constants.playPage = 0;
            Constants.playAlbum = album;
            Constants.playList.clear();
            Intent intent = new Intent(Constants.ACTION_JUMR_MYPAGE);
            intent.putExtra("title", album.getAudioName());
            aty.sendBroadcast(intent);
            aty.changePlay();
        } else if (v == mkf_button){
            home_search.setText(null);// 清空显示内容
            mIatResults.clear();
            aty.mIatDialog.setListener(mRecognizerDialogListener);
            Intent intent = new Intent("startIat");
            aty.sendBroadcast(intent);
        }
	}


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Album album = (Album) parent.getAdapter().getItem(position);
        if (album != null){
            Intent intent = new Intent(aty, AudioListActivity.class);
            intent.putExtra("album", album);
            aty.startActivityForResult(intent, 0);
        }
    }

    private String searchName = "";

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!searchName.equals(s.toString())){
            searchName = s.toString();
            getSearchData();
        }
    }

    private void getSearchData() {
        aty.showLoadDialog(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpHelper httpHelper = new HttpHelper();
                httpHelper.connect();
                List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
                list.add(new BasicNameValuePair(Constants.VALUES[0], "1"));
                list.add(new BasicNameValuePair(Constants.VALUES[2], searchPage+""));
                list.add(new BasicNameValuePair(Constants.VALUES[3], searchName));
                HttpResponse httpResponse = httpHelper.doGet(Constants.HTTPURL, list);
                json = Utils.parseResponse(httpResponse);
                List<Album> l = Utils.pressAlbumJson(json);
                handler.sendMessage(handler.obtainMessage(2, l));
            }
        }).start();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onStart() {
        super.onStart();
        // 开放统计 移动数据统计分析
        FlowerCollector.onResume(aty);
        FlowerCollector.onPageStart("Tag");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (searchPop.isShowing()){
            searchPop.dismiss();
        }
    }


    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printTransResult( results );
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            if(error.getErrorCode() == 14002) {
                Utils.showToast(aty, "识别失败,请确认是否已开通翻译功能");
            } else {
                Utils.showToast(aty, "识别失败");
            }
        }

    };

    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        aty.mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        aty.mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        aty.mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        aty.mIat.setParameter( SpeechConstant.ASR_SCH, "1" );
        aty.mIat.setParameter( SpeechConstant.ADD_CAP, "translate" );
        aty.mIat.setParameter( SpeechConstant.TRS_SRC, "its" );
        // 设置语言
        aty.mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        aty.mIat.setParameter(SpeechConstant.ACCENT, null);
        aty.mIat.setParameter( SpeechConstant.ORI_LANG, "en" );
        aty.mIat.setParameter( SpeechConstant.TRANS_LANG, "cn" );


        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        aty.mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        aty.mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        aty.mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        aty.mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        aty.mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }

    private void printTransResult (RecognizerResult results) {
        String trans  = JsonParser.parseTransResult(results.getResultString(),"dst");
        String oris = JsonParser.parseTransResult(results.getResultString(),"src");

        if( TextUtils.isEmpty(trans)||TextUtils.isEmpty(oris) ){
            Utils.showToast(aty, "解析结果失败，请确认是否已开通翻译功能。");
        }else{
            home_search.setText( "原始语言:\n"+oris+"\n目标语言:\n"+trans );
        }

    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        final Column column = (Column) expandableListView.getExpandableListAdapter().getGroup(i);
        if (i1 == expandableListView.getExpandableListAdapter().getChildrenCount(i)-1 && column != null){
            adapter.setAdd(i);
            column.setPage(column.getPage()+1);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpHelper httpHelper = new HttpHelper();
                    httpHelper.connect();
                    List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
                    list.add(new BasicNameValuePair(Constants.VALUES[0], "1"));
                    list.add(new BasicNameValuePair(Constants.VALUES[2], column.getPage()+""));
                    list.add(new BasicNameValuePair(Constants.VALUES[4], column.getId()+""));
                    list.add(new BasicNameValuePair(Constants.VALUES[5], "3"));
                    HttpResponse albumResponse = httpHelper.doGet(Constants.HTTPURL, list);
                    List<Album> albumList = column.getAlbumList();
                    List<Album> albums = Utils.pressAlbumJson(Utils.parseResponse(albumResponse));
                    if (albums != null && albums.size() > 0){
                        albumList.addAll(albums);
                        column.setAlbumList(albumList);
                        adapter.getAdd();
                        handler.sendMessage(handler.obtainMessage(3));
//                        adapter.notifyDataSetChanged();
                    } else {
                        column.setPage(column.getPage()-1);
                    }
                }
            }).start();

        } else {
            Album album = (Album) expandableListView.getExpandableListAdapter().getChild(i, i1);
            if (album != null) {
                Intent intent = new Intent(aty, AudioListActivity.class);
                intent.putExtra("album", album);
                aty.startActivityForResult(intent, 0);
            }
        }
        return false;
    }

    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
        searchPage++;
        getSearchData();
    }
}
