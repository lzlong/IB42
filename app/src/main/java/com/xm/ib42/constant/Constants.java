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

}
