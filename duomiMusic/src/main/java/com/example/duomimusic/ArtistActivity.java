package com.example.duomimusic;

import activity.SideslipActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class ArtistActivity extends Activity implements OnClickListener{
	ImageButton btn_singback;//歌手列表中返回按钮
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.artist);
		findViewById(R.id.singer_back).setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		startActivity(new Intent(this,SideslipActivity.class));
	}

}
