package com.xm.ib42;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.sunflower.FlowerCollector;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.Tencent;
import com.xm.ib42.adapter.AudioListAdapter;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.entity.Album;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.util.DialogUtils;
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
    private PullToRefreshListView audio_lv_desc, audio_lv_asc;
//    private int albumId;
    private Album album;
    private int page = 1;
    private int pageNum = 20;
    private List<Audio> audioListDesc;
    private List<Audio> audioListAsc;
    private AudioListAdapter adapterDesc, adapterAsc;
    private AudioListAdapter searchAdapter;
    private Intent intent;
    private Dialog loadDialog;
    private EditText audio_search;
    private ImageButton audio_mkf_button;
    private PopupWindow searchPop;
    private PullToRefreshListView home_search_lv;
    private TextView down, desc_asc;

    public SharedPreferences mSharedPreferences;

    private PopupWindow downPop;
    private Button select_all, adtn;

    private ImageView session, timeline, favorite, qq, qzone;
    private PopupWindow sharePop;
    public IWXAPI api;
    public int mTargetScene = SendMessageToWX.Req.WXSceneSession;

    private void initView() {
        title_back = (TextView) findViewById(R.id.title_back);
        title_back.setVisibility(View.VISIBLE);
        audio_lv_desc = (PullToRefreshListView) findViewById(R.id.audio_lv_desc);
        audio_lv_asc = (PullToRefreshListView) findViewById(R.id.audio_lv_asc);
        audio_lv_desc.setVisibility(View.VISIBLE);
        audio_lv_asc.setVisibility(View.GONE);
        desc_asc = (TextView) findViewById(R.id.desc_asc);
        down = (TextView) findViewById(R.id.down);

        audio_search = (EditText) findViewById(R.id.audio_search);
        audio_mkf_button = (ImageButton) findViewById(R.id.audio_mkf_button);

        loadDialog = DialogUtils.createLoadingDialog(this, "加载中...");

        View view = getLayoutInflater().inflate(R.layout.home_search, null);
        home_search_lv = (PullToRefreshListView) view.findViewById(R.id.home_search_lv);
        searchPop = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        searchPop.setContentView(view);
        searchPop.setOutsideTouchable(true);
        searchPop.setBackgroundDrawable(new BitmapDrawable());

        audioListDesc = new ArrayList<>();
        audioListAsc = new ArrayList<>();
        searchList = new ArrayList<>();
        intent = getIntent();
        album = (Album) intent.getSerializableExtra("album");

        mSharedPreferences = getSharedPreferences("audio", Context.MODE_PRIVATE);

        if (album != null) {
            audioListDesc = Utils.getAudioList(mSharedPreferences, album);
            if (audioListDesc.size() <= 0){
                if (!loadDialog.isShowing()){
                    loadDialog.show();
                }
            } else {
                adapterDesc = new AudioListAdapter(AudioListActivity.this, audioListDesc, AudioListActivity.this);
                audio_lv_desc.setAdapter(adapterDesc);
            }
            getdata();
        }
        audio_lv_desc.setMode(PullToRefreshBase.Mode.PULL_FROM_END);//上拉刷新
        audio_lv_desc.setOnRefreshListener(this);
        audio_lv_desc.setOnItemClickListener(this);
        audio_lv_asc.setMode(PullToRefreshBase.Mode.PULL_FROM_END);//上拉刷新
        audio_lv_asc.setOnRefreshListener(this);
        audio_lv_asc.setOnItemClickListener(this);
        audio_search.addTextChangedListener(this);
        title_back.setOnClickListener(this);
        audio_mkf_button.setOnClickListener(this);
        desc_asc.setOnClickListener(this);
        down.setOnClickListener(this);

        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(getApplicationContext(), mInitListener);

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(this, mInitListener);

        itemListener();

        View downView = getLayoutInflater().inflate(R.layout.audiodown, null);
        select_all = (Button) downView.findViewById(R.id.select_all);
        select_all.setOnClickListener(this);
        adtn = (Button) downView.findViewById(R.id.adtn);
        adtn.setOnClickListener(this);
        downPop = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, 200);
        downPop.setContentView(downView);
        downPop.setOutsideTouchable(false);
//        downPop.setBackgroundDrawable(new BitmapDrawable());

        View shareView = getLayoutInflater().inflate(R.layout.sharepop, null);
        session = (ImageView) shareView.findViewById(R.id.session);
        timeline = (ImageView) shareView.findViewById(R.id.timeline);
        favorite = (ImageView) shareView.findViewById(R.id.favorite);
        qq = (ImageView) shareView.findViewById(R.id.qq);
        qzone = (ImageView) shareView.findViewById(R.id.qzone);
        sharePop = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sharePop.setContentView(shareView);
        //        sharePop.setFocusable(false);
        sharePop.setOutsideTouchable(true);
        sharePop.setBackgroundDrawable(new BitmapDrawable());

        session.setOnClickListener(this);
        timeline.setOnClickListener(this);
        favorite.setOnClickListener(this);
        qq.setOnClickListener(this);
        qzone.setOnClickListener(this);

        mTencent = Tencent.createInstance(Constants.QQAPP_ID, this.getApplicationContext());

        api = WXAPIFactory.createWXAPI(getApplicationContext(), Constants.APP_ID);
        api.registerApp(Constants.APP_ID);

    }

    private void itemListener() {
        home_search_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Constants.playAlbum = album;
                Constants.playList.clear();
                if (yppx.equals(Constants.YPPXDESC)){
                    Constants.playList.addAll(audioListDesc);
                } else {
                    Constants.playList.addAll(audioListAsc);
                }
                Audio audio = (Audio) adapterView.getAdapter().getItem(i);
                if (Constants.playAlbum.getYppx() == 0){
                    Constants.playAlbum.setAudioIdDesc(audio.getId());
                    Constants.playAlbum.setAudioNameDesc(audio.getTitle());
                } else {
                    Constants.playAlbum.setAudioIdAsc(audio.getId());
                    Constants.playAlbum.setAudioNameAsc(audio.getTitle());
                }
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
                    if (yppx.equals(Constants.YPPXDESC)){
                        if (page == 1){
                            audioListDesc.clear();
                            audioListDesc.addAll(list);
                        } else {
                            audioListDesc.addAll(list);
                        }
                        if (adapterDesc == null){
                            adapterDesc = new AudioListAdapter(AudioListActivity.this, audioListDesc, AudioListActivity.this);
                            audio_lv_desc.setAdapter(adapterDesc);
                        } else {
                            adapterDesc.notifyDataSetChanged();
                        }
                        audio_lv_desc.setVisibility(View.VISIBLE);
                        audio_lv_asc.setVisibility(View.GONE);
                        Utils.saveAudioList(mSharedPreferences, audioListDesc, album);
                    } else {
                        if (page == 1){
                            audioListAsc.clear();
                            audioListAsc.addAll(list);
                        } else {
                            audioListAsc.addAll(list);
                        }
                        if (adapterAsc == null){
                            adapterAsc = new AudioListAdapter(AudioListActivity.this, audioListDesc, AudioListActivity.this);
                            audio_lv_asc.setAdapter(adapterAsc);
                        } else {
                            adapterAsc.notifyDataSetChanged();
                        }
                        audio_lv_desc.setVisibility(View.GONE);
                        audio_lv_asc.setVisibility(View.VISIBLE);
                        Utils.saveAudioList(mSharedPreferences, audioListAsc, album);
                    }
//                    if (adapter == null){
//                    } else {
//                        adapter.notifyDataSetChanged();
//                    }
                    audio_lv_desc.onRefreshComplete();
                    audio_lv_asc.onRefreshComplete();
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
                        searchAdapter = new AudioListAdapter(AudioListActivity.this, searchList, AudioListActivity.this);
                        home_search_lv.setAdapter(searchAdapter);
                    } else {
                        searchAdapter.notifyDataSetChanged();
                    }
                    if (searchList != null && searchList.size() > 0 && !searchPop.isShowing()){
                        searchPop.showAsDropDown(audio_search, 0, 20);
                    }
                }
            }
        }
    };

    private List<Audio> searchList;
    private String yppx = Constants.YPPXDESC;

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
                list.add(new BasicNameValuePair(Constants.VALUES[6], yppx));
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
        Constants.playList.clear();
        if (yppx.equals(Constants.YPPXDESC)){
            Constants.playAlbum.setYppx(0);
            Constants.playList.addAll(audioListDesc);
        } else {
            Constants.playAlbum.setYppx(1);
            Constants.playList.addAll(audioListAsc);
        }
        Audio audio = (Audio) adapterView.getAdapter().getItem(i);
        if (Constants.playAlbum.getYppx() == 0){
            Constants.playAlbum.setAudioIdDesc(audio.getId());
            Constants.playAlbum.setAudioNameDesc(audio.getTitle());
        } else {
            Constants.playAlbum.setAudioIdAsc(audio.getId());
            Constants.playAlbum.setAudioNameAsc(audio.getTitle());
        }
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
//        if (!loadDialog.isShowing()){
//            loadDialog.show();
//        }
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
        } else if (view == audio_mkf_button){
            setParam();
            audio_search.setText(null);// 清空显示内容
            mIatResults.clear();
            mIatDialog.setListener(mRecognizerDialogListener);
            // 移动数据分析，收集开始听写事件
            FlowerCollector.onEvent(getApplicationContext(), "iat_recognize");
            // 显示听写对话框
            mIatDialog.show();
        } else if (view == desc_asc){
            if (yppx.equals(Constants.YPPXASC)){
                yppx = Constants.YPPXDESC;
                desc_asc.setText("降序");
                down.setText("批量下载");
                if (downPop.isShowing()){
                    downPop.dismiss();
                }
                if (audioListDesc != null && audioListDesc.size() > 0){
                    adapterDesc = new AudioListAdapter(this, audioListDesc, AudioListActivity.this);
                    audio_lv_desc.setAdapter(adapterDesc);
                } else {
                    audioListDesc = Utils.getAudioList(mSharedPreferences, album);
                    adapterDesc = new AudioListAdapter(this, audioListDesc, AudioListActivity.this);
                    audio_lv_desc.setAdapter(adapterDesc);
                    getdata();
                }
                adapterDesc.setDown(false);
                audio_lv_desc.setVisibility(View.VISIBLE);
                audio_lv_asc.setVisibility(View.GONE);
            } else {
                yppx = Constants.YPPXASC;
                desc_asc.setText("升序");
                down.setText("批量下载");
                if (downPop.isShowing()){
                    downPop.dismiss();
                }
                if (audioListAsc != null && audioListAsc.size() > 0){
                    adapterAsc = new AudioListAdapter(this, audioListAsc, AudioListActivity.this);
                    audio_lv_asc.setAdapter(adapterAsc);
                } else {
                    audioListAsc = Utils.getAudioList(mSharedPreferences, album);
                    adapterAsc = new AudioListAdapter(this, audioListAsc, AudioListActivity.this);
                    audio_lv_asc.setAdapter(adapterAsc);
                    getdata();
                }
                adapterAsc.setDown(false);
                audio_lv_desc.setVisibility(View.GONE);
                audio_lv_asc.setVisibility(View.VISIBLE);
            }
        } else if (view == down){
            if (yppx.equals(Constants.YPPXDESC)){
                if (adapterDesc != null){
                    if (adapterDesc.isDown()){
                        adapterDesc.setDown(false);
                        down.setText("批量下载");
                        if (downPop.isShowing()){
                            downPop.dismiss();
                        }
                        for (int i = 0; i < audioListDesc.size(); i++) {
                            audioListDesc.get(i).setCheck(false);
                        }
                        if (yppx.equals(Constants.YPPXDESC)){
                        } else {
                            for (int i = 0; i < audioListAsc.size(); i++) {
                                audioListAsc.get(i).setCheck(false);
                            }
                        }
                    } else {
                        adapterDesc.setDown(true);
                        down.setText("取消");
                        downPop.showAtLocation(getLayoutInflater().inflate(R.layout.audiolist, null), Gravity.BOTTOM, 0, 0);
                    }
                    adapterDesc.notifyDataSetChanged();

                }
            } else {
                if (adapterAsc != null){
                    if (adapterAsc.isDown()){
                        adapterAsc.setDown(false);
                        down.setText("批量下载");
                        if (downPop.isShowing()){
                            downPop.dismiss();
                        }
                        for (int i = 0; i < audioListAsc.size(); i++) {
                            audioListAsc.get(i).setCheck(false);
                        }
                    } else {
                        adapterAsc.setDown(true);
                        down.setText("取消");
                        downPop.showAtLocation(getLayoutInflater().inflate(R.layout.audiolist, null), Gravity.BOTTOM, 0, 0);
                    }
                    adapterAsc.notifyDataSetChanged();
                }
            }
        } else if (view == adtn){
            List<Audio> list = new ArrayList<>();
            if (yppx.equals(Constants.YPPXDESC)){
                for (int i = 0; i < audioListDesc.size(); i++) {
                    Audio audio = audioListDesc.get(i);
                    if (audio.isCheck()){
                        list.add(audio);
                        audio.setCheck(false);
                    }
                }
                adapterDesc.setDown(false);
                adapterDesc.notifyDataSetChanged();
            } else {
                for (int i = 0; i < audioListAsc.size(); i++) {
                    Audio audio = audioListAsc.get(i);
                    if (audio.isCheck()){
                        list.add(audio);
                        audio.setCheck(false);
                    }
                }
                adapterAsc.setDown(false);
                adapterAsc.notifyDataSetChanged();
            }
            if (list.size() > 0){
                MainActivity.downLoadManager.add(list);
                if (downPop.isShowing()){
                    downPop.dismiss();
                }
                down.setText("下载");
           } else {
                Utils.showToast(AudioListActivity.this, "请选择要下载的音频");
            }
        } else if (view == session){
            mTargetScene = SendMessageToWX.Req.WXSceneSession;
            shareApp();
        } else if (view == timeline){
            mTargetScene = SendMessageToWX.Req.WXSceneTimeline;
            shareApp();
        } else if (view == favorite){
            mTargetScene = SendMessageToWX.Req.WXSceneFavorite;
            shareApp();
        } else if (view == qq){
            shareApp2QQ();
        } else if (view == qzone){
            shareApp2QQ();
        } else if (view == select_all){
            if (yppx.equals(Constants.YPPXDESC)){
                for (int i = 0; i < audioListDesc.size(); i++) {
                    Audio audio = audioListDesc.get(i);
                    audio.setCheck(true);
                }
                adapterDesc.setDown(false);
                adapterDesc.notifyDataSetChanged();
            } else {
                for (int i = 0; i < audioListAsc.size(); i++) {
                    Audio audio = audioListAsc.get(i);
                    audio.setCheck(true);
                }
                adapterAsc.setDown(false);
                adapterAsc.notifyDataSetChanged();
            }
        }
    }
    private  Audio mAudio;
    public Tencent mTencent;
    private void shareApp2QQ(){
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, "印心讲堂");
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  "");
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  Constants.SHAREURL+mAudio.getId());
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, "");
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  "印心讲堂");
        mTencent.shareToQQ(AudioListActivity.this, params, new MainActivity.BaseUiListener());
    }

    private static final int THUMB_SIZE = 150;
    private void shareApp(){
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = Constants.SHAREURL+mAudio.getId();
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = "欢迎使用「印心讲堂」";
        msg.description = "";
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
        bmp.recycle();
        msg.thumbData = Utils.bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = mTargetScene;
        api.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public void showPop(Audio audio){
        mAudio = audio;
        if (!sharePop.isShowing()){
            sharePop.showAtLocation(getLayoutInflater().inflate(R.layout.audiolist, null), Gravity.BOTTOM, 0, 0);
        }
    }

    // 语音听写对象
    public SpeechRecognizer mIat;
    // 语音听写UI
    public RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d("Tag", "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
            }
        }
    };


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
                Utils.showToast(AudioListActivity.this, "识别失败,请确认是否已开通翻译功能");
            } else {
                Utils.showToast(AudioListActivity.this, "识别失败");
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
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        mIat.setParameter( SpeechConstant.ASR_SCH, "1" );
        mIat.setParameter( SpeechConstant.ADD_CAP, "translate" );
        mIat.setParameter( SpeechConstant.TRS_SRC, "its" );
        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "en_us");
        mIat.setParameter( SpeechConstant.ORI_LANG, "cn" );
        mIat.setParameter( SpeechConstant.TRANS_LANG, "en" );


        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }

    private void printTransResult (RecognizerResult results) {
        String trans  = JsonParser.parseTransResult(results.getResultString(),"dst");
        String oris = JsonParser.parseTransResult(results.getResultString(),"src");

        if( TextUtils.isEmpty(trans)||TextUtils.isEmpty(oris) ){
            Utils.showToast(this, "解析结果失败，请确认是否已开通翻译功能。");
        }else{
            audio_search.setText(oris);
        }

    }
    
}
