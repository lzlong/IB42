
package com.xm.ib42;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.xm.ib42.adapter.AudioListAdapter;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.dao.AudioDao;
import com.xm.ib42.entity.Album;
import com.xm.ib42.entity.Audio;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by long on 17-11-6.
 */

public class DownActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downlayout);
        initView();
    }

    private TextView title_back;
    private ListView down_con_lv;
    private Album mAlbum;
    private AudioDao mAudioDao;
    private AudioListAdapter mAudioListAdapter;
    private Intent intent;

    private void initView() {
        title_back = (TextView) findViewById(R.id.title_back);
        title_back.setVisibility(View.VISIBLE);
        title_back.setOnClickListener(this);
        down_con_lv = (ListView) findViewById(R.id.down_con_lv);
        down_con_lv.setOnItemClickListener(this);
        mAudioDao = new AudioDao(this);
        mAudioList = new ArrayList<>();

        intent = getIntent();
        mAlbum = (Album) intent.getSerializableExtra("album");
        if (mAlbum != null){
            getData();
        }
    }

    private List<Audio> mAudioList;

    private void getData() {
        List<Audio> list = mAudioDao.searchByAlbum(mAlbum.getId()+"");
        if (list != null){
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).isDownFinish()){
                    mAudioList.add(list.get(i));
                }
            }
            mAudioListAdapter = new AudioListAdapter(this, mAudioList);
            down_con_lv.setAdapter(mAudioListAdapter);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == title_back){
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//        if (Constants.playAlbum != null){
//            if (Constants.playAlbum.getId() != mAlbum.getId()){
//                Constants.playAlbum = mAlbum;
//            }
//        } else {
//        }
        Constants.playAlbum = mAlbum;
        Constants.playList.clear();
        Constants.playList.addAll(mAudioList);
        Audio audio = (Audio) adapterView.getAdapter().getItem(i);
        Constants.playAlbum.setAudioId(audio.getId());
        Constants.playAlbum.setAudioName(audio.getTitle());
//        Constants.playPage = Constants.playList.size() / 10;
        intent.putExtra("position", i);
        setResult(0, intent);
        finish();
    }
}
