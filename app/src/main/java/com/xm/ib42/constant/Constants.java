package com.xm.ib42.constant;

import com.xm.ib42.entity.Album;
import com.xm.ib42.entity.Audio;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by long on 17-8-27.
 */
public class Constants {
    public static final String TAG = "Tag";
    public static final String HTTPURL = "http://g1.124000.org/";
//    public static final String COLUMNURL = "http://g1.124000.org/";
//    public static final String ALBUMURL = "http://g1.124000.org/";
//    public static final String ALBUMSUBURL = "http://g1.124000.org/";
    public static final String SEARCHURL = "http://xm.ib42.com/?json=get_search_result&s=";
    public static final String UPDATEVERURL = "http://app.webfuwu.com/up.asp?appid=1696";
    public static final String APPDOWNURL = "http://app.webfuwu.com";
    public static final String SHAREURL = "http://app.webfuwu.com/?appid=1696";
    public static final String URLPAGE = "&page=";
    public static final String[] VALUES = {"json", "p", "page", "keyword", "lm_id", "pn"};

    public static String MSG_SONG_PLAY_OVER = "歌曲播放完成切换下一曲";

    public static String TryListener = "TRYLISTENER";

    public static final String APP_ID = "wx146a438a7e069ff1";
    public static final String QQAPP_ID = "101429567";
    public static final String QQAPP_KEY = "c8baaac02e6f925782bcbc73a3cf21dd";

    public static Album playAlbum;
    public static final List<Audio> playList = new ArrayList<>();
    public static int playPage = 1;


    public static final String ACTION_PLAY = "com.xm.ib42.PLAY";
    public static final String ACTION_NET_PLAY = "com.xm.ib42.NET_PLAY";
    public static final String ACTION_PAUSE = "com.xm.ib42.PAUSE";
    public static final String ACTION_STOP = "com.xm.ib42.STOP";
    public static final String ACTION_SEEK = "com.xm.ib42.SEEK";
    public static final String ACTION_PREVIOUS = "com.xm.ib42.PREVIOUS";
    public static final String ACTION_NEXT = "com.xm.ib42.NEXT";
    public static final String ACTION_JUMR = "com.xm.ib42.JUMP";
    public static final String ACTION_JUMR_MYPAGE = "com.xm.ib42.JUMP_MYPAGE";
    public static final String ACTION_JUMR_OTHER = "com.xm.ib42.JUMP_OTHER";
    public static final String ACTION_FIND = "com.xm.ib42.FIND";
    public static final String ACTION_UPDATE = "com.xm.ib42.UPDATE";
    public static final String ACTION_UPDATE_ALL = "com.xm.ib42.UPDATE_ALL";
    public static final String ACTION_UPDATE_TIME = "com.xm.ib42.UPDATE_TIME";
    public static final String ACTION_UPDATE_LRC = "com.xm.ib42.UPDATE_LRC";
    public static final String ACTION_SERVICESTOPED = "com.xm.ib42.SERVICESTOPED";
    public static final String ACTION_LISTCHANGED = "com.xm.ib42.LISTCHANGED";
    public static final String ACTION_DISS_DIALOG = "com.xm.ib42.DISSDIALOG";
    public static final String ACTION_SET_PLAYMODE = "com.xm.ib42.PALY_MODE";
    public static final String ACTION_STAR_THREAD = "com.xm.ib42.STAR_THREA";
    public static final String ACTION_LRC = "com.xm.ib42.lrc";
    
    
}
