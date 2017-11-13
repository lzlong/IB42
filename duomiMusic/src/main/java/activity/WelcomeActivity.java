package activity;

import com.example.duomimusic.R;
import com.example.duomimusic.R.layout;
import com.example.duomimusic.R.menu;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.Window;

public class WelcomeActivity extends Activity {
	Handler handler=new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent=new Intent(WelcomeActivity.this,SideslipActivity.class);
				startActivity(intent);
				finish();
			}
		},1000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
