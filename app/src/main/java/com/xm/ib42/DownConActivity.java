package com.xm.ib42;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ListView;

import com.xm.ib42.adapter.DownConAdapter;
import com.xm.ib42.dao.DownLoadInfoDao;
import com.xm.ib42.entity.DownLoadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by long on 17-11-6.
 */

public class DownConActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downlayout);
        initView();
    }

    private ListView down_con_lv;

    private void initView() {
        down_con_lv = (ListView) findViewById(R.id.down_con_lv);
        mDownLoadInfoDao = new DownLoadInfoDao(this);

        detData();
    }

    private DownLoadInfoDao mDownLoadInfoDao;
    private List<DownLoadInfo> list= new ArrayList<>();
    private DownConAdapter mDownAdapter;

    private void detData() {
        list = mDownLoadInfoDao.searchAll();
        if (list != null){
            if (mDownAdapter == null) {
                mDownAdapter = new DownConAdapter(this, list);
            }
        }
    }



}
