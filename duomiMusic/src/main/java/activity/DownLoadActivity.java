package activity;

import com.example.duomimusic.R;
import com.example.duomimusic.R.id;
import com.example.duomimusic.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class DownLoadActivity extends Activity implements OnClickListener{
	ImageButton btn_down_back;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download);
		findViewById(R.id.btn_local_download).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		startActivity(new Intent(this, SideslipActivity.class));
	}

}
