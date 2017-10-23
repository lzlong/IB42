package com.xm.ib42.entity;

import java.util.List;

/**
 * Created by long on 17-10-23.
 */

public class Column {
    private int id;
    private String title;
    private String url;
    private List<Album> mAlbumList

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Album> getAlbumList() {
        return mAlbumList;
    }

    public void setAlbumList(List<Album> albumList) {
        mAlbumList = albumList;
    }
}
