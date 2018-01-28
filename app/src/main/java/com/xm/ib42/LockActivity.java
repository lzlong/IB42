package com.xm.ib42;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.xm.ib42.app.MyApplication;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.dao.AudioDao;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.util.SildingFinishLayout;
import com.xm.ib42.util.Utils;

/**
 * Created by long on 18-1-7.
 */

public class LockActivity extends Activity implements View.OnClickListener {

    private TextView mTime,mDate,mMusicName,mMusicArtsit,mLrc;
    private ImageView pre,play,next;
    private Handler mHandler;
    private SildingFinishLayout mView;
    public AudioDao audioDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                        // bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        setContentView(R.layout.locklayout);
        mMusicName = (TextView) findViewById(R.id.lock_music_name);
        mMusicArtsit = (TextView) findViewById(R.id.lock_music_artsit);
        pre = (ImageView) findViewById(R.id.lock_music_pre);
        play = (ImageView) findViewById(R.id.lock_music_play);
        next = (ImageView) findViewById(R.id.lock_music_next);
        mView = (SildingFinishLayout) findViewById(R.id.lock_root);
        mView.setOnSildingFinishListener(new SildingFinishLayout.OnSildingFinishListener() {

            @Override
            public void onSildingFinish() {
                finish();
            }
        });
        mView.setTouchView(getWindow().getDecorView());
        pre.setOnClickListener(this);
        play.setOnClickListener(this);
        next.setOnClickListener(this);

        audioDao = new AudioDao(this);

        if (MyApplication.mediaPlayer.isPlaying()) {
            play.setImageResource(R.mipmap.zangt);
            isplaying = true;
            mMusicArtsit.setText(Constants.playAlbum.getTitle());
        } else {
            finish();
        }

    }

    private MusicinfoRec mMusicinfoRec;
    @Override
    public void onStart() {
        super.onStart();
        mMusicinfoRec=new MusicinfoRec();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_UPDATE);
        filter.addAction(Constants.ACTION_UPDATE_LRC);
        registerReceiver(mMusicinfoRec, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("lock"," on resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("lock"," on pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("lock"," on stop");
    }


    public Intent broadcastIntent;
    public boolean isplaying = false;

    @Override
    public void onClick(View view) {
        if (view == pre){
            if (Constants.playList == null
                    ||Constants.playList.size() <= 0){
                Utils.showToast(this, "播放列表为空");
                return;
            }
            if (audio != null){
                audio.setCurrDurationTime(MyApplication.mediaPlayer.getCurrentPosition());
                audioDao.updateByDuration(audio.getId(), audio.getCurrDurationTime());
                for (int i = 0; i < Constants.playList.size(); i++) {
                    if (audio.getId() == Constants.playList.get(i).getId()){
                        Constants.playList.get(i).setCurrDurationTime(audio.getCurrDurationTime());
                        break;
                    }
                }
            }

            broadcastIntent = new Intent();
            play.setImageResource(R.mipmap.bof);
            isplaying = true;
            broadcastIntent.setAction(Constants.ACTION_PREVIOUS);
            sendBroadcast(broadcastIntent);
        } else if (view == play){
            if (Constants.playList == null
                    ||Constants.playList.size() <= 0){
                Utils.showToast(this, "播放列表为空");
                return;
            }
            broadcastIntent = new Intent();
            if (!isplaying) {
                broadcastIntent.setAction(Constants.ACTION_PLAY);
                sendBroadcast(broadcastIntent);
                isplaying = true;
                play.setImageResource(R.mipmap.zangt);
            } else {
                broadcastIntent.setAction(Constants.ACTION_PAUSE);
                sendBroadcast(broadcastIntent);
                isplaying = false;
                play.setImageResource(R.mipmap.bof);
            }
        } else if (view == next){
            if (Constants.playList == null
                    ||Constants.playList.size() <= 0){
                Utils.showToast(this, "播放列表为空");
                return;
            }
            if (audio != null){
                audio.setCurrDurationTime(MyApplication.mediaPlayer.getCurrentPosition());
                audioDao.updateByDuration(audio.getId(), audio.getCurrDurationTime());
                for (int i = 0; i < Constants.playList.size(); i++) {
                    if (audio.getId() == Constants.playList.get(i).getId()){
                        Constants.playList.get(i).setCurrDurationTime(audio.getCurrDurationTime());
                        break;
                    }
                }
            }
            broadcastIntent = new Intent();
            play.setImageResource(R.mipmap.zangt);
            isplaying = true;
            broadcastIntent.setAction(Constants.ACTION_NEXT);
            sendBroadcast(broadcastIntent);

        }
    }


    private Audio audio;

    /**
     * 播放器-广播接收器
     * */
    private class MusicinfoRec extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_UPDATE)) {
                int position = intent.getIntExtra("position", 0);
                audio = (Audio) intent.getSerializableExtra("music");
                if (audio == null)return;
                if (audio != null){
                    mMusicName.setText(audio.getTitle());
                    if (MyApplication.mediaPlayer.isPlaying()) {
                        play.setImageResource(R.mipmap.zangt);
                        isplaying = true;
                    } else {
                        isplaying = false;
                        play.setImageResource(R.mipmap.bof);
                    }
                    mMusicArtsit.setText(Constants.playAlbum.getTitle());
                }
            }
        }
    }

}
