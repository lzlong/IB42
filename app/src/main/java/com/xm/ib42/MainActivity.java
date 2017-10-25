package com.xm.ib42;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.sunflower.FlowerCollector;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.dao.AlbumDao;
import com.xm.ib42.dao.AudioDao;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.entity.Column;
import com.xm.ib42.service.DownLoadManager;
import com.xm.ib42.service.MediaPlayerManager;
import com.xm.ib42.util.DialogUtils;
import com.xm.ib42.util.HttpHelper;
import com.xm.ib42.util.SystemSetting;
import com.xm.ib42.util.VersionUpdateDialog;

import org.apache.http.HttpResponse;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity implements View.OnClickListener{

    private LinearLayout mBt1, mBt2, mBt3, mBt4;
    private LinearLayout mTab_item_container;
    private FragmentManager mFM = null;

    LinearLayout content_container;
    public MediaPlayerManager mediaPlayerManager;
    Intent m_Intent;

    public AlbumDao albumDao;
    public AudioDao audioDao;

//    public List<Album> homeList;
    public List<Column> homeList;

    private Dialog loadDialog;
    public SystemSetting setting;

    // 语音听写对象
    public SpeechRecognizer mIat;
    // 语音听写UI
    public RecognizerDialog mIatDialog;

    public IWXAPI api;
    public int mTargetScene = SendMessageToWX.Req.WXSceneSession;

    public int duration = 0;//
    public int currentDuration = 0;// 已经播放时长
    public String playName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        init();

        changeHome();

        updateVersion();

        getService();
    }

    private void updateVersion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpHelper httpHelper = new HttpHelper();
                httpHelper.connect();
                HttpResponse httpResponse = httpHelper.doGet(Constants.UPDATEVERURL);


            }
        }).start();
    }

    private Map<String, String> updateMap;

    Handler updateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1){
                VersionUpdateDialog versionUpdateDlg = new VersionUpdateDialog(MainActivity.this, updateMap.get(""),
                        updateMap.get(""), new VersionUpdateDialog.DialogListener() {
                    @Override
                    public void onSure() {
                    }

                    @Override
                    public void onCancel() {
                        // 强制升级
                    }
                });
                versionUpdateDlg.show();
            }
        }
    };

    private void init() {
        mTab_item_container = (LinearLayout) findViewById(R.id.tab_item_container);

        mBt1 = (LinearLayout) findViewById(R.id.tab_bt_1);
        mBt2 = (LinearLayout) findViewById(R.id.tab_bt_2);
        mBt3 = (LinearLayout) findViewById(R.id.tab_bt_3);
        mBt4 = (LinearLayout) findViewById(R.id.tab_bt_4);

        loadDialog = DialogUtils.createLoadingDialog(this, "加载中...");

        mBt1.setOnClickListener(this);
        mBt2.setOnClickListener(this);
        mBt3.setOnClickListener(this);
        mBt4.setOnClickListener(this);

        albumDao = new AlbumDao(this);
        audioDao = new AudioDao(this);

        homeList = new ArrayList<>();

        content_container = (LinearLayout) findViewById(R.id.content_container);

        setting = new SystemSetting(this, true);
        if (setting.getValue(SystemSetting.KEY_PLAYER_ALBUMID) != null){
            int albumId = Integer.parseInt(setting.getValue(SystemSetting.KEY_PLAYER_ALBUMID));
            Constants.playAlbum = albumDao.searchById(albumId);
//            Constants.playList = audioDao.searchByAlbum(albumId+"");
        }

        api = WXAPIFactory.createWXAPI(getApplicationContext(), Constants.APP_ID);
        api.registerApp(Constants.APP_ID);

        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(getApplicationContext(), mInitListener);

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(this, mInitListener);

    }


    private int mSelectIndex = 0;

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.tab_bt_1:
                changeHome();
                break;
            case R.id.tab_bt_2:
                changePlay();
                break;
            case R.id.tab_bt_3:
                changeMyPage();
                break;
            case R.id.tab_bt_4:
                changeDown();
                break;
            default:
                break;
        }
    }


    int select = 0xFF1296DB;
    int noSeclect = 0xFF000000;

    public void updateButtonLayout(int tab){
        switch (tab){
            case 0:
                ((ImageView)mBt1.getChildAt(0)).setImageResource(R.mipmap.home_select);
                ((TextView)mBt1.getChildAt(1)).setTextColor(select);
                ((ImageView)mBt2.getChildAt(0)).setImageResource(R.mipmap.play_no);
                ((TextView)mBt2.getChildAt(1)).setTextColor(noSeclect);
                ((ImageView)mBt3.getChildAt(0)).setImageResource(R.mipmap.my_no);
                ((TextView)mBt3.getChildAt(1)).setTextColor(noSeclect);
                ((ImageView)mBt4.getChildAt(0)).setImageResource(R.mipmap.down_no);
                ((TextView)mBt4.getChildAt(1)).setTextColor(noSeclect);
                break;
            case 1:
                ((ImageView)mBt1.getChildAt(0)).setImageResource(R.mipmap.home_no);
                ((TextView)mBt1.getChildAt(1)).setTextColor(noSeclect);
                ((ImageView)mBt2.getChildAt(0)).setImageResource(R.mipmap.play_select);
                ((TextView)mBt2.getChildAt(1)).setTextColor(select);
                ((ImageView)mBt3.getChildAt(0)).setImageResource(R.mipmap.my_no);
                ((TextView)mBt3.getChildAt(1)).setTextColor(noSeclect);
                ((ImageView)mBt4.getChildAt(0)).setImageResource(R.mipmap.down_no);
                ((TextView)mBt4.getChildAt(1)).setTextColor(noSeclect);
                break;
            case 2:
                ((ImageView)mBt1.getChildAt(0)).setImageResource(R.mipmap.home_no);
                ((TextView)mBt1.getChildAt(1)).setTextColor(noSeclect);
                ((ImageView)mBt2.getChildAt(0)).setImageResource(R.mipmap.play_no);
                ((TextView)mBt2.getChildAt(1)).setTextColor(noSeclect);
                ((ImageView)mBt3.getChildAt(0)).setImageResource(R.mipmap.my_select);
                ((TextView)mBt3.getChildAt(1)).setTextColor(select);
                ((ImageView)mBt4.getChildAt(0)).setImageResource(R.mipmap.down_no);
                ((TextView)mBt4.getChildAt(1)).setTextColor(noSeclect);
                break;
            case 3:
                ((ImageView)mBt1.getChildAt(0)).setImageResource(R.mipmap.home_no);
                ((TextView)mBt1.getChildAt(1)).setTextColor(noSeclect);
                ((ImageView)mBt2.getChildAt(0)).setImageResource(R.mipmap.play_no);
                ((TextView)mBt2.getChildAt(1)).setTextColor(noSeclect);
                ((ImageView)mBt3.getChildAt(0)).setImageResource(R.mipmap.my_no);
                ((TextView)mBt3.getChildAt(1)).setTextColor(noSeclect);
                ((ImageView)mBt4.getChildAt(0)).setImageResource(R.mipmap.down_select);
                ((TextView)mBt4.getChildAt(1)).setTextColor(select);
                break;
        }
    }

    /**
     *
     */
    private void changeHome() {
        Fragment f = new HomePageFragment();
        if (null == mFM)
            mFM = getSupportFragmentManager();
        FragmentTransaction ft = mFM.beginTransaction();
        ft.replace(R.id.content_container, f);
        ft.commit();
        mSelectIndex = 0;
        updateButtonLayout(mSelectIndex);
    }

    /**
     *
     */
    public void changePlay() {
        Fragment f = new PlayPageFragment();
        if (null == mFM)
            mFM = getSupportFragmentManager();
        FragmentTransaction ft = mFM.beginTransaction();
        ft.replace(R.id.content_container, f);
        ft.commit();
        mSelectIndex = 1;
        updateButtonLayout(mSelectIndex);
    }

    /**
     *
     */
    public void changeMyPage() {
        Fragment f = new MyPageFragment();
        if (null == mFM)
            mFM = getSupportFragmentManager();
        FragmentTransaction ft = mFM.beginTransaction();
        ft.replace(R.id.content_container, f);
        ft.commit();
        mSelectIndex = 2;
        updateButtonLayout(mSelectIndex);
    }

    /**
     *
     */
    public void changeDown() {
        Fragment f = new DownPageFragment();
        if (null == mFM)
            mFM = getSupportFragmentManager();
        FragmentTransaction ft = mFM.beginTransaction();
        ft.replace(R.id.content_container, f);
        ft.commit();
        mSelectIndex = 3;
        updateButtonLayout(mSelectIndex);
    }

    private static Boolean isQuit = false;
    private Timer timer = new Timer();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isQuit == false) {
                isQuit = true;
                Toast.makeText(getBaseContext(), "再按一次返回键退出", Toast.LENGTH_SHORT).show();
                TimerTask task = null;
                task = new TimerTask() {
                    @Override
                    public void run() {
                        isQuit = false;
                    }
                };
                timer.schedule(task, 2000);
            } else {
                finish();
            }
        } else {
        }
        return false;
    }

    public DownLoadManager downLoadManager;
    private DownLoadBroadcastRecevier downLoadBroadcastRecevier;
    private IatBroadcast iatBroadcast;

    private void getService(){
        //播放器管理
        if (mediaPlayerManager == null){
            mediaPlayerManager=new MediaPlayerManager(this);
        }
        mediaPlayerManager.setConnectionListener(mConnectionListener);
        mediaPlayerManager.startAndBindService();

        //注册播放器-广播接收器
        //        mediaPlayerBroadcastReceiver=new MediaPlayerBroadcastReceiver();
        //        registerReceiver(mediaPlayerBroadcastReceiver, new IntentFilter(MediaPlayerManager.BROADCASTRECEVIER_ACTON));
        //注册下载任务-广播接收器
        downLoadBroadcastRecevier=new DownLoadBroadcastRecevier();
        registerReceiver(downLoadBroadcastRecevier, new IntentFilter(DownLoadManager.BROADCASTRECEVIER_ACTON));

        downLoadManager=new DownLoadManager(this);
        downLoadManager.startAndBindService();

        iatBroadcast = new IatBroadcast();
        registerReceiver(iatBroadcast, new IntentFilter("startIat"));
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    private MediaPlayerManager.ServiceConnectionListener mConnectionListener=new MediaPlayerManager.ServiceConnectionListener() {
        @Override
        public void onServiceDisconnected() {
        }
        @Override
        public void onServiceConnected() {

        }
    };

    /**
     * 下载任务-广播接收器
     * */
    private class DownLoadBroadcastRecevier extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            int flag=intent.getIntExtra("flag", -1);
            if(flag==DownLoadManager.FLAG_CHANGED){
            }else if(flag==DownLoadManager.FLAG_WAIT){
            }else if(flag==DownLoadManager.FLAG_COMPLETED){
            }else if(flag==DownLoadManager.FLAG_FAILED){
            }else if(flag==DownLoadManager.FLAG_TIMEOUT){
            }else if(flag==DownLoadManager.FLAG_ERROR){
            }else if(flag==DownLoadManager.FLAG_COMMON){
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downLoadManager.unbindService();
        EventBus.getDefault().unregister(this);
        if( null != mIat ){
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
        if (mediaPlayerManager!=null){
//            unregisterReceiver(mediaPlayerBroadcastReceiver);
            unregisterReceiver(downLoadBroadcastRecevier);
            mediaPlayerManager.unbindService();
            //mediaPlayerManager = null;
            unregisterReceiver(iatBroadcast);
        }
    }

    public void showLoadDialog(boolean isShow){
        if (isShow){
            if (!loadDialog.isShowing()){
                loadDialog.show();
            }
        } else {
            DialogUtils.closeDialog(loadDialog);
        }
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d("Tag", "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
//                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    class IatBroadcast extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null){
                if (intent.getAction().equals("startIat")){
                    startTat();
                }
            }
        }
    }

    public void startTat(){
        // 移动数据分析，收集开始听写事件
        FlowerCollector.onEvent(getApplicationContext(), "iat_recognize");
        // 显示听写对话框
        mIatDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0){
            if (resultCode == 0
                    && data != null){
                Audio audio = (Audio) data.getSerializableExtra("audio");
                if (audio != null) {
                    Constants.playAlbum.setAudioId(audio.getId());
                    Constants.playAlbum.setAudioName(audio.getTitle());
                    Constants.playPage = Constants.playList.size() / 10;
                    if (albumDao.isExist(Constants.playAlbum.getTitle()) == -1){
                        albumDao.add(Constants.playAlbum);
                    } else {
                        albumDao.update(Constants.playAlbum);
                    }
                    mediaPlayerManager.player(Constants.playAlbum.getId());
                }
            }
        }
    }
}
