
package com.xm.ib42;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xm.ib42.adapter.DownAudioAdapter;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.dao.AudioDao;
import com.xm.ib42.entity.Album;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by long on 17-11-6.
 */

public class DownActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener{

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
    private DownAudioAdapter mAudioListAdapter;
    private Intent intent;
    private PopupWindow deletePop;
    private Button delete_true, delete_cancel;

    private void initView() {
        title_back = (TextView) findViewById(R.id.title_back);
        title_back.setVisibility(View.VISIBLE);
        title_back.setOnClickListener(this);
        down_con_lv = (ListView) findViewById(R.id.down_con_lv);
        down_con_lv.setOnItemClickListener(this);
        down_con_lv.setOnItemLongClickListener(this);
        mAudioDao = new AudioDao(this);
        mAudioList = new ArrayList<>();

        View view = getLayoutInflater().inflate(R.layout.delete_dialog, null);
        delete_true = (Button) view.findViewById(R.id.delete_true);
        delete_cancel = (Button) view.findViewById(R.id.delete_cancel);
        deletePop = new PopupWindow(LinearLayout.LayoutParams.WRAP_CONTENT, 600);
        deletePop.setContentView(view);
        deletePop.setFocusable(true);
        delete_cancel.setOnClickListener(this);
        delete_true.setOnClickListener(this);

        intent = getIntent();
        mAlbum = (Album) intent.getSerializableExtra("album");
        if (mAlbum != null){
            getData();
        }
    }

    private List<Audio> mAudioList;

    private void getData() {
        mAudioList.clear();
        List<Audio> list = mAudioDao.searchByAlbum(mAlbum.getId()+"");
        if (list != null){
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).isDownFinish()){
                    mAudioList.add(list.get(i));
                }
            }
            mAudioListAdapter = new DownAudioAdapter(this, mAudioList);
            down_con_lv.setAdapter(mAudioListAdapter);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == title_back){
            finish();
        } else if (view == delete_true){
            if (deletePop.isShowing()){
                deletePop.dismiss();
            }
            if (deleteAudio != null){
                //删除文件
                List<Audio> list = new ArrayList<>();
                list.add(deleteAudio);
                Utils.deleteDown(list);
                //音频记录改为未下载
                mAudioDao.updateDownByAudio(deleteAudio.getId());
                getData();
            }
        } else if (view == delete_cancel){
            if (deletePop.isShowing()){
                deletePop.dismiss();
            }
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
        if (Constants.playAlbum.getYppx() == 0){
            Constants.playAlbum.setAudioIdDesc(audio.getId());
            Constants.playAlbum.setAudioNameDesc(audio.getTitle());
        } else {
            Constants.playAlbum.setAudioIdAsc(audio.getId());
            Constants.playAlbum.setAudioNameAsc(audio.getTitle());
        }
//        Constants.playPage = Constants.playList.size() / 10;
        intent.putExtra("position", i);
        setResult(0, intent);
        finish();
    }

    private Audio deleteAudio;

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        deleteAudio = (Audio) adapterView.getAdapter().getItem(i);
        if (deleteAudio != null){
            if (!deletePop.isShowing()){
                deletePop.showAtLocation(getLayoutInflater().inflate(R.layout.downlayout, null), Gravity.CENTER, 0, 0);
            }
        }
        return false;
    }
}
