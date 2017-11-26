package com.xm.ib42;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.xm.ib42.adapter.PlayListAdapter;
import com.xm.ib42.app.MyApplication;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.service.MediaPlayerManager;
import com.xm.ib42.util.Common;
import com.xm.ib42.util.HttpHelper;
import com.xm.ib42.util.SystemSetting;
import com.xm.ib42.util.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.xm.ib42.app.MyApplication.context;
import static com.xm.ib42.app.MyApplication.musicPreference;

/**
 * home2
 * @author andye
 *
 */
public class PlayPageFragment extends Fragment implements OnClickListener, AdapterView.OnItemClickListener,
        PullToRefreshBase.OnRefreshListener{

	private MainActivity aty;
	private View convertView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		convertView = inflater.inflate(R.layout.play_page, null);
		return convertView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		aty = (MainActivity) getActivity();
		init(convertView);
	}

	private TextView title_name;
	private ImageView play_down, play_share, play_model,
            play_up, play, play_next, play_list, play_img;
    private TextView play_time, play_alltime;
    private SeekBar play_bar;
    private TextView play_name;
    private PopupWindow playPop;
    private PullToRefreshListView home_search_lv;
    private PlayListAdapter playListAdapter;
    private ImageView session, timeline, favorite, qq, qzone;
    private PopupWindow sharePop;

    public boolean isplaying = false;
    public Intent broadcastIntent;

	private void init(View convertView) {

        title_name = (TextView) convertView.findViewById(R.id.title_name);
        title_name.setText("印心讲堂播放器");
        play_down = (ImageView) convertView.findViewById(R.id.play_down);
        play_share = (ImageView) convertView.findViewById(R.id.play_share);
        play_model = (ImageView) convertView.findViewById(R.id.play_model);
        play_up = (ImageView) convertView.findViewById(R.id.play_up);
        play = (ImageView) convertView.findViewById(R.id.play);
        play_next = (ImageView) convertView.findViewById(R.id.play_next);
        play_list = (ImageView) convertView.findViewById(R.id.play_list);
        play_img = (ImageView) convertView.findViewById(R.id.play_img);
        play_time = (TextView) convertView.findViewById(R.id.play_time);
        play_alltime = (TextView) convertView.findViewById(R.id.play_alltime);
        play_bar = (SeekBar) convertView.findViewById(R.id.play_bar);
        play_name = (TextView) convertView.findViewById(R.id.play_name);

        mSetting = new SystemSetting(aty, false);

        View view = aty.getLayoutInflater().inflate(R.layout.home_search, null);
        home_search_lv = (PullToRefreshListView) view.findViewById(R.id.home_search_lv);
        playPop = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, 600);
        playPop.setContentView(view);
        playPop.setFocusable(true);
        home_search_lv.setMode(PullToRefreshBase.Mode.PULL_FROM_END);

        View shareView = aty.getLayoutInflater().inflate(R.layout.sharepop, null);
        session = (ImageView) shareView.findViewById(R.id.session);
        timeline = (ImageView) shareView.findViewById(R.id.timeline);
        favorite = (ImageView) shareView.findViewById(R.id.favorite);
        qq = (ImageView) shareView.findViewById(R.id.qq);
        qzone = (ImageView) shareView.findViewById(R.id.qzone);
        sharePop = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sharePop.setContentView(shareView);
        sharePop.setFocusable(true);

        click();
        if (aty.mediaPlayerManager.getPlayerState() == MediaPlayerManager.STATE_PLAYER){
            play.setImageResource(R.mipmap.zangt);
        }

        play_time.setText(Common.formatSecondTime(aty.currentDuration));
        play_alltime.setText(Common.formatSecondTime(aty.duration));
        play_bar.setProgress(aty.currentDuration);
        play_bar.setMax(aty.duration);
        play_name.setText(aty.playName);

        if (Constants.playAlbum != null && Constants.playAlbum.getImageUrl() != null){
            Glide.with(aty)
                    .load(Constants.playAlbum.getImageUrl())
                    .placeholder(R.mipmap.kaiping2)
                    .error(R.mipmap.kaiping2)
                    .override(100, 100)
                    .into(play_img);
        }
        ProgeressThread thread = new ProgeressThread();
        thread.start();
    }

    boolean isrunable = true;
    int curms;
    int totalms = 1;

    class ProgeressThread extends Thread {
        @Override
        public void run() {
            while (isrunable) {
                if (MyApplication.mediaPlayer != null
                        && MyApplication.mediaPlayer.isPlaying()) {
                    curms = MyApplication.mediaPlayer.getCurrentPosition();
                    nameshandler.sendEmptyMessage(20);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            super.run();
        }

    }

    Handler nameshandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 20:
//                    try {
//                        int progress = curms * 100 / totalms;
//                        // 设置当前进度
//                        play_bar.setProgress(progress);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    play_bar.setProgress(curms);
                    play_time.setText(Utils.gettim(curms));
                    break;
            }
        }
    };

    private void click() {
        play_down.setOnClickListener(this);
        play_share.setOnClickListener(this);
        play_model.setOnClickListener(this);
        play_up.setOnClickListener(this);
        play.setOnClickListener(this);
        play_next.setOnClickListener(this);
        play_list.setOnClickListener(this);
        session.setOnClickListener(this);
        timeline.setOnClickListener(this);
        favorite.setOnClickListener(this);
        qq.setOnClickListener(this);
        qzone.setOnClickListener(this);
        home_search_lv.setOnItemClickListener(this);
        home_search_lv.setOnRefreshListener(this);
        play_bar.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    @Override
	public void onClick(View v) {
        if (v == play_down){
            if (Constants.playAlbum == null){
                return;
            }
            if(!Common.getNetIsAvailable(aty)){
                Utils.showToast(aty, "当前网络不可用");
                return;
            }
            if(!Common.isExistSdCard()){
                Utils.showToast(aty, "请先插入SD卡");
                return;
            }
//            //判断是否在下载列表中
//            if(aty.downLoadInfoDao.isExist(aty.mediaPlayerManager.getAudio().getNetUrl())){
//                Utils.showToast(aty, "此歌曲已经在下载列表中");
//                return;
//            }
            //判断是否已经下载过
            if(aty.audioDao.isDownFinish(Constants.playAlbum.getAudioId())){
                Utils.showToast(aty, "此歌曲已经在下载过了");
                return;
            }
            //添加到下载列表中
            aty.downLoadManager.add(aty.audioDao.searchById(Constants.playAlbum.getAudioId(), true));
        } else if (v == play_share){
            if (!sharePop.isShowing()){
                sharePop.showAtLocation(convertView, Gravity.BOTTOM, 0, 0);
            }
        } else if (v == play_model){
            if (Constants.playList == null
                    ||Constants.playList.size() <= 0){
                Utils.showToast(aty, "播放列表为空");
                return;
            }
            broadcastIntent = new Intent();
            aty.nowplaymode++;
            if (aty.nowplaymode == 1) {
                play_model.setImageResource(R.mipmap.danquxh);
            } else {
                aty.nowplaymode = 0;
                play_model.setImageResource(R.mipmap.shuanxubf);
            }
            musicPreference.savaPlayMode(context, aty.nowplaymode);
            broadcastIntent.setAction(Constants.ACTION_SET_PLAYMODE);
            broadcastIntent.putExtra("play_mode", aty.nowplaymode);
            aty.sendBroadcast(broadcastIntent);

        } else if (v == play_up){
            if (Constants.playList == null
                    ||Constants.playList.size() <= 0){
                Utils.showToast(aty, "播放列表为空");
                return;
            }

            if (audio != null){
                audio.setCurrDurationTime(MyApplication.mediaPlayer.getCurrentPosition());
                aty.audioDao.updateByDuration(audio.getId(), audio.getCurrDurationTime());
                for (int i = 0; i < Constants.playList.size(); i++) {
                    if (audio.getId() == Constants.playList.get(i).getId()){
                        Constants.playList.get(i).setCurrDurationTime(audio.getCurrDurationTime());
                        break;
                    }
                }
            }

            broadcastIntent = new Intent();
            aty.showLoadDialog(true);
            play.setImageResource(R.mipmap.bof);
            isplaying = true;
            broadcastIntent.setAction(Constants.ACTION_PREVIOUS);
            context.sendBroadcast(broadcastIntent);
        } else if (v == play){
            if (Constants.playList == null
                    ||Constants.playList.size() <= 0){
                Utils.showToast(aty, "播放列表为空");
                return;
            }
            broadcastIntent = new Intent();
            if (!isplaying) {
//                if (aty.position > 0) {// 如果大于0说明我们记忆了上次退出时候的歌曲
//                    broadcastIntent.setAction(Constants.ACTION_JUMR);
//                    broadcastIntent.putExtra("position", aty.position);
//                } else {
//                }
                broadcastIntent.setAction(Constants.ACTION_PLAY);
                context.sendBroadcast(broadcastIntent);
                isplaying = true;
                play.setImageResource(R.mipmap.zangt);
            } else {
                broadcastIntent.setAction(Constants.ACTION_PAUSE);
                aty.sendBroadcast(broadcastIntent);
                isplaying = false;
                play.setImageResource(R.mipmap.bof);
            }
        } else if (v == play_next){
            if (Constants.playList == null
                    ||Constants.playList.size() <= 0){
                Utils.showToast(aty, "播放列表为空");
                return;
            }
            if (audio != null){
                audio.setCurrDurationTime(MyApplication.mediaPlayer.getCurrentPosition());
                aty.audioDao.updateByDuration(audio.getId(), audio.getCurrDurationTime());
                for (int i = 0; i < Constants.playList.size(); i++) {
                    if (audio.getId() == Constants.playList.get(i).getId()){
                        Constants.playList.get(i).setCurrDurationTime(audio.getCurrDurationTime());
                        break;
                    }
                }
            }
            broadcastIntent = new Intent();
            aty.showLoadDialog(true);
            play.setImageResource(R.mipmap.zangt);
            isplaying = true;
            broadcastIntent.setAction(Constants.ACTION_NEXT);
            context.sendBroadcast(broadcastIntent);

        } else if (v == play_list){
            if (Constants.playList != null){
                if (playListAdapter == null){
                    playListAdapter = new PlayListAdapter(aty, Constants.playList);
                    home_search_lv.setAdapter(playListAdapter);
                } else {
                    playListAdapter.notifyDataSetChanged();
                }
//                Audio audio = null;
//                if (audio != null){
//                    playListAdapter.setPlayId(audio.getId());
//                    for (int i = 0; i < Constants.playList.size(); i++) {
//                        if (audio.getId() == Constants.playList.get(i).getId()){
//                            home_search_lv.getRefreshableView().setSelection(i);
//                        }
//                    }
//                }
                if (!playPop.isShowing()){
                    playPop.showAtLocation(convertView, Gravity.BOTTOM, 0, 0);
                }
            }
        } else if (v == session){
            aty.mTargetScene = SendMessageToWX.Req.WXSceneSession;
            shareApp();
        } else if (v == timeline){
            aty.mTargetScene = SendMessageToWX.Req.WXSceneTimeline;
            shareApp();
        } else if (v == favorite){
            aty.mTargetScene = SendMessageToWX.Req.WXSceneFavorite;
            shareApp();
        } else if (v == qq){
            shareApp2QQ();
        } else if (v == qzone){
            shareApp2QQ();
        }
	}

	private void shareApp2QQ(){
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, "印心讲堂");
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  "");
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  Constants.SHAREURL);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, "");
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  "印心讲堂");
        aty.mTencent.shareToQQ(aty, params, new MainActivity.BaseUiListener());
    }

    private static final int THUMB_SIZE = 150;
    private void shareApp(){
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = Constants.SHAREURL;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = "欢迎使用「印心讲堂」";
        msg.description = "";
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.bai);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
        bmp.recycle();
        msg.thumbData = Utils.bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = aty.mTargetScene;
        aty.api.sendReq(req);
    }
    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private SystemSetting mSetting;
    private int currentTime = 0;

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (fromUser == true && Math.abs(progress - curms) >= 5) {
                curms = progress;
                broadcastIntent = new Intent(Constants.ACTION_SEEK);
                broadcastIntent.putExtra("seekcurr", progress);// 讲拖动的进度传进Service
                aty.sendBroadcast(broadcastIntent);
                play_bar.setProgress(progress);
            }
        }
    };

    private MusicinfoRec mMusicinfoRec;
    @Override
    public void onStart() {
        super.onStart();
        mMusicinfoRec=new MusicinfoRec();
        IntentFilter filter = new IntentFilter(MediaPlayerManager.BROADCASTRECEVIER_ACTON);
        filter.addAction(Constants.ACTION_UPDATE);
        filter.addAction(Constants.ACTION_UPDATE_LRC);
        aty.registerReceiver(mMusicinfoRec, filter);

        aty.sendBroadcast(new Intent(Constants.ACTION_UPDATE_ALL));
        aty.nowplaymode = musicPreference.getPlayMode(context);
        if (aty.nowplaymode == 0) {// 0 顺序播放 1 单曲循环
            play_model.setImageResource(R.mipmap.shuanxubf);
        } else if (aty.nowplaymode == 1) {
            play_model.setImageResource(R.mipmap.danquxh);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        aty.unregisterReceiver(mMusicinfoRec);
    }
    private boolean isSeekDrag = false;//进度是否在拖动

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Audio audio = (Audio) parent.getAdapter().getItem(position);
        if (audio != null){
            Constants.playAlbum.setAudioId(audio.getId());
            Constants.playAlbum.setAudioName(audio.getTitle());
            playListAdapter.setPlayId(audio.getId());
            home_search_lv.getRefreshableView().setSelection(position);
//            aty.mediaPlayerManager.player(Constants.playAlbum.getId());
            Intent intent = new Intent(Constants.ACTION_JUMR);
            intent.putExtra("position", position-1);
            context.sendBroadcast(intent);
            playPop.dismiss();
        }
    }


    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
        Constants.playPage++;
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpHelper httpHelper = new HttpHelper();
                httpHelper.connect();
                List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
                list.add(new BasicNameValuePair(Constants.VALUES[0], "1"));
                list.add(new BasicNameValuePair(Constants.VALUES[1], Constants.playAlbum.getId()+""));
                list.add(new BasicNameValuePair(Constants.VALUES[2], Constants.playPage+""));
                HttpResponse httpResponse = httpHelper.doGet(Constants.HTTPURL, list);
                JSONObject json = Utils.parseResponse(httpResponse);
                List<Audio> l = Utils.pressAudioJson(json, Constants.playAlbum);
                mHandler.sendMessage(mHandler.obtainMessage(0, l));
            }
        }).start();
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0){
                List<Audio> list = (List<Audio>) msg.obj;
                if (list != null){
                    Constants.playList.addAll(list);
                    playListAdapter.notifyDataSetChanged();
                }
                home_search_lv.onRefreshComplete();
            }
        }
    };

    /**
     * 播放器-广播接收器
     * */
    private Audio audio;

    private class MusicinfoRec extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_UPDATE)) {
                aty.isShow = false;
                aty.showLoadDialog(false);
                aty.position = intent.getIntExtra("position", 0);
                audio = (Audio) intent.getSerializableExtra("music");
                if (audio == null)return;
                if (audio != null && playListAdapter != null){
                    playListAdapter.setPlayId(audio.getId());
                    for (int i = 0; i < Constants.playList.size(); i++) {
                        if (audio.getId() == Constants.playList.get(i).getId()){
                            home_search_lv.getRefreshableView().setSelection(i);
                        }
                    }
                }
                totalms = intent.getIntExtra("totalms", 288888);// 总时长
                play_bar.setMax(totalms);
                play_alltime.setText(Utils.gettim(totalms));
                play_name.setText(audio.getTitle());
                if (MyApplication.mediaPlayer.isPlaying()) {
                    play.setImageResource(R.mipmap.zangt);
                    isplaying = true;
                } else {
                    isplaying = false;
                    play.setImageResource(R.mipmap.bof);
                }
            }
        }
    }


}
