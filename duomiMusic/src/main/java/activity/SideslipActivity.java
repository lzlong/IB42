package activity;


import com.example.duomimusic.ArtistActivity;
import com.example.duomimusic.LocalMusicActivity;
import com.example.duomimusic.MyHSV;
import com.example.duomimusic.PlayMusicActivity;
import com.example.duomimusic.R;
import com.example.duomimusic.R.drawable;
import com.example.duomimusic.R.id;
import com.example.duomimusic.R.layout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;

public class SideslipActivity extends Activity implements OnClickListener {
	
	
	private AlertDialog.Builder builder = null;
	private AlertDialog dialog = null;
	public static MyHSV myHSV;
	/** 滚动条的子布局对象 **/
	private LinearLayout right;
	/** 第二页布局视图对象 **/
	private View app;
	/** 保存左边布局的宽度 */
	int leftWidth;
	int rightWidth;// 右边布局的宽度
	/** 滚动参数 **/
	private boolean flagMove = false;
	public static int offset = 0;// 偏移量
	private LayoutInflater inflater;
	/** View数组，加入子视图 **/
	private View[] children;
	ScrollView scrollView;
	ImageButton btn_sideslip;
	int change = 0;//
	/**
	 * sideslipMyMusic:sliding中的我的音乐*;local_myMusic:mumusic中我的音乐--local_artist:
	 * mymusic中的歌手
	 */
	RelativeLayout sideslipMyMusic, local_myMusic, local_artist, local_album,
			local_downLoad, local_myLove;
	LinearLayout sidesing_about,myMusic_play_bar, sidesing_exit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sliding);
		inflater = LayoutInflater.from(this);
		app = inflater.inflate(R.layout.mymusic, null);// 第二布局

		sideslipMyMusic = (RelativeLayout) findViewById(R.id.rl_sliding_mynusic);
		local_myMusic = (RelativeLayout) app.findViewById(R.id.rl_local_music);
		btn_sideslip = (ImageButton) app.findViewById(R.id.btn_sideslip);
		local_artist = (RelativeLayout) app.findViewById(R.id.rl_mymusic_artist);
		local_album = (RelativeLayout) app.findViewById(R.id.rl_mymusic_album);
		local_downLoad = (RelativeLayout) app.findViewById(R.id.rl_mymusic_download);
		local_myLove = (RelativeLayout) app.findViewById(R.id.rl_mymusic_love);
		sidesing_about=(LinearLayout) findViewById(R.id.sliding_about);
		myMusic_play_bar=(LinearLayout) app.findViewById(R.id.ll_play_bar);
		sidesing_exit=(LinearLayout) findViewById(R.id.sliding_exit);
		
		
		
		right = (LinearLayout) findViewById(R.id.top);// 第二布局中的linearlaout
		myHSV = (MyHSV) findViewById(R.id.myHSV);// 自定义
		// 第一布局的linearlayout
		scrollView = (ScrollView) findViewById(R.id.scrollview);// 黑色部分布局
		// 动态生成一个TextView
		LinearLayout txtArg = new LinearLayout(this);// 透明部分
		children = new View[] { txtArg, app };
		btn_sideslip.setOnClickListener(this);
		sideslipMyMusic.setOnClickListener(this);
		local_myMusic.setOnClickListener(this);
		local_artist.setOnClickListener(this);
		local_album.setOnClickListener(this);
		local_downLoad.setOnClickListener(this);
		local_myLove.setOnClickListener(this);
		sidesing_about.setOnClickListener(this);
		myMusic_play_bar.setOnClickListener(this);
		sidesing_exit.setOnClickListener(this);
	}

	/**
	 * 窗口获得焦点（手指按到屏幕） 窗口失去焦点（手指离开屏幕）
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// super.onWindowFocusChanged(hasFocus);
		if (change == 0) {
			onGlobalLayout();
			change++;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 初始化滚动条并计算相应View对象的宽高
	 */
	public void onGlobalLayout() {

		/**
		 * 获得左边布局的宽度 getWidth(): View在设定好布局后整个View的宽度。 getMeasuredWidth():
		 * 对View上的内容进行测量后得到的View内容占据的宽度，
		 * 前提是你必须在父布局的onLayout()方法或者此View的onDraw()方法里调用measure(0,0);
		 * (measure中的参数的值你自己可以定义)，否则你得到的结果和getWidth()得到的结果是一样的。
		 */
		leftWidth = scrollView.getMeasuredWidth();// 最底层的布局
		final int w = myHSV.getMeasuredWidth();// 全屏的宽度
		final int h = myHSV.getMeasuredHeight();
		rightWidth = w - leftWidth;// 获得剩余部分的宽度
		Log.d("Tag", "leftwidth--" + leftWidth + ";w--" + w);
		// right.removeAllViews();
		// 0-->w 1-->h
		int[] dims = new int[2];
		for (int i = 0; i < children.length; i++) {
			getViewSize(i, w, h, dims);
			right.addView(children[i], dims[0], dims[1]);
		}
		myHSV.setBtnWith(rightWidth);
		myHSV.setAppWidth(w);
	}

	/**
	 * 获取各个View视图的宽高 0宽 1高
	 */
	public void getViewSize(int idx, int w, int h, int[] dims) {
		dims[0] = w;
		dims[1] = h;
		final int menuIdx = 0;
		if (idx == menuIdx) {
			dims[0] = w - rightWidth;
		}
		System.out.println("idx---" + idx + "------w---" + dims[0]
				+ "------h----" + dims[1]);

	}

	/**
	 * 顶部按钮左右移动
	 * 
	 * @param width
	 */
	public void moveScrollList(int width) {
		int menuWidth = width;// 273

		if (flagMove) {
			// Scroll to 0 to reveal menu
			offset = 0;
			myHSV.smoothScrollTo(offset, 0);
		} else {
			// Scroll to menuWidth so menu isn't on screen.
			offset = menuWidth;
			myHSV.smoothScrollTo(offset, 0);
		}
		flagMove = !flagMove;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_sideslip:
			int width = children[0].getWidth();
			moveScrollList(width);
			break;
		case R.id.rl_sliding_mynusic:
			int width1 = children[0].getWidth();
			moveScrollList(width1);
			break;
		case R.id.rl_local_music:
			startActivity(new Intent(this, LocalMusicActivity.class));
			break;
		case R.id.rl_mymusic_artist:
			startActivity(new Intent(this, ArtistActivity.class));
		case R.id.rl_mymusic_album:
			startActivity(new Intent(this, AlbumActivity.class));
			break;
		case R.id.rl_mymusic_download:
			startActivity(new Intent(this, DownLoadActivity.class));
			break;
		case R.id.rl_mymusic_love:
			startActivity(new Intent(this, MyLoveActivity.class));
			break;
		case R.id.sliding_about:
			startActivity(new Intent(this, AboutMusicActivity.class));
			break;
		case R.id.ll_play_bar:
			startActivity(new Intent(this, PlayMusicActivity.class));
			break;
		case R.id.sliding_exit:
			AlertDialog.Builder builder=new AlertDialog.Builder(SideslipActivity.this);
			builder.setTitle("退出软件");
			builder.setIcon(R.drawable.notification);
			builder.setMessage("确定要退出程序吗？").
			setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			}).setNegativeButton("取消", null).show();

		}
	}

}
