package com.xm.ib42.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xm.ib42.R;
import com.xm.ib42.entity.Album;

import java.util.List;


/**
 * Created by long on 17-9-10.
 */

public class DownAdapter extends BaseArrayListAdapter {

    private Context mContext;

    public DownAdapter(Context context, List<Album> list) {
        this.mContext = context;
        this.data = list;
    }

    @Override
    public ViewHolder getViewHolder(View convertView, ViewGroup parent, int position) {
        ViewHolder mViewHolder = null;
        Album album = (Album) this.data.get(position);
        mViewHolder = ViewHolder.get(mContext, convertView, parent, R.layout.down_item);
        ImageView down_item_img = mViewHolder.findViewById(R.id.down_item_img);
        TextView down_item_name = mViewHolder.findViewById(R.id.down_item_name);
        TextView down_item_name2 = mViewHolder.findViewById(R.id.down_item_name2);
        if (album.getYppx() == 0){
            down_item_name.setText(album.getAudioNameDesc());
        } else {
            down_item_name.setText(album.getAudioNameAsc());
        }
        down_item_name2.setText(album.getTitle());
        if (album.getImageUrl() != null){
            Glide.with(mContext)
                    .load(album.getImageUrl())
                    .placeholder(R.mipmap.kaiping2)
                    .error(R.mipmap.kaiping2)
                    .override(100, 100)
                    .into(down_item_img);
        }
        return mViewHolder;
    }


}
