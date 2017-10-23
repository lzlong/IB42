package com.xm.ib42;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.xm.ib42.adapter.PlayListAdapter;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.service.MediaPlayerManager;
import com.xm.ib42.util.Common;
import com.xm.ib42.util.SystemSetting;
import com.xm.ib42.util.Utils;

import static com.xm.ib42.service.MediaPlayerManager.STATE_PLAYER;

/**
 * home2
 * @author andye
 *
 */
public class PlayPageFragment extends Fragment implements OnClickListener, AdapterView.OnItemClickListener {

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
    private ListView home_search_lv;
    private PlayListAdapter playListAdapter;
    private ImageView session, timeline, favorite;
    private PopupWindow sharePop;

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
        home_search_lv = (ListView) view.findViewById(R.id.home_search_lv);
        playPop = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, 600);
        playPop.setContentView(view);
        playPop.setFocusable(true);

        View shareView = aty.getLayoutInflater().inflate(R.layout.sharepop, null);
        session = (ImageView) shareView.findViewById(R.id.session);
        timeline = (ImageView) shareView.findViewById(R.id.timeline);
        favorite = (ImageView) shareView.findViewById(R.id.favorite);
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
    }

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
        home_search_lv.setOnItemClickListener(this);
        play_bar.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    @Override
	public void onClick(View v) {
        if (v == play_down){
            if (Constants.playAlbum == null
                    ||aty.mediaPlayerManager.getAudio() == null){
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
            if(aty.audioDao.isDownFinish(aty.mediaPlayerManager.getAudio().getId())){
                Utils.showToast(aty, "此歌曲已经在下载过了");
                return;
            }
            //添加到下载列表中
            aty.downLoadManager.add(aty.mediaPlayerManager.getAudio());
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
            if (aty.mediaPlayerManager.getPlayerMode() == MediaPlayerManager.MODE_CIRCLELIST){
                aty.mediaPlayerManager.setPlayerMode(MediaPlayerManager.MODE_CIRCLEONE);
                play_model.setImageResource(R.mipmap.danquxh);
            } else {
                aty.mediaPlayerManager.setPlayerMode(MediaPlayerManager.MODE_CIRCLELIST);
                play_model.setImageResource(R.mipmap.shuanxubf);
            }
        } else if (v == play_up){
            if (Constants.playList == null
                    ||Constants.playList.size() <= 0){
                Utils.showToast(aty, "播放列表为空");
                return;
            }
            aty.showLoadDialog(true);
            aty.mediaPlayerManager.previousPlayer();
        } else if (v == play){
            if (Constants.playList == null
                    ||Constants.playList.size() <= 0){
                Utils.showToast(aty, "播放列表为空");
                return;
            }
            aty.mediaPlayerManager.pauseOrPlayer();
            if (aty.mediaPlayerManager.getPlayerState() == STATE_PLAYER){
                play.setImageResource(R.mipmap.zangt);
            } else {
                play.setImageResource(R.mipmap.bof);
            }
        } else if (v == play_next){
            if (Constants.playList == null
                    ||Constants.playList.size() <= 0){
                Utils.showToast(aty, "播放列表为空");
                return;
            }
            aty.showLoadDialog(true);
            aty.mediaPlayerManager.nextPlayer();
        } else if (v == play_list){
            if (Constants.playList != null){
                if (playListAdapter == null){
                    playListAdapter = new PlayListAdapter(aty, Constants.playList);
                    home_search_lv.setAdapter(playListAdapter);
                } else {
                    playListAdapter.notifyDataSetChanged();
                }
                Audio audio = aty.mediaPlayerManager.getAudio();
                if (audio != null){
                    playListAdapter.setPlayId(audio.getId());
                    for (int i = 0; i < Constants.playList.size(); i++) {
                        if (audio.getId() == Constants.playList.get(i).getId()){
                            home_search_lv.setSelection(i);
                        }
                    }
                }
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
        }
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
            if (seekBar.getId() == R.id.play_bar) {
                isSeekDrag = false;
                aty.mediaPlayerManager.seekTo(seekBar.getProgress());
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            if (seekBar.getId() == R.id.play_bar) {
                isSeekDrag = true;
                play_time.setText(Common.formatSecondTime(seekBar.getProgress()));
            }
        }

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (seekBar.getId() == R.id.play_bar) {
                if (isSeekDrag) {
                    play_time.setText(Common.formatSecondTime(progress));
                }
            }

            if (mSetting.getBooleanValue(Constants.TryListener)){
                int millions = progress / 1000;
                if (millions>10){
                    aty.mediaPlayerManager.nextPlayer();
                }
            }
            currentTime = progress;
        }
    };

    private MediaPlayerBroadcastReceiver mediaPlayerBroadcastReceiver;
    @Override
    public void onStart() {
        super.onStart();
        mediaPlayerBroadcastReceiver=new MediaPlayerBroadcastReceiver();
        aty.registerReceiver(mediaPlayerBroadcastReceiver, new IntentFilter(MediaPlayerManager.BROADCASTRECEVIER_ACTON));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        aty.unregisterReceiver(mediaPlayerBroadcastReceiver);
    }
    private boolean isSeekDrag = false;//进度是否在拖动

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Audio audio = (Audio) parent.getAdapter().getItem(position);
        if (audio != null){
            Constants.playAlbum.setAudioId(audio.getId());
            Constants.playAlbum.setAudioName(audio.getTitle());
            playListAdapter.setPlayId(audio.getId());
            home_search_lv.setSelection(position);
            aty.mediaPlayerManager.player(Constants.playAlbum.getId());
            playPop.dismiss();
        }
    }

    /**
     * 播放器-广播接收器
     * */
    private class MediaPlayerBroadcastReceiver extends BroadcastReceiver {
        private Bitmap mCachedArtwork;
        private Bitmap mDefaultArtWork;
        @Override
        public void onReceive(Context context, Intent intent) {
            aty.showLoadDialog(false);
            int flag=intent.getIntExtra("flag", -1);
            int lAlbum_id = intent.getIntExtra("album_id", -1);

            mDefaultArtWork = BitmapFactory.decodeResource(aty.getResources(), R.mipmap.kaiping2);
            if (lAlbum_id == -1){
                mCachedArtwork = mDefaultArtWork;
            }else {
//                mCachedArtwork = MusicUtils.getCachedArtwork(context, lAlbum_id, mDefaultArtWork);
            }

            if(flag==MediaPlayerManager.FLAG_CHANGED){
                if (!isSeekDrag) {
                    int currentPosition = intent.getIntExtra("currentPosition", 0);
                    int duration = intent.getIntExtra("duration", 0);
                    play_time.setText(Common.formatSecondTime(currentPosition));
                    play_alltime.setText(Common.formatSecondTime(duration));
                    play_bar.setProgress(currentPosition);
                    //progress change
                    currentTime = currentPosition;
                    play_bar.setMax(duration);

                    aty.duration = duration;
                    aty.currentDuration = currentPosition;
                }
                int currentPosition=intent.getIntExtra("currentPosition", 0);
                int duration=intent.getIntExtra("duration", 0);
                play_time.setText(Common.formatSecondTime(currentPosition));
                play_alltime.setText(Common.formatSecondTime(duration));
                play_bar.setProgress(currentPosition);
                play_bar.setMax(duration);
                play.setImageResource(R.mipmap.zangt);
                aty.duration = duration;
                aty.currentDuration = currentPosition;
                aty.playName = intent.getStringExtra("audioName");
                play_name.setText(intent.getStringExtra("audioName"));
            }else if(flag==MediaPlayerManager.FLAG_PREPARE){
                String albumPic=intent.getStringExtra("albumPic");
                int duration=intent.getIntExtra("duration", 0);
                int currentPosition=intent.getIntExtra("currentPosition", 0);
                play_time.setText(Common.formatSecondTime(currentPosition));
                play_alltime.setText(Common.formatSecondTime(duration));
                play_bar.setProgress(currentPosition);
                play_bar.setMax(duration);
                play_name.setText(intent.getStringExtra("audioName"));
//                aty.showLoadDialog(true);
            }else if(flag==MediaPlayerManager.FLAG_INIT){//初始化播放信息
//                int duration=intent.getIntExtra("duration", 0);
//                int currentPosition=intent.getIntExtra("currentPosition", 0);
//                play_time.setText(Common.formatSecondTime(currentPosition));
//                play_alltime.setText(Common.formatSecondTime(duration));
//                play_bar.setProgress(currentPosition);
//                play_bar.setMax(duration);
//                play_name.setText(intent.getStringExtra("audioName"));
//                int playerState=intent.getIntExtra("playerState", 0);
//                if(playerState== STATE_PLAYER||playerState==MediaPlayerManager.STATE_PREPARE
//                        ||playerState==MediaPlayerManager.STATE_OVER){//播放
//                    aty.showLoadDialog(false);
//                }else{
//                    aty.showLoadDialog(true);
//                }

            }else if(flag==MediaPlayerManager.FLAG_LIST){
                //自动切歌播放，更新前台歌曲列表
                //modi 发送更新歌词界面消息
            }else if(flag==MediaPlayerManager.FLAG_BUFFERING){
//                aty.showLoadDialog(true);
//                play_time.setText("00:00");
//                play_alltime.setText("00:00");
//                play_bar.setProgress(0);
//                play_bar.setMax(100);
            }
        }
    }

}
