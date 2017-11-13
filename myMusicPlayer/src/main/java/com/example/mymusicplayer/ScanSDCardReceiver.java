package com.example.mymusicplayer;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.util.Log;

/**
 * 扫描SD卡的广播接收器，当接收到的广播是ACTION_MEDIA_SCANNER_STARTED时，说明SD卡在
 * 扫描状态，显示提示对话框，告诉用户系统状态。当接收到的广播是ACTION_MEDIA_SCANNER_FINISHED时，
 * 说明SD卡已经扫描完了所以将提示对话框取消
 * @author pc
 *
 */
public class ScanSDCardReceiver extends BroadcastReceiver{
	private AlertDialog.Builder builder = null;
	private AlertDialog dialog =null; 
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)){
			Log.i("Log", "SD卡正在扫描中，请稍等");
			builder = new AlertDialog.Builder(context);
			builder.setMessage("正在扫描SD卡");
			dialog = builder.create();
			dialog.show();
		}else if(intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)){
			dialog.cancel();
			intent.setAction(intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			
		}
	}
	
}
