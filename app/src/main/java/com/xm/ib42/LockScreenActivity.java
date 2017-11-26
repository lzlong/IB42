package com.xm.ib42;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * Created by long on 17-11-24.
 */

public class LockScreenActivity extends SwipeBackActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        this.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|               //这个在锁屏状态下
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.play_page);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
