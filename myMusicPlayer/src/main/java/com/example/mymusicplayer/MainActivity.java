package com.example.mymusicplayer;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;

public class MainActivity extends TabActivity implements OnCheckedChangeListener{
	private TabHost tabhost;
	private RadioGroup radioGroup;
	private RadioButton radioBtn1,radioBtn2;
	private String FLAGONE = "TAB1";
	private String FLAGTWO = "TAB2";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		radioGroup.setOnCheckedChangeListener(this);
		//��һ��������ǩ
		Intent musicListIntent = new Intent(this,MusicActivity.class);
		tabhost = getTabHost();
		
		tabhost.addTab(tabhost.newTabSpec(FLAGONE).setIndicator("�����б�", getResources().
				getDrawable(R.drawable.icon1))
				.setContent(musicListIntent));
		
		//�ڶ���������ǩ
		Intent netMusicIntent = new Intent(this,NetMusicActivity.class);
		tabhost.addTab(tabhost.newTabSpec(FLAGTWO).setIndicator("��������", 
				getResources().getDrawable(R.drawable.icon_2_n)).setContent(netMusicIntent));
	
	}
	
	
	/**
	 * ��radioGroup�ӵ���¼�
	 * @param buttonView
	 * @param isChecked
	 */
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.radioBtn1:
			tabhost.setCurrentTabByTag(FLAGONE);
			break;
		case R.id.radioBtn2:
			tabhost.setCurrentTabByTag(FLAGTWO);
			break;
		}
		
	}
	
}
