package com.xm.ib42.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xm.ib42.R;
import com.xm.ib42.entity.Audio;

import java.util.List;

/**
 * Created by long on 17-8-27.
 */

public class DownAudioAdapter extends BaseArrayListAdapter {

    private Context mContext;
    private boolean isDown;

    public DownAudioAdapter(Context context, List<Audio> list) {
        this.mContext = context;
        this.data = list;
    }

    @Override
    public ViewHolder getViewHolder(View convertView, ViewGroup parent, int position) {
        ViewHolder mViewHolder = null;
        final Audio audio = (Audio) this.data.get(position);
        mViewHolder = ViewHolder.get(mContext, convertView, parent, R.layout.downaudio_item);
        TextView audio_name = mViewHolder.findViewById(R.id.audio_name);
        audio_name.setText(audio.getTitle());
        return mViewHolder;
    }
}
