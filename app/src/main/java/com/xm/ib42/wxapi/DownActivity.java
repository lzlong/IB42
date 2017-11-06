package com.xm.ib42.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ListView;

import com.xm.ib42.R;

/**
 * Created by long on 17-11-6.
 */

public class DownActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downlayout);
        initView();
    }

    private ListView down_con_lv;

    private void initView() {
        down_con_lv = (ListView) findViewById(R.id.down_con_lv);
    }
}
