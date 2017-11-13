package com.example.duomimusic;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScanReceiver extends BroadcastReceiver{
	private AlertDialog.Builder builder=null;
	private AlertDialog dialog=null;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action=intent.getAction();
		if(intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)){
			builder=new AlertDialog.Builder(context);
			builder.setMessage("请稍后，正在扫描SDcard资源...");
			dialog=builder.create();
			dialog.show();
			dialog.cancel();
		}else if(Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)){
			builder=new AlertDialog.Builder(context);
			builder.setMessage("请稍后，扫描SDcard完成");
			dialog=builder.create();
			dialog.show();
			dialog.cancel();
		}
	}
	

}
