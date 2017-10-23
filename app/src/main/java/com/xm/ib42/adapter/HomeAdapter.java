package com.xm.ib42.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xm.ib42.R;
import com.xm.ib42.entity.Album;

import java.util.List;

/**
 * Created by long on 17-8-27.
 */

public class HomeAdapter extends BaseExpandableListAdapter {

    private Context mContext;

    public HomeAdapter(Context context, List<Album> list) {
        this.mContext = context;
        this.data = list;
    }

//    @Override
//    public BaseArrayListAdapter.ViewHolder getViewHolder(View convertView, ViewGroup parent, int position) {
//        BaseArrayListAdapter.ViewHolder mViewHolder = null;
//        Album album = (Album) this.data.get(position);
//        mViewHolder = BaseArrayListAdapter.ViewHolder.get(mContext, convertView, parent, R.layout.home_page_item);
//        ImageView home_page_item_img = mViewHolder.findViewById(R.id.home_page_item_img);
//        TextView home_page_item_tv = mViewHolder.findViewById(R.id.home_page_item_tv);
//        home_page_item_tv.setText(album.getTitle());
//        if (album.getImageUrl() != null){
//            Glide.with(mContext)
//                    .load(album.getImageUrl())
//                    .placeholder(R.mipmap.kaiping2)
//                    .error(R.mipmap.kaiping2)
//                    .override(100, 100)
//                    .into(home_page_item_img);
//        }
//        return mViewHolder;
//    }

    @Override
    public int getGroupCount() {
        return 0;
    }

    @Override
    public int getChildrenCount(int i) {
        return 0;
    }

    @Override
    public Object getGroup(int i) {
        return null;
    }

    @Override
    public Object getChild(int i, int i1) {
        return null;
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public long getChildId(int i, int i1) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }
}
