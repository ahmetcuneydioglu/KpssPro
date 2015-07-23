package com.arkay.quizpower;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AboutUsActivity extends Activity  implements OnClickListener{
	
	private TextView txtEmail, txtFacebook, txtTwitter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_about_us);
		
		txtEmail = (TextView)findViewById(R.id.txtEmail);
		txtEmail.setOnClickListener(this);
		txtFacebook = (TextView)findViewById(R.id.txtFacebook);
		txtFacebook.setOnClickListener(this);
		txtTwitter = (TextView)findViewById(R.id.txtTwitter);
		txtTwitter.setOnClickListener(this);
		
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.txtEmail:
			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
		            "mailto",""+getResources().getString(R.string.email_url), null));
			intent.putExtra(Intent.EXTRA_SUBJECT, "EXTRA_SUBJECT");
			startActivity(Intent.createChooser(intent, "Send email..."));
			break;
		case R.id.txtFacebook:
			Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(getResources().getString(R.string.facebook_url)));
			startActivity(browserIntent);
			break;
		case R.id.txtTwitter:
			Intent browserIntents = new Intent("android.intent.action.VIEW", Uri.parse(getResources().getString(R.string.twitter_url)));
			startActivity(browserIntents);
			break;
		}
	}
}
