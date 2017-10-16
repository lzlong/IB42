package com.xm.ib42.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xm.ib42.R;
import com.xm.ib42.entity.Album;

import java.util.List;

/**
 * Created by long on 17-8-27.
 */

public class HomeSearchAdapter extends BaseArrayListAdapter {

    private Context mContext;

    public HomeSearchAdapter(Context context, List<Album> list) {
        this.mContext = context;
        this.data = list;
    }

    @Override
    public ViewHolder getViewHolder(View convertView, ViewGroup parent, int position) {
        ViewHolder mViewHolder = null;
        Album album = (Album) this.data.get(position);
        mViewHolder = ViewHolder.get(mContext, convertView, parent, R.layout.home_search_item);
        TextView home_search_name = mViewHolder.findViewById(R.id.home_search_name);
        home_search_name.setText(album.getTitle());
        return mViewHolder;
    }
}
