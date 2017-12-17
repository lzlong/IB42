package com.xm.ib42;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.xm.ib42.adapter.DownConAdapter;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.dao.DownLoadInfoDao;
import com.xm.ib42.entity.DownLoadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by long on 17-11-6.
 */

public class DownConActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downlayout);
        initView();
    }

    private TextView title_back;
    private ListView down_con_lv;
    private ProBroadcastReceiver mProBroadcastReceiver;

    private void initView() {
        title_back = (TextView) findViewById(R.id.title_back);
        title_back.setVisibility(View.VISIBLE);
        title_back.setOnClickListener(this);
        down_con_lv = (ListView) findViewById(R.id.down_con_lv);
        mDownLoadInfoDao = new DownLoadInfoDao(this);
        mProBroadcastReceiver = new ProBroadcastReceiver();
        IntentFilter filter = new IntentFilter(Constants.ACTION_DOWN_CON);
        registerReceiver(mProBroadcastReceiver, filter);

        down_con_lv.setOnItemClickListener(this);
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        DownLoadInfo downLoadInfo = (DownLoadInfo) adapterView.getAdapter().getItem(i);
        if (downLoadInfo != null){

        }
    }

    @Override
    public void onClick(View view) {
        if (view == title_back){
            finish();
        }
    }

    private long currTime = 0;

    private class ProBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_DOWN_CON)){
                DownLoadInfo downLoadInfo = (DownLoadInfo) intent.getSerializableExtra("downLoadInfo");
                if (downLoadInfo != null){
                    int index = 0;
                    for (int i = 0; i < list.size(); i++) {
                        if (downLoadInfo.getId() == list.get(i).getId()){
                            list.get(i).setCompleteSize(downLoadInfo.getCompleteSize());
                            list.get(i).setState(downLoadInfo.getState());
                            index = i;
                            break;
                        }
                    }
                    if (downLoadInfo.getState() == 3){
                        list.remove(index);
                        mDownAdapter.notifyDataSetChanged();
                    } else {
                        if (System.currentTimeMillis() - currTime == 5000){
                            mDownAdapter.notifyDataSetChanged();
                            currTime = System.currentTimeMillis();
                        }
                    }
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
