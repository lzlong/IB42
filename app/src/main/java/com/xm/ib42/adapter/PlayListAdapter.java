package com.xm.ib42.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xm.ib42.R;
import com.xm.ib42.entity.Audio;

import java.util.List;


/**
 * Created by long on 17-9-10.
 */

public class PlayListAdapter extends BaseArrayListAdapter {

    private Context mContext;
    private int playId;

    public PlayListAdapter(Context context, List<Audio> list) {
        this.mContext = context;
        this.data = list;
    }

    @Override
    public ViewHolder getViewHolder(View convertView, ViewGroup parent, int position) {
        ViewHolder mViewHolder = null;
        Audio audio = (Audio) this.data.get(position);
        mViewHolder = ViewHolder.get(mContext, convertView, parent, R.layout.play_list_item);
        TextView play_list_tv = mViewHolder.findViewById(R.id.play_list_tv);
        if (playId == audio.getId()){
            play_list_tv.setTextColor(Color.RED);
        } else {
            play_list_tv.setTextColor(Color.BLACK);
        }
        play_list_tv.setText(audio.getTitle());
        return mViewHolder;
    }


    public void setPlayId(int playId){
        this.playId = playId;
        notifyDataSetChanged();
    }

}
