package com.ub.location;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
/**
 * Main class : to provide user various options.
 * @author vikram
 *
 */
public class LocationTimerActivity extends Activity implements OnClickListener   {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);

		View trackMeButton = findViewById(R.id.track_me_button);
		trackMeButton.setOnClickListener(this);

		View settingButton = findViewById(R.id.setting_button);
		settingButton.setOnClickListener(this);

		View lastTrackButton = findViewById(R.id.last_track_button);
		lastTrackButton.setOnClickListener(this);

		View aboutButton = findViewById(R.id.about_button);
		aboutButton.setOnClickListener(this);

		View exitButton = findViewById(R.id.exit_button);
		exitButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		Intent intent;
		switch (arg0.getId()) {
		case R.id.track_me_button:
			intent = new Intent(this,Track.class);
			startActivity(intent);
			break;	
		case R.id.last_track_button:

			break;
		case R.id.about_button:
			intent = new Intent(this,About.class);
			startActivity(intent);
			break;
		case R.id.setting_button:
			intent = new Intent(this,Setting.class);
			startActivity(intent);
			break;

		case R.id.exit_button:
			finish();
		default:
			break;
		}

	}

}