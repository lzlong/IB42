package com.xm.ib42.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xm.ib42.R;
import com.xm.ib42.entity.DownLoadInfo;
import com.xm.ib42.util.CircleNumberProgress;

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
        TextView down_con_name = mViewHolder.findViewById(R.id.down_con_name);
        CircleNumberProgress down_pro = mViewHolder.findViewById(R.id.down_pro);
        ImageView down_con = mViewHolder.findViewById(R.id.down_con);
        down_con_name.setText(downLoadInfo.getName());
//        down_pro.setMax(downLoadInfo.getFileSize());
        if (downLoadInfo.getFileSize() > 0){
            down_pro.setProgress((int) (downLoadInfo.getCompleteSize()*100/(downLoadInfo.getFileSize())));
        }
        down_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return mViewHolder;
    }
}
