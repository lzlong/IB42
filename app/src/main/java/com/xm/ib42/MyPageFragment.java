package com.xm.ib42;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xm.ib42.adapter.MyAdapter;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.dao.AlbumDao;
import com.xm.ib42.dao.AudioDao;
import com.xm.ib42.entity.Album;

import java.util.List;

/**
 * home3
 * @author andye
 *
 */
public class MyPageFragment extends Fragment implements OnClickListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener{

	private MainActivity aty;
	private View convertView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		convertView = inflater.inflate(R.layout.my_page, null);
		return convertView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		aty = (MainActivity) getActivity();
		init(convertView);
	}

	private TextView title_name;
    private ListView my_lv;
	private List<Album> albumList;
	private AlbumDao albumDao;
	private MyAdapter adapter;
    private AudioDao audioDao;
    private PopupWindow deletePop;
    private Button delete_true, delete_cancel;

	private void init(View v) {
        title_name = (TextView) convertView.findViewById(R.id.title_name);
        title_name.setText("收听历史");
        my_lv = (ListView) convertView.findViewById(R.id.my_lv);

        View view = aty.getLayoutInflater().inflate(R.layout.delete_dialog, null);
        delete_true = (Button) view.findViewById(R.id.delete_true);
        delete_cancel = (Button) view.findViewById(R.id.delete_cancel);
        deletePop = new PopupWindow(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        deletePop.setContentView(view);
        deletePop.setFocusable(true);


        audioDao = new AudioDao(aty);
        albumDao = new AlbumDao(aty);
        aty.showLoadDialog(true);
        getData();
        aty.showLoadDialog(false);
        my_lv.setOnItemClickListener(this);
        my_lv.setOnItemLongClickListener(this);
        delete_true.setOnClickListener(this);
        delete_cancel.setOnClickListener(this);
    }

    private void getData() {
        albumList = albumDao.searchAll();
        if (albumList != null){
			if (adapter == null){
				adapter = new MyAdapter(aty, albumList);
				my_lv.setAdapter(adapter);
			}
        }
    }

    /**
	 */
	@Override
	public void onClick(View v) {
        if (v == delete_true){
            if (deletePop.isShowing()){
                deletePop.dismiss();
            }
            if (deleteAlbum != null){
                //删除文件
                //音频记录改为未下载
                audioDao.deleteByAlbum(deleteAlbum.getId());
            }
        } else if (v == delete_cancel){
            if (deletePop.isShowing()){
                deletePop.dismiss();
            }
        }
	}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Album album = (Album) parent.getAdapter().getItem(position);
        if (Constants.playAlbum != null){
            if (Constants.playAlbum.getId() != album.getId()){
                Constants.playAlbum = album;
            }
        } else {
            Constants.playAlbum = album;
        }
        if (Constants.playAlbum != null){
            aty.showLoadDialog(true);
//            Constants.playList.addAll(audioDao.searchByAlbum(Constants.playAlbum.getId()+""));
//            if (Constants.playList != null){
//                aty.mediaPlayerManager.setPlayerFlag(MediaPlayerManager.PLAYERFLAG_WEB);
//                aty.mediaPlayerManager.setPlayerList(aty.audioList);
                aty.mediaPlayerManager.player(Constants.playAlbum.getId());
                aty.showLoadDialog(false);
                aty.changePlay();
//            }
        }
    }

    private Album deleteAlbum;

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        deleteAlbum = (Album) adapterView.getAdapter().getItem(position);
        if (deleteAlbum != null){
            if (!deletePop.isShowing()){
                deletePop.showAtLocation(convertView, Gravity.CENTER, 0, 0);
            }
        }
        return false;
    }
}
