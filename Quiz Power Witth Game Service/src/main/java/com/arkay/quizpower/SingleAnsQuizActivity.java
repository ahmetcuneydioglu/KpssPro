package com.arkay.quizpower;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.arkay.quizpower.bean.Question;
import com.arkay.quizpower.bean.QuestionHandler;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Single Answare activtiy that display question and next, prev button and show next button. 
 * @author Arkay
 *
 */
public class SingleAnsQuizActivity extends Activity implements OnClickListener, OnTouchListener{
	private float x1 = 0, x3=0;
	private float moveCount;
	private ScrollView mainlayout;
	
	private Button btnNext, btnPrev, btnShowAnsware, btnJump;
	private TextView txtQuestion, txtAnsware, txtMenu, txtQuestionNO;
	private int currentQuestion=0;
	private QuestionHandler questionHandler;
	private int totalQuestion=0;
	AdView adView;
	private ProgressDialog progress;
	private InterstitialAd interstitial;
	String questionJson = "";
	private List<Question> playQuizquestions =null;
	 boolean isTestMode= false;
	int levelNo=1;
	RelativeLayout playQuizmainLayout, top_panel, layout_bottom;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_quiz);
		txtQuestion = (TextView)findViewById(R.id.txtQuestion);
		txtAnsware = (TextView)findViewById(R.id.txtAnsware);
		txtMenu = (TextView)findViewById(R.id.txtMenu);
		txtMenu.setOnClickListener(this);
		txtQuestionNO = (TextView)findViewById(R.id.txtQuestionNO);
		
		btnNext = (Button)findViewById(R.id.btnNext);
		btnNext.setOnClickListener(this);
		btnPrev = (Button)findViewById(R.id.btnPrev);
		btnPrev.setOnClickListener(this);
		btnShowAnsware = (Button)findViewById(R.id.btnShowAnsware);
		btnShowAnsware.setOnClickListener(this);
		
		btnJump = (Button)findViewById(R.id.btnJump);
		btnJump.setOnClickListener(this);
		
		playQuizmainLayout = (RelativeLayout)findViewById(R.id.playQuizmainLayout);
		top_panel = (RelativeLayout)findViewById(R.id.top_panel);
		layout_bottom = (RelativeLayout)findViewById(R.id.layout_bottom);
		
		Intent intent = this.getIntent();
		levelNo = intent.getExtras().getInt("level_no");
		Resources ress = getResources();
		boolean isQuestionFromWeb = ress.getBoolean(R.bool.isQuestionFormWeb); 
		if(isQuestionFromWeb){
			progress = new ProgressDialog(this);
	        progress.setTitle("Please Wait!!");
	        progress.setMessage("Data Loading..");
	        progress.setCancelable(false);
	        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        progress.show();
			LoadQuestions task = new LoadQuestions();
			task.execute(new String[] { ress.getString(R.string.get_question_right_answare)+levelNo });
		}else{
			questionHandler = new QuestionHandler(getPackageName(),levelNo);
			nextQuizQuestion();
		}
		
		mainlayout=(ScrollView)findViewById(R.id.scroll_layout);
		mainlayout.setOnTouchListener(this);
		
		
		// Create an ad
		adView = new AdView(this);
	    adView.setAdSize(AdSize.SMART_BANNER);
	    adView.setAdUnitId(getString(R.string.admob_banner));
	    
	    // Add the AdView to the view hierarchy. The view will have no size until the ad is loaded.
	    LinearLayout layout = (LinearLayout) findViewById(R.id.ads_layout);
	    layout.addView(adView);

	    // Create ad request.
	    isTestMode = ress.getBoolean(R.bool.istestmode);
	    AdRequest adRequest =null;
	    if(isTestMode){
	    	 // Request for Ads
	    	 System.out.println("Testing.... app");
	          adRequest = new AdRequest.Builder()
	         .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
	         .addTestDevice("0C2DF43E6E70766851B6A3E5EE46A9B8")
	                .build();
	    }else{
	    	System.out.println("Live Apps");
	    	 adRequest = new AdRequest.Builder().build();
	    }
	    
	    // Start loading the ad in the background.
	    adView.loadAd(adRequest);

	    // Create the interstitial.
	    interstitial = new InterstitialAd(this);
	    interstitial.setAdUnitId(getString(R.string.admob_intersitital));
	    // Begin loading your interstitial.
	    interstitial.loadAd(adRequest);

	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.btnNext:
			nextButtonClick();
			changeBackColor();
			break;
		case R.id.btnPrev:
			prevButtonClick();
			changeBackColor();
			break;
		case R.id.txtMenu:
			finish();
			break;
		case R.id.btnShowAnsware:
			txtAnsware.setText(questionHandler.lstQuestions.get(currentQuestion).getAnsware());
			break;
		case R.id.btnJump:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);
			input.setKeyListener(new DigitsKeyListener());

			alert.setView(input);

			alert.setTitle("Jump Question: ").setMessage("Question No:").setPositiveButton(
					"Go", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Editable go = input.getText();
							try{
								int tt = Integer.parseInt(go.toString().trim());
								currentQuestion = tt;
								currentQuestion = currentQuestion -2;
								nextButtonClick();
							}catch(NumberFormatException nfe){
								
							}
							
						}
					}).setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.cancel();
						}
					}).show();
			break;
		}
	}
	
	public void changeBackColor(){
		
		playQuizmainLayout.setBackgroundColor(getResources().getColor(R.color.app_background_color_1));
		top_panel.setBackgroundColor(getResources().getColor(R.color.app_background_color_1_1));
		btnShowAnsware.setBackgroundResource(R.drawable.option_button_color_1);
		btnShowAnsware.setTextColor(getResources().getColor(R.color.app_background_color_1));
		layout_bottom.setBackgroundColor(getResources().getColor(R.color.app_background_color_1_1));
	}
	
	private void nextButtonClick(){
		currentQuestion++;
		if(currentQuestion<totalQuestion){
			txtQuestion.setText(questionHandler.lstQuestions.get(currentQuestion).getQuestion());
			txtAnsware.setText("");
			if(currentQuestion%20==0){
				displayInterstitial();
			}
		}else{
			currentQuestion--;
			
		}
		txtQuestionNO.setText(""+(currentQuestion + 1)+"/"+totalQuestion);
	}
	private void prevButtonClick(){
		currentQuestion--;
		if(currentQuestion>=0){
			
			txtQuestion.setText(questionHandler.lstQuestions.get(currentQuestion).getQuestion());
			txtAnsware.setText("");
		}else{
			currentQuestion++;
			
		}
		txtQuestionNO.setText(""+(currentQuestion + 1)+"/"+totalQuestion);
	}
	 // Invoke displayInterstitial() when you are ready to display an interstitial.
	  public void displayInterstitial() {
	    if (interstitial.isLoaded()) {
	      interstitial.show();
	      
	      // Create the interstitial.
		    interstitial = new InterstitialAd(this);
		    interstitial.setAdUnitId(getString(R.string.admob_intersitital));

		    AdRequest adRequest =null;
		    if(isTestMode){
		    	 // Request for Ads
		    	 System.out.println("Testing.... app");
		          adRequest = new AdRequest.Builder()
		         .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		         .addTestDevice("B15149A4EC1ED23173A27B04134DD483")
		                .build();
		    }else{
		    	System.out.println("Live Apps");
		    	 adRequest = new AdRequest.Builder().build();
		    }
		    // Begin loading your interstitial.
		    interstitial.loadAd(adRequest);
	    }
	  }

	public boolean touchChangeQuote(View v,MotionEvent event) {
		switch (event.getAction()){
		
		case MotionEvent.ACTION_DOWN:
			x1 = event.getX();
			break;
		
		case MotionEvent.ACTION_UP:
			x3 = event.getX();
			if(x1 < x3 ){
				moveCount=x3-x1;
				if(moveCount>150){
					System.out.println("Previous");
					prevButtonClick();
				}
				
			}
			else if(x1 > x3)
			{
				moveCount=x1-x3;
				if(moveCount>150){
					System.out.println("Next");
					nextButtonClick();
				}
					
			}
			
			break;
			
	}
		return false;
		
	}
	

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()){
		case R.id.scroll_layout :
			touchChangeQuote(v,event);
			return true;
		
		default:
			break;
		}
		
	 return false;
	}
	
	private class LoadQuestions extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			
			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try {
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();

					BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						questionJson += s;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					playQuizquestions = new ArrayList<Question>();
					JSONArray users = new JSONArray(questionJson);
					for (int i = 0; i < users.length(); i++) {
						JSONObject c = users.getJSONObject(i);
						Question tempQuestion = new Question(i,c.getString("question"));
						String rightAns = c.getString("rightans");
						tempQuestion.setAnsware(rightAns);
		         		System.out.println(tempQuestion);
						playQuizquestions.add(tempQuestion);
						
					}
					QuestionHandler.lstQuestions = playQuizquestions;
					
				} catch (JSONException e) {
					Log.e("JSON Parser", "Error parsing data" + e.toString());
				}
			}
			return questionJson;
		}

		@Override
		protected void onPostExecute(String result) {
			if(playQuizquestions.size()<=0){
				questionHandler = new QuestionHandler(getPackageName(),levelNo);
				
			}
			if(playQuizquestions.size()<=0){
				if(playQuizquestions.size()<=9){
					questionHandler = new QuestionHandler(getPackageName(),levelNo);
				}
			}
			
			progress.cancel();
			nextQuizQuestion();
		}
		

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
		}

	}
	public void nextQuizQuestion(){
		totalQuestion = questionHandler.lstQuestions.size();
		System.out.println("Level: "+questionHandler.lstQuestions.size());
		if(totalQuestion>=1){
			txtQuestion.setText(questionHandler.lstQuestions.get(currentQuestion).getQuestion());
			txtQuestionNO.setText(""+(currentQuestion + 1)+"/"+totalQuestion);
		}else{
			AlertDialog alertDialog = new AlertDialog.Builder(
                    SingleAnsQuizActivity.this).create();

		    // Setting Dialog Title
		    alertDialog.setTitle("No Questions");
		
		    // Setting Dialog Message
		    alertDialog.setMessage("There are no enough question of this level");
		
		    // Setting Icon to Dialog
		   // alertDialog.setIcon(R.drawable.tick);
		
		    // Setting OK Button
		    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            // Write your code here to execute after dialog closed
		            //Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
		            	finish();
		            }
		    });
		
		    // Showing Alert Message
		    alertDialog.show();
		}
	}
}
