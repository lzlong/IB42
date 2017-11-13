package com.example.mymusicplayer;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.util.Log;

/**
 * ɨ��SD���Ĺ㲥�������������յ��Ĺ㲥��ACTION_MEDIA_SCANNER_STARTEDʱ��˵��SD����
 * ɨ��״̬����ʾ��ʾ�Ի��򣬸����û�ϵͳ״̬�������յ��Ĺ㲥��ACTION_MEDIA_SCANNER_FINISHEDʱ��
 * ˵��SD���Ѿ�ɨ���������Խ���ʾ�Ի���ȡ��
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
			Log.i("Log", "SD������ɨ���У����Ե�");
			builder = new AlertDialog.Builder(context);
			builder.setMessage("����ɨ��SD��");
			dialog = builder.create();
			dialog.show();
		}else if(intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)){
			dialog.cancel();
			intent.setAction(intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			
		}
	}
	
}
