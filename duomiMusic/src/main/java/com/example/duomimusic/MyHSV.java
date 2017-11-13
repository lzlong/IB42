package com.example.duomimusic;

import activity.SideslipActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class MyHSV extends HorizontalScrollView{
	private int currentOffset=0;//手点在屏幕上x州的坐标
	private int sumWidth;//获得当前window的宽度
	private int btnWidth;//右边的按钮
	private int txtArgWidth;
	private int appWidth;//全屏

	public MyHSV(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		currentOffset=(int) ev.getRawX();
		sumWidth=this.getMeasuredWidth();//获得当前window的宽度
		SideslipActivity.offset=computeHorizontalScrollOffset();
		if(SideslipActivity.offset==0){
			if (currentOffset <= (sumWidth - btnWidth)) {
				return false;
			} else {
				return super.onTouchEvent(ev);
			}
		} else {
			return super.onTouchEvent(ev);
		}
	}

	public void setBtnWith(int btnWidth) {
		this.btnWidth = btnWidth;
	}

	public void setTxtArgWidth(int txtArgWidth) {
		this.txtArgWidth = txtArgWidth;
	}

	public void setAppWidth(int appWidth) {
		this.appWidth = appWidth;
	}
}
