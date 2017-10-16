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
 * Created by long on 17-8-27.
 */

public class HomeAdapter extends BaseArrayListAdapter {

    private Context mContext;

    public HomeAdapter(Context context, List<Album> list) {
        this.mContext = context;
        this.data = list;
    }

    @Override
    public ViewHolder getViewHolder(View convertView, ViewGroup parent, int position) {
        ViewHolder mViewHolder = null;
        Album album = (Album) this.data.get(position);
        mViewHolder = ViewHolder.get(mContext, convertView, parent, R.layout.home_page_item);
        ImageView home_page_item_img = mViewHolder.findViewById(R.id.home_page_item_img);
        TextView home_page_item_tv = mViewHolder.findViewById(R.id.home_page_item_tv);
        home_page_item_tv.setText(album.getTitle());
        if (album.getImageUrl() != null){
            Glide.with(mContext)
                    .load(album.getImageUrl())
                    .placeholder(R.mipmap.kaiping2)
                    .error(R.mipmap.kaiping2)
                    .override(100, 100)
                    .into(home_page_item_img);
        }
        return mViewHolder;
    }
}
