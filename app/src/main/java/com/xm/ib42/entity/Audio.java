package com.xm.ib42.entity;

import java.io.Serializable;

/**
 * Created by long on 17-8-27.
 */

public class Audio implements Serializable{

    private int id;// id
    private Album album;// 专辑
    private String title;//名称
    private String displayName;// 文件名称[含后缀名]
    private String netUrl;// 网络路径
    private int durationTime;// 播放时间
    private int currDurationTime;// 播放时间
    private int size;// 文件大小
    private String filePath;// 文件路径
    private String cachePath;// 缓存路径
//    private String playerList;// 播放列表的Id集合，它们之间用’$id$’分隔
    private boolean isNet;// 是否是网络音乐
    private boolean isDownFinish;// 是否是下载完成
    private boolean isCacheFinish;// 是否缓存完成
    private int state;
    private boolean isCheck;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNetUrl() {
        return netUrl;
    }

    public void setNetUrl(String netUrl) {
        this.netUrl = netUrl;
    }

    public int getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(int durationTime) {
        this.durationTime = durationTime;
    }

    public int getCurrDurationTime() {
        return currDurationTime;
    }

    public void setCurrDurationTime(int currDurationTime) {
        this.currDurationTime = currDurationTime;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
//
//    public String getPlayerList() {
//        return playerList;
//    }
//
//    public void setPlayerList(String playerList) {
//        this.playerList = playerList;
//    }
//
    public boolean isNet() {
        return isNet;
    }

    public void setNet(boolean net) {
        isNet = net;
    }

    public boolean isDownFinish() {
        return isDownFinish;
    }

    public void setDownFinish(boolean downFinish) {
        isDownFinish = downFinish;
    }

    public boolean isCacheFinish() {
        return isCacheFinish;
    }

    public void setCacheFinish(boolean cacheFinish) {
        isCacheFinish = cacheFinish;
    }

    public String getCachePath() {
        return cachePath;
    }

    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }
}
