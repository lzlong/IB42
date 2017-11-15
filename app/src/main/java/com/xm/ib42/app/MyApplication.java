package com.xm.ib42.app;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;

import com.iflytek.cloud.SpeechUtility;
import com.tencent.bugly.crashreport.CrashReport;
import com.xm.ib42.R;
import com.xm.ib42.util.MusicPreference;

import org.wlf.filedownloader.FileDownloadConfiguration;
import org.wlf.filedownloader.FileDownloader;

/**
 * Created by Modi on 2016/12/6.
 * 邮箱：1294432350@qq.com
 */

public class MyApplication extends Application {

    public static MediaPlayer mediaPlayer;
    public static MusicPreference musicPreference;

    public static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        mediaPlayer = new MediaPlayer();

        musicPreference = new MusicPreference(context);

        SpeechUtility.createUtility(getApplicationContext(), "appid=" + getString(R.string.app_id));

        // 1、创建Builder
        FileDownloadConfiguration.Builder builder = new FileDownloadConfiguration.Builder(this);

    // 2.配置Builder
    // 配置下载文件保存的文件夹
        /*builder.configFileDownloadDir(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                "FileDownloader");*/
        builder.configFileDownloadDir(Environment.getExternalStorageDirectory().getAbsolutePath());
    // 配置同时下载任务数量，如果不配置默认为2
        builder.configDownloadTaskSize(3);
    // 配置失败时尝试重试的次数，如果不配置默认为0不尝试
        builder.configRetryDownloadTimes(5);
    // 开启调试模式，方便查看日志等调试相关，如果不配置默认不开启
        builder.configDebugMode(true);
    // 配置连接网络超时时间，如果不配置默认为15秒
        builder.configConnectTimeout(150*1000);// 25秒

    // 3、使用配置文件初始化FileDownloader
        FileDownloadConfiguration configuration = builder.build();
        FileDownloader.init(configuration);

        CrashReport.initCrashReport(getApplicationContext(), "e3d13d5cc7", false);

    }
}
