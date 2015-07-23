package com.arkay.quizpower;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;

/**
 * Setting screen activity for user user sound and vibration. also on this app we use for Rate me and Share me functionality.
 * 
 * @author I-BALL
 *
 */
public class SettingActivity extends Activity implements OnClickListener {

	private ToggleButton toggleSoundEffect,
			toggleVibration;
	private Button btnShareMe, btnRateMe;


	private boolean isSoundEffect, isVibration;
	SharedPreferences settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		settings = getSharedPreferences(MenuHomeScreenActivity.PREFS_NAME, 0);

		isSoundEffect = settings.getBoolean(
				MenuHomeScreenActivity.SOUND_EFFECT, true);
		isVibration = settings.getBoolean(MenuHomeScreenActivity.VIBRATION,
				true);

		toggleSoundEffect = (ToggleButton) findViewById(R.id.toggleSoundEffect);
		toggleSoundEffect.setOnClickListener(this);
		toggleSoundEffect.setText(null);
		toggleSoundEffect.setChecked(isSoundEffect);

		toggleVibration = (ToggleButton) findViewById(R.id.toggleVibration);
		toggleVibration.setOnClickListener(this);
		toggleVibration.setText(null);
		toggleVibration.setChecked(isVibration);

		btnShareMe = (Button) findViewById(R.id.btnShareMe);
		btnShareMe.setOnClickListener(this);
		btnRateMe = (Button) findViewById(R.id.btnRateMe);
		btnRateMe.setOnClickListener(this);
	}

	public void onClick(View v) {
		Log.i("info", "Start");

		switch (v.getId()) {
		case R.id.toggleSoundEffect:
			SharedPreferences.Editor editor = settings.edit();
			Log.i("info", "" + toggleSoundEffect.isChecked());
			editor = settings.edit();
			editor.putBoolean(MenuHomeScreenActivity.SOUND_EFFECT,
					toggleSoundEffect.isChecked());
			editor.commit();
			break;
		case R.id.toggleVibration:
			Log.i("info", "" + toggleVibration.isChecked());
			editor = settings.edit();
			editor.putBoolean(MenuHomeScreenActivity.VIBRATION,
					toggleVibration.isChecked());
			editor.commit();
			break;
		case R.id.btnRateMe:
			String appPackageName = getPackageName();
			Intent marketIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id=" + appPackageName));
			marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
					| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			startActivity(marketIntent);
			break;
		case R.id.btnShareMe:
			startSendMailActivity();
			break;

		}
	}

	public void startSendMailActivity() {

		try {
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_SUBJECT,
					"" + getResources().getString(R.string.app_name));
			String sAux = "\nLet me recommend you this application\n\n";
			sAux = sAux + "https://play.google.com/store/apps/details?id="
					+ getPackageName() + " \n\n";
			i.putExtra(Intent.EXTRA_TEXT, sAux);
			startActivity(Intent.createChooser(i, "choose one"));
		} catch (Exception e) { // e.toString();
		}
	}

}
