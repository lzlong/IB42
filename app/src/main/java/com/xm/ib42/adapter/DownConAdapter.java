package com.xm.ib42.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xm.ib42.R;
import com.xm.ib42.entity.DownLoadInfo;

import java.util.List;

/**
 * Created by long on 17-11-6.
 */

public class DownConAdapter extends BaseArrayListAdapter<DownLoadInfo> {

    private Context mContext;

    public DownConAdapter(Context context, List<DownLoadInfo> list) {
        mContext = context;
        this.data = list;
    }

    @Override
    public ViewHolder getViewHolder(View convertView, ViewGroup parent, int position) {
        ViewHolder mViewHolder = null;
        DownLoadInfo downLoadInfo = (DownLoadInfo) this.data.get(position);
        mViewHolder = ViewHolder.get(mContext, convertView, parent, R.layout.down_con_item);
        ImageView down_con_img = mViewHolder.findViewById(R.id.down_con_img);
        TextView down_con_name = mViewHolder.findViewById(R.id.down_con_name);
        TextView down_con_name2 = mViewHolder.findViewById(R.id.down_con_name2);
        ImageView down_con = mViewHolder.findViewById(R.id.down_con);
        down_con_name.setText(downLoadInfo.getName());
        down_con_name2.setText(downLoadInfo.getAlbum());
//        if (album.getImageUrl() != null){
//            Glide.with(mContext)
//                    .load(album.getImageUrl())
//                    .placeholder(R.mipmap.kaiping2)
//                    .error(R.mipmap.kaiping2)
//                    .override(100, 100)
//                    .into(down_con_img);
//        }
        down_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return mViewHolder;
    }
}
