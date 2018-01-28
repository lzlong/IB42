package com.xm.ib42.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by long on 17-8-27.
 */

public class Album implements Serializable{
    private int id;
    private String title;
    private String imageUrl;
    private int audioIdDesc;
    private int audioIdAsc;
    private String audioNameDesc;
    private String audioNameAsc;
    private List<Audio> audioList;
    private int audioNum;
    private boolean isDelete;
    private int yppx; // 0 升序, 1 降序

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Audio> getAudioList() {
        return audioList;
    }

    public void setAudioList(List<Audio> audioList) {
        this.audioList = audioList;
    }

    public int getAudioIdDesc() {
        return audioIdDesc;
    }

    public void setAudioIdDesc(int audioIdDesc) {
        this.audioIdDesc = audioIdDesc;
    }

    public int getAudioIdAsc() {
        return audioIdAsc;
    }

    public void setAudioIdAsc(int audioIdAsc) {
        this.audioIdAsc = audioIdAsc;
    }

    public String getAudioNameDesc() {
        return audioNameDesc;
    }

    public void setAudioNameDesc(String audioNameDesc) {
        this.audioNameDesc = audioNameDesc;
    }

    public String getAudioNameAsc() {
        return audioNameAsc;
    }

    public void setAudioNameAsc(String audioNameAsc) {
        this.audioNameAsc = audioNameAsc;
    }

    public int getAudioNum() {
        return audioNum;
    }

    public void setAudioNum(int audioNum) {
        this.audioNum = audioNum;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public int getYppx() {
        return yppx;
    }

    public void setYppx(int yppx) {
        this.yppx = yppx;
    }
}
