package com.xm.ib42.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xm.ib42.R;
import com.xm.ib42.entity.Album;
import com.xm.ib42.entity.Column;

import java.util.List;

import static com.xm.ib42.R.id.home_page_item_img;

/**
 * Created by long on 17-8-27.
 */

public class HomeAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<Column> data;
    private Handler handler;

    public HomeAdapter(Context context, List<Column> list, Handler handler) {
        this.mContext = context;
        this.data = list;
        this.handler = handler;
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
        if (data != null){
            return data.size();
        }
        return 0;
    }

    @Override
    public int getChildrenCount(int i) {
        if (data != null
                && data.get(i) != null
                && data.get(i).getAlbumList() != null){
            return data.get(i).getAlbumList().size();
        }
        return 0;
    }

    @Override
    public Object getGroup(int i) {
        if (data != null){
            data.get(i);
        }
        return null;
    }

    @Override
    public Object getChild(int i, int i1) {
        if (data != null
                && data.get(i) != null
                && data.get(i).getAlbumList() != null){
            return data.get(i).getAlbumList().get(i1);
        }
        return null;
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        GroupHolder groupHolder = null;
        if (view == null){
            groupHolder = new GroupHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.home_page_item_head, null);
            groupHolder.home_column_title = (TextView) view.findViewById(R.id.home_column_title);
            groupHolder.home_column_more = (ImageView) view.findViewById(R.id.home_coulmn_more);
            view.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) view.getTag();
        }
        Column column = data.get(i);
        groupHolder.home_column_title.setText(column.getTitle()+"("+column.getTitle()+")");
        groupHolder.home_column_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        ChildHolder holder = null;
        if (view == null){
            holder = new ChildHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.home_page_item, null);
            holder.home_page_item_img = (ImageView) view.findViewById(home_page_item_img);
            holder.home_album_title = (TextView) view.findViewById(R.id.home_album_title);
            holder.home_audio_num = (TextView) view.findViewById(R.id.home_audio_num);
            view.setTag(holder);
        } else {
            holder = (ChildHolder) view.getTag();
        }
        Album album = data.get(i).getAlbumList().get(i1);
        if (album.getImageUrl() != null){
            Glide.with(mContext)
                    .load(album.getImageUrl())
                    .placeholder(R.mipmap.kaiping2)
                    .error(R.mipmap.kaiping2)
                    .override(100, 100)
                    .into(holder.home_page_item_img);
        }
        holder.home_album_title.setText(album.getTitle());
        holder.home_audio_num.setText(album.getAudioNum());
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    class GroupHolder{
        TextView home_column_title;
        ImageView home_column_more;
    }

    class ChildHolder{
        ImageView home_page_item_img;
        TextView home_album_title;
        TextView home_audio_num;
    }
}
