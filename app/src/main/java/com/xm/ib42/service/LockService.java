package com.xm.ib42.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.xm.ib42.LockScreenActivity;

/**
 * Created by long on 17-11-24.
 */

public class LockService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == Intent.ACTION_SCREEN_OFF) {
                    System.out.println("收到锁屏广播");
                    Intent lockscreen = new Intent(LockService.this, LockScreenActivity.class);
                    lockscreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(lockscreen);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(receiver, filter);
    }
}
