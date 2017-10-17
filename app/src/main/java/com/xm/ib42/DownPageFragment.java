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

import com.xm.ib42.adapter.DownAdapter;
import com.xm.ib42.constant.Constants;
import com.xm.ib42.dao.AlbumDao;
import com.xm.ib42.dao.AudioDao;
import com.xm.ib42.entity.Album;
import com.xm.ib42.entity.Audio;
import com.xm.ib42.service.MediaPlayerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * home4
 * 
 * @author andye
 * 
 */
public class DownPageFragment extends Fragment implements OnClickListener, AdapterView.OnItemClickListener,
		AdapterView.OnItemLongClickListener {
	private MainActivity aty;
	private View convertView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		convertView = inflater.inflate(R.layout.down_page, null);
		return convertView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		aty = (MainActivity) getActivity();
		init(convertView);
	}

	private TextView title_name;
	private ListView down_lv;
    private DownAdapter adapter;
    private PopupWindow deletePop;
    private Button confirm, cancel;

	private void init(View v) {
		title_name = (TextView) convertView.findViewById(R.id.title_name);
		title_name.setText("离线");
		down_lv = (ListView) convertView.findViewById(R.id.down_lv);

        View view = aty.getLayoutInflater().inflate(R.layout.deletepop, null);
        confirm = (Button) view.findViewById(R.id.confirm);
        cancel = (Button) view.findViewById(R.id.cancel);
        deletePop = new PopupWindow(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        deletePop.setContentView(view);
        deletePop.setFocusable(true);


        down_lv.setOnItemClickListener(this);
        down_lv.setOnItemLongClickListener(this);
        confirm.setOnClickListener(this);
        cancel.setOnClickListener(this);
        audioDao = new AudioDao(aty);
        albumDao = new AlbumDao(aty);
		getData();
	}

    private AudioDao audioDao;
    private AlbumDao albumDao;
	private List<Audio> audioList;
    private Map<Integer, Album> albumMap;
	private List<Album> albumList;
	private List<Album> albumDownList;
	/**
	 * 查询数据库中标记为下载的音频
	 */
	private void getData() {
        audioList = audioDao.searchByDownLoad();
		albumList = new ArrayList<>();
        albumMap = new HashMap<>();
		for (int i = 0; audioList != null && i < audioList.size(); i++) {
			Audio audio = audioList.get(i);
            if (!albumMap.containsKey(audio.getAlbum().getId())){
//				albumMap.put(audio.getAlbum().getId(), audio.getAlbum());
				Album album = albumDao.searchById(audio.getAlbum().getId());
				albumList.add(album);
			} else {
			}
		}
//		albumList.addAll(albumMap.values());
//        albumDownList = new ArrayList<>();
//        for (int i = 0; i < albumList.size(); i++) {
//            Album album = albumList.get(i);
//            album = albumDao.searchById(album.getId());
//        }
        if (adapter == null){
            adapter = new DownAdapter(aty, albumList);
            down_lv.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

	@Override
	public void onClick(View v) {
        if (v == confirm){
            if (deletePop.isShowing()){
                deletePop.dismiss();
            }
            if (deleteAlbum != null){
                audioDao.deleteByAlbum(deleteAlbum.getId());
            }
        } else if (v == cancel){
            if (deletePop.isShowing()){
                deletePop.dismiss();
            }
        }
	}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Constants.playAlbum = (Album) parent.getAdapter().getItem(position);
        if (Constants.playAlbum != null){
            aty.showLoadDialog(true);
            Constants.playList.addAll(audioDao.searchByAlbum(Constants.playAlbum.getId()+""));
			if (Constants.playList != null){
                aty.mediaPlayerManager.setPlayerFlag(MediaPlayerManager.PLAYERFLAG_WEB);
//				aty.mediaPlayerManager.setPlayerList(Constants.audioList);
                aty.mediaPlayerManager.player(Constants.playAlbum.getId());
                aty.showLoadDialog(false);
                aty.changePlay();
			}
        }

    }

    private Album deleteAlbum;

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        deleteAlbum = (Album) parent.getAdapter().getItem(position);
        if (deleteAlbum != null){
            if (!deletePop.isShowing()){
                deletePop.showAtLocation(convertView, Gravity.CENTER, 0, 0);
            }
        }
		return false;
	}
}
