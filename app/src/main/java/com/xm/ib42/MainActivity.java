package com.xm.ib42;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.tencent.open.utils.HttpUtils;
import com.tencent.tauth.IRequestListener;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.xm.ib42.app.MyApplication;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.dao.AlbumDao;
import com.xm.ib42.dao.AudioDao;
import com.xm.ib42.dao.DownLoadInfoDao;
import com.xm.ib42.entity.Column;
import com.xm.ib42.service.DownLoadManager;
import com.xm.ib42.service.LockService;
import com.xm.ib42.service.MediaPlayerService;
import com.xm.ib42.util.AppUtils;
import com.xm.ib42.util.DialogUtils;
import com.xm.ib42.util.HttpHelper;
import com.xm.ib42.util.SmartDownloadProgressListener;
import com.xm.ib42.util.SmartFileDownloader;
import com.xm.ib42.util.Utils;
import com.xm.ib42.util.WPopupWindow;

import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.xm.ib42.app.MyApplication.context;

public class MainActivity extends FragmentActivity implements View.OnClickListener{

    private LinearLayout mBt1, mBt2, mBt3, mBt4;
    private LinearLayout mTab_item_container;
    private FragmentManager mFM = null;

    LinearLayout content_container;
    Intent m_Intent;

    public AlbumDao albumDao;
    public AudioDao audioDao;
    public DownLoadInfoDao mDownLoadInfoDao;

//    public List<Album> homeList;
    public List<Column> homeList;

    private Dialog loadDialog;

    // 语音听写对象
    public SpeechRecognizer mIat;
    // 语音听写UI
    public RecognizerDialog mIatDialog;

    public IWXAPI api;
    public int mTargetScene = SendMessageToWX.Req.WXSceneSession;

    public int duration = 0;//
    public int currentDuration = 0;// 已经播放时长
    public String playName = "";
    public Tencent mTencent;
    public int position;
    public int albumId = -1;
    public int nowplaymode;// 当前播放模式

    public boolean isShow = true;
    public boolean isFrist = true;

    public SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTencent = Tencent.createInstance(Constants.QQAPP_ID, this.getApplicationContext());

        init();

        changeHome();

        getService();

    }

    int versionCode;
    private void updateVersion() {
        if (!isShow)return;
        versionCode = AppUtils.getAppVersionCode(MainActivity.this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpHelper httpHelper = new HttpHelper();
                httpHelper.connect();
                HttpResponse httpResponse = httpHelper.doGet(Constants.UPDATEVERURL);
//                String data = "{{v:2},{mess:更新说明：当前版本V2.0 优化已知BUG},{urls:/uploadfile/image/20170710/20170710182813541354.apk}}";
                String data = Utils.parseResponseData(httpResponse);
                updateMap = Utils.parseVersionData(data);
                if (updateMap != null && Utils.isUpdate(updateMap.get("v"), MainActivity.this)){
                    updateHandler.sendMessage(updateHandler.obtainMessage(0));
                }
            }
        }).start();
    }

    private Map<String, String> updateMap;
    String dir = Environment.getExternalStorageDirectory().toString() +
            File.separator + Constants.APK_DIRECTORY + File.separator;//文件保存目录
    String path = "";
    Handler updateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0){
                if (updateMap != null){
                    path = Constants.APPDOWNURL+updateMap.get("urls");
                    showDownload();
                }
            } else if (msg.what == 1){
                int size = msg.getData().getInt("size");
                pro.setProgress(size);
                float result = (float)pro.getProgress()/ (float)pro.getMax();
                int p = (int)(result*100);
                if(pro.getProgress()==pro.getMax()) {
                    Utils.showToast(MainActivity.this, "下载成功, 正在准备安装");
                    if (popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                    AppUtils.installApp(MainActivity.this, dir + path.substring(path.lastIndexOf('/') + 1));
                }
            } else if (msg.what == -1){
                wh.findViewById(R.id.down_l).setVisibility(View.VISIBLE);
                wh.findViewById(R.id.down_ll).setVisibility(View.GONE);
                Utils.showToast(MainActivity.this, msg.getData().getString("error"));
            }
        }
    };
    private ProgressBar pro;
    WPopupWindow popupWindow;
    View wh;
    private void showDownload() {
        wh = LayoutInflater.from(this).inflate(R.layout.downpop,null);
        pro = (ProgressBar) wh.findViewById(R.id.down_pro);
        popupWindow = new WPopupWindow(wh);
        popupWindow.showAtLocation(Utils.getContentView(MainActivity.this), Gravity.CENTER, 0, 0);
        wh.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    File file = new File(dir);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    ((TextView)wh.findViewById(R.id.down_t)).setText("正在下载");
                    wh.findViewById(R.id.down_l).setVisibility(View.GONE);
                    wh.findViewById(R.id.down_ll).setVisibility(View.VISIBLE);
                    download(wh, pro, path, file);
                }else{
                    Utils.showToast(MainActivity.this, "SDCard不存在或者写保护");
                }
            }
        });
        wh.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        wh.findViewById(R.id.houtai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        wh.findViewById(R.id.cancel_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loader != null){
                    loader.pause(true);
                }
                popupWindow.dismiss();
            }
        });
    }
    SmartFileDownloader loader;
    private void download(final View wh, final ProgressBar pro, final String path, final File dir) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    loader = new SmartFileDownloader(MainActivity.this, path, dir, 3);
                    int length = loader.getFileSize();//获取文件的长度
                    pro.setMax(length);
                    loader.download(new SmartDownloadProgressListener() {
                        @Override
                        public void onDownloadSize(int size) {//可以实时得到文件下载的长度
                            Message msg = new Message();
                            msg.what = 1;
                            msg.getData().putInt("size", size);
                            updateHandler.sendMessage(msg);
                        }
                    });
                } catch (Exception e) {
                    Message msg = new Message();//信息提示
                    msg.what = -1;
                    msg.getData().putString("error", "下载失败, 点击确定重新下载");//如果下载错误，显示提示失败！
                    updateHandler.sendMessage(msg);
                }
            }
        }).start();//开始
    }


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
        mDownLoadInfoDao = new DownLoadInfoDao(this);

        homeList = new ArrayList<>();

        content_container = (LinearLayout) findViewById(R.id.content_container);


        position = MyApplication.musicPreference.getSavePosition(context);
        albumId = MyApplication.musicPreference.getAlbum(context);

        api = WXAPIFactory.createWXAPI(getApplicationContext(), Constants.APP_ID);
        api.registerApp(Constants.APP_ID);

        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(getApplicationContext(), mInitListener);

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(this, mInitListener);

        mSharedPreferences = getSharedPreferences("column", Context.MODE_PRIVATE);
        homeList = Utils.getColumn(mSharedPreferences);

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

    public static DownLoadManager downLoadManager;
    private IatBroadcast iatBroadcast;

    private void getService(){
        //播放器管理

        startService(new Intent(context, MediaPlayerService.class));
        startService(new Intent(context, LockService.class));

        downLoadManager=new DownLoadManager(this);
        downLoadManager.startAndBindService();

        iatBroadcast = new IatBroadcast();
        registerReceiver(iatBroadcast, new IntentFilter("startIat"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isFrist){
            isFrist = false;
            updateVersion();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downLoadManager.unbindService();
//        EventBus.getDefault().unregister(this);
        if( null != mIat ){
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }

        //保存播放状态
        if (!MyApplication.mediaPlayer.isPlaying()){
            MyApplication.musicPreference.savePlayAlbum(this, Constants.playAlbum);
        }

        unregisterReceiver(iatBroadcast);

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

    class IatBroadcast extends BroadcastReceiver {

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
                changePlay();
//                showLoadDialog(true);
                Intent intent = new Intent(Constants.ACTION_JUMP);
                intent.putExtra("position", data.getIntExtra("position", position));
                context.sendBroadcast(intent);
            } else if (resultCode == 1
                    && data != null){
                changePlay();
//                showLoadDialog(true);
                Intent intent = new Intent(Constants.ACTION_JUMP_MYPAGE);
                intent.putExtra("title", data.getStringExtra("title"));
                context.sendBroadcast(intent);
            }
        }
    }

    static class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object o) {
//            doComplete(response);
        }
        protected void doComplete(JSONObject values) {
        }
        @Override
        public void onError(UiError uiError) {

        }

        @Override
        public void onCancel() {

        }
    }

    class BaseApiListener implements IRequestListener {

        @Override
        public void onComplete(JSONObject jsonObject) {

        }

        @Override
        public void onIOException(IOException e) {

        }

        @Override
        public void onMalformedURLException(MalformedURLException e) {

        }

        @Override
        public void onJSONException(JSONException e) {

        }

        @Override
        public void onConnectTimeoutException(ConnectTimeoutException e) {

        }

        @Override
        public void onSocketTimeoutException(SocketTimeoutException e) {

        }

        @Override
        public void onNetworkUnavailableException(HttpUtils.NetworkUnavailableException e) {
            // 当前网络不可用时触发此异常
        }

        @Override
        public void onHttpStatusException(HttpUtils.HttpStatusException e) {
            // http请求返回码非200时触发此异常
        }

        @Override
        public void onUnknowException(Exception e) {
            // 出现未知错误时会触发此异常
        }
    }



}
