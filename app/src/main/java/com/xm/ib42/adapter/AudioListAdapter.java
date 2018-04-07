package com.xm.ib42.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.xm.ib42.AudioListActivity;
import com.xm.ib42.R;
import com.xm.ib42.entity.Audio;

import java.util.List;

/**
 * Created by long on 17-8-27.
 */

public class AudioListAdapter extends BaseArrayListAdapter {

    private Context mContext;
    private boolean isDown;
    private AudioListActivity mAudioListActivity;

    public AudioListAdapter(Context context, List<Audio> list, AudioListActivity mAudioListActivity) {
        this.mContext = context;
        this.data = list;
        this.mAudioListActivity = mAudioListActivity;
    }

    public void setDown(boolean down) {
        isDown = down;
    }

    public boolean isDown() {
        return isDown;
    }

    @Override
    public ViewHolder getViewHolder(View convertView, ViewGroup parent, int position) {
        ViewHolder mViewHolder = null;
        final Audio audio = (Audio) this.data.get(position);
        mViewHolder = ViewHolder.get(mContext, convertView, parent, R.layout.audiolist_item);
        TextView audio_name = mViewHolder.findViewById(R.id.audio_name);
        ImageView audio_share = mViewHolder.findViewById(R.id.audio_share);
        CheckBox audio_check = mViewHolder.findViewById(R.id.audio_down);
        if (isDown){
            audio_check.setVisibility(View.VISIBLE);
            audio_share.setVisibility(View.GONE);
        } else {
            audio_share.setVisibility(View.VISIBLE);
            audio_check.setVisibility(View.GONE);
        }
        if (audio.isCheck()){
            audio_check.setChecked(true);
        } else {
            audio_check.setChecked(false);
        }
        audio_check.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (audio.isCheck()){
                    audio.setCheck(false);
                } else {
                    audio.setCheck(true);
                }
                notifyDataSetChanged();
            }

        });
        audio_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAudioListActivity.showPop(audio);
            }
        });
        audio_name.setText(audio.getTitle());
        return mViewHolder;
    }
}
