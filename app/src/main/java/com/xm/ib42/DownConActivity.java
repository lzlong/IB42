package com.xm.ib42;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ListView;

import com.xm.ib42.adapter.DownConAdapter;
import com.xm.ib42.constant.Constants;
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
    private ProBroadcastReceiver mProBroadcastReceiver;

    private void initView() {
        down_con_lv = (ListView) findViewById(R.id.down_con_lv);
        mDownLoadInfoDao = new DownLoadInfoDao(this);
        mProBroadcastReceiver = new ProBroadcastReceiver();
        IntentFilter filter = new IntentFilter(Constants.ACTION_DOWN_CON);
        registerReceiver(mProBroadcastReceiver, filter);

        getData();
    }

    private DownLoadInfoDao mDownLoadInfoDao;
    private List<DownLoadInfo> list= new ArrayList<>();
    private DownConAdapter mDownAdapter;

    private void getData() {
        list = mDownLoadInfoDao.searchAll();
        if (list != null){
            if (mDownAdapter == null) {
                mDownAdapter = new DownConAdapter(this, list);
                down_con_lv.setAdapter(mDownAdapter);
            }
        }
    }

    private class ProBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_DOWN_CON)){
                DownLoadInfo downLoadInfo = (DownLoadInfo) intent.getSerializableExtra("downLoadInfo");
                if (downLoadInfo != null){
                    for (int i = 0; i < list.size(); i++) {
                        if (downLoadInfo.getId() == list.get(i).getId()){
                            list.get(i).setCompleteSize(downLoadInfo.getCompleteSize());
                        }
                    }
                    mDownAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mProBroadcastReceiver);
    }
}
