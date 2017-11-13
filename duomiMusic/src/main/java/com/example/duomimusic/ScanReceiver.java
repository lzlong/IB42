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
			builder.setMessage("���Ժ�����ɨ��SDcard��Դ...");
			dialog=builder.create();
			dialog.show();
			dialog.cancel();
		}else if(Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)){
			builder=new AlertDialog.Builder(context);
			builder.setMessage("���Ժ�ɨ��SDcard���");
			dialog=builder.create();
			dialog.show();
			dialog.cancel();
		}
	}
	

}
