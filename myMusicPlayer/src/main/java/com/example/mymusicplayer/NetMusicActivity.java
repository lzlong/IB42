package com.example.mymusicplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class NetMusicActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.net);
	}
}
