package com.arkay.quizpower;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arkay.quizpower.bean.GameData;
import com.arkay.quizpower.playquizbeans.PlayQuizLevel;
import com.arkay.quizpower.playquizbeans.PlayQuizQuestion;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Quiz activity, on This screen user play quiz with four option. 
 * @author Arkay App
 *
 */

public class QuizPlayActivity  extends Fragment  implements OnClickListener{
	private static int levelNo=1;
	private PlayQuizLevel level;
	private int quextionIndex=0;
	private boolean isSoundEffect;
	private boolean isVibration;

	private int NO_OF_QUESTION = 10;
	private int totalScore=0;
	private int score=0;
	private int correctQuestion=0;
	private int inCorrectQuestion=0;
	
	
	private TextView quizImage;
	private Button btnOpt1, btnOpt2, btnOpt3, btnOpt4;
	private TextView txtQuestion, txtScore, txtLevel;
	private SharedPreferences settings;
	boolean isSoundOn=false;
	private Animation animation;
	private MediaPlayer rightAnsware, wrongeAnsware;
	private TextView txtTrueQuestion, txtFalseQuestion;
	private final Handler mHandler = new Handler();
	private SharedPreferences.Editor editor;
	private View v;
	AdView adView;
	private InterstitialAd interstitial;
	private List<PlayQuizQuestion> playQuizquestions =null;
	private RelativeLayout playQuizmainLayout,top_panel;
	
	 private ProgressDialog progress;
	 String questionJson = "";
	 
	 Animation animationFromRight,animationFromLeft;
	
	 public interface Listener {
	        public void onStartGameRequested(boolean hardMode);
	        public void onShowAchievementsRequested();
	        public void onShowLeaderboardsRequested();
	        public void onSignInButtonClicked();
	        public void onSignOutButtonClicked();
	        public void unlockAchievement(int achievementId, String fallbackString);
	        public void displyHomeScreen();
	        public void  updateLeaderboards(int finalScore);
	        public GameData getGameData();
	        public QuizCompletedActivity getQuizCompletedFragment();
	        public void saveDataToCloud();
	    }
	  Listener mListener = null;
	  
	  @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        v = inflater.inflate(R.layout.fragment_quiz_play, container, false);
	        final int[] CLICKABLES = new int[] {
	                R.id.btnOpt1, R.id.btnOpt2,
	                R.id.btnOpt3,R.id.btnOpt4
	        };
	        for (int i : CLICKABLES) {
	            v.findViewById(i).setOnClickListener(this);
	        }
	        
	        animationFromRight = new TranslateAnimation(500f, 0f, 0f, 0f);
			animationFromRight.setDuration(600);
			animationFromLeft = new TranslateAnimation(-500f, 0f, 0f, 0f);
			animationFromLeft.setDuration(600);
			
			animationFromRight = new TranslateAnimation(500f, 0f, 0f, 0f);
			animationFromRight.setDuration(600);
			animationFromLeft = new TranslateAnimation(-500f, 0f, 0f, 0f);
			animationFromLeft.setDuration(600);
			
			
	        settings = getActivity().getSharedPreferences(MenuHomeScreenActivity.PREFS_NAME, 0);
	        
	       resetAllValue();
	       
	       
			// Create an ad
			adView = new AdView(getActivity());
		    adView.setAdSize(AdSize.SMART_BANNER);
		    adView.setAdUnitId(getString(R.string.admob_banner));
		    
		    // Add the AdView to the view hierarchy. The view will have no size  until the ad is loaded.
		    LinearLayout layout = (LinearLayout) v.findViewById(R.id.ads_layout);
		    layout.addView(adView);
		    // Create an ad request. Check logcat output for the hashed device ID to get test ads on a physical device.
		    // Create ad request.
		    Resources ress = getResources();
		    boolean isTestMode = ress.getBoolean(R.bool.istestmode);
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
		    interstitial = new InterstitialAd(getActivity());
		    interstitial.setAdUnitId(getString(R.string.admob_intersitital));
		    // Begin loading your interstitial.
		    interstitial.loadAd(adRequest);
	        
	        return v;
	  }
	  public void setListener(Listener l) {
	        mListener = l;
	    }

	    @Override
	    public void onStart() {
	        super.onStart();
	        updateUi();
	    }

	    @Override
		public void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			 updateUi();
		}

		public void setGreeting(String greeting) {
	      //  mGreeting = greeting;
	        updateUi();
	    }

	    void updateUi() {
	        if (getActivity() == null) return;
	        getActivity().findViewById(R.id.linearLayout1).setVisibility(View.GONE);
	        

	    }

	private void nextQuizQuestion(){
		int count_question_completed = mListener.getGameData().getCountHowManyQuestionCompleted();
		count_question_completed++;
		mListener.getGameData().setCountHowManyQuestionCompleted(count_question_completed);
		unlockhowManyQuestionCompleted(count_question_completed);
		System.out.println("Count Question Completed: "+ mListener.getGameData().getCountHowManyQuestionCompleted());
		if(quextionIndex>=NO_OF_QUESTION){
			if(score==100){
				mListener.unlockAchievement(R.string.achievement_perfectionist,"Perfectionist");
			}
			if(score>=1 && score<50){
				mListener.unlockAchievement(R.string.achievement_sadness,"Sadness");
			}
			mListener.updateLeaderboards(totalScore);
			
			int howManyTimesPlayQuiz = mListener.getGameData().getCountHowManyTimePlay();
			System.out.println("How Many Time Play: "+howManyTimesPlayQuiz);
			howManyTimesPlayQuiz++;
			unlockPlayTime(howManyTimesPlayQuiz);
			mListener.getGameData().setCountHowManyTimePlay(howManyTimesPlayQuiz);
			
			count_question_completed = mListener.getGameData().getCountHowManyQuestionCompleted();
			count_question_completed--;
			mListener.getGameData().setCountHowManyQuestionCompleted(count_question_completed);
			
			saveScore();
			
			
			displayInterstitial();
			
			getActivity().getSupportFragmentManager().popBackStack();
			getActivity().getSupportFragmentManager().beginTransaction().replace( R.id.fragment_container, mListener.getQuizCompletedFragment() ).addToBackStack( "tag" ).commit();
			
			
			blankAllValue();
			/*QuizCompletedDialog cdd=new QuizCompletedDialog(getActivity());
			cdd.show();*/ 
			//mListener.displyHomeScreen();
			return;
		}
		
		btnOpt1.setClickable(true);
		btnOpt2.setClickable(true);
		btnOpt3.setClickable(true);
		btnOpt4.setClickable(true);

		/*btnOpt1.setTextColor(getResources().getColor(R.color.text_color));
		btnOpt2.setTextColor(getResources().getColor(R.color.text_color));
		btnOpt3.setTextColor(getResources().getColor(R.color.text_color));
		btnOpt4.setTextColor(getResources().getColor(R.color.text_color));*/
		
		if(quextionIndex<level.getNoOfQuestion()){
			int temp = quextionIndex;
			txtQuestion.setText(""+ ++temp+"/"+NO_OF_QUESTION);
			String imgName = level.getQuestion().get(quextionIndex).getQuestion();
	        Pattern p = Pattern.compile(" ");          
		 	Matcher m = p.matcher(imgName);
		 	imgName = m.replaceAll("_");
		 	quizImage.setText(level.getQuestion().get(quextionIndex).getQuestion());
	       
	        ArrayList<String> options = new ArrayList<String>();
	        options.addAll(level.getQuestion().get(quextionIndex).getOptions());
	        Collections.shuffle(options);
	        
	        btnOpt1.setText(""+options.get(0).trim());
	        btnOpt2.setText(""+options.get(1).trim());
	        btnOpt3.setText(""+options.get(2).trim());
	        btnOpt4.setText(""+options.get(3).trim());
		}
		
		btnOpt1.startAnimation(animationFromLeft);
		btnOpt2.startAnimation(animationFromRight);
		btnOpt3.startAnimation(animationFromLeft);
		btnOpt4.startAnimation(animationFromRight);
		
		playQuizmainLayout.setBackgroundColor(getResources().getColor(R.color.app_background_color_1));
		top_panel.setBackgroundColor(getResources().getColor(R.color.app_background_color_1_1));
		changeBtnTexColor();
		
		btnOpt1.setBackgroundResource(R.drawable.option_button_color_1);
		btnOpt2.setBackgroundResource(R.drawable.option_button_color_1);
		btnOpt3.setBackgroundResource(R.drawable.option_button_color_1);
		btnOpt4.setBackgroundResource(R.drawable.option_button_color_1);
		
		
		
	        
	}
	
	public void changeBtnTexColor(){
		btnOpt1.setTextColor(getResources().getColor(R.color.app_background_color_1));
		btnOpt2.setTextColor(getResources().getColor(R.color.app_background_color_1));
		btnOpt3.setTextColor(getResources().getColor(R.color.app_background_color_1));
		btnOpt4.setTextColor(getResources().getColor(R.color.app_background_color_1));
		txtQuestion.setTextColor(getResources().getColor(R.color.app_background_color_1));
		txtScore.setTextColor(getResources().getColor(R.color.app_background_color_1));
		
		
	}
	// Invoke displayInterstitial() when you are ready to display an interstitial.
	  public void displayInterstitial() {
	    if (interstitial.isLoaded()) {
	      interstitial.show();
	    }
	  }

	  
	@Override
	public void onClick(View v) {
		
		if(quextionIndex<level.getNoOfQuestion()){
			btnOpt1.setClickable(false);
			btnOpt2.setClickable(false);
			btnOpt3.setClickable(false);
			btnOpt4.setClickable(false);
			switch(v.getId()){
			case R.id.btnOpt1:
				if(btnOpt1.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())){
					quextionIndex++;
					changeBtnTexColor();
					btnOpt1.setTextColor(getResources().getColor(R.color.White));
					/*btnOpt2.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt3.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt4.setTextColor(getResources().getColor(R.color.text_color));
					*/
					addScore();
					btnOpt1.setBackgroundResource(R.drawable.button_green);
					btnOpt1.startAnimation(animation);
					btnOpt1.setTextColor(getResources().getColor(R.color.White));
				}else{
					btnOpt1.setBackgroundResource(R.drawable.button_red);
					changeBtnTexColor();
					
					btnOpt1.setTextColor(getResources().getColor(R.color.White));
					/*btnOpt2.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt3.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt4.setTextColor(getResources().getColor(R.color.text_color));*/
					wrongeQuestion();
					quextionIndex++;
				}
				break;
			case R.id.btnOpt2:
				if(btnOpt2.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())){
					quextionIndex++;
					
					changeBtnTexColor();
					btnOpt2.setTextColor(getResources().getColor(R.color.White));
					
					/*btnOpt1.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt3.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt4.setTextColor(getResources().getColor(R.color.text_color));*/
					addScore();
					btnOpt2.setBackgroundResource(R.drawable.button_green);
					btnOpt2.startAnimation(animation);
					btnOpt2.setTextColor(getResources().getColor(R.color.White));
				}else{
					btnOpt2.setBackgroundResource(R.drawable.button_red);
					changeBtnTexColor();
					
					btnOpt2.setTextColor(getResources().getColor(R.color.White));
					/*btnOpt1.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt3.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt4.setTextColor(getResources().getColor(R.color.text_color));*/
					wrongeQuestion();
					quextionIndex++;
				}
				break;
			case R.id.btnOpt3:
				if(btnOpt3.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())){
					quextionIndex++;
					changeBtnTexColor();
					/*btnOpt1.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt2.setTextColor(getResources().getColor(R.color.text_color));*/
					btnOpt3.setTextColor(getResources().getColor(R.color.White));
					//btnOpt4.setTextColor(getResources().getColor(R.color.text_color));
					addScore();
					btnOpt3.setBackgroundResource(R.drawable.button_green);
					btnOpt3.startAnimation(animation);
					btnOpt3.setTextColor(getResources().getColor(R.color.White));
				}else{
					btnOpt3.setBackgroundResource(R.drawable.button_red);
					changeBtnTexColor();
					btnOpt3.setTextColor(getResources().getColor(R.color.White));
					/*btnOpt1.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt2.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt4.setTextColor(getResources().getColor(R.color.text_color));*/
					wrongeQuestion();
					quextionIndex++;
				}
				break;
			case R.id.btnOpt4:
				if(btnOpt4.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())){
					quextionIndex++;
					changeBtnTexColor();
					/*btnOpt1.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt2.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt3.setTextColor(getResources().getColor(R.color.text_color));*/
					btnOpt4.setTextColor(getResources().getColor(R.color.White));
					addScore();
					btnOpt4.setBackgroundResource(R.drawable.button_green);
					btnOpt4.startAnimation(animation);
					btnOpt4.setTextColor(getResources().getColor(R.color.White));
				}else{
					btnOpt4.setBackgroundResource(R.drawable.button_red);
					changeBtnTexColor();
					btnOpt4.setTextColor(getResources().getColor(R.color.White));
					/*btnOpt1.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt2.setTextColor(getResources().getColor(R.color.text_color));
					btnOpt3.setTextColor(getResources().getColor(R.color.text_color));*/
					wrongeQuestion();
					quextionIndex++;
				}
				break;
			}
			
		}else{
			mHandler.postDelayed(mUpdateUITimerTask, 2 * 10);
		}
		mHandler.postDelayed(mUpdateUITimerTask, 2 * 1000);
		txtScore.setText(""+totalScore);
		
	}
	private final Runnable mUpdateUITimerTask = new Runnable() {
	    public void run() {
	    	btnOpt1.setBackgroundResource(R.drawable.button_blue);
			btnOpt2.setBackgroundResource(R.drawable.button_blue);
			btnOpt3.setBackgroundResource(R.drawable.button_blue);
			btnOpt4.setBackgroundResource(R.drawable.button_blue);
	    	btnOpt1.clearAnimation();
	    	btnOpt2.clearAnimation();
	    	btnOpt3.clearAnimation();
	    	btnOpt4.clearAnimation();
	    	nextQuizQuestion();
	    }
	};
	
	private void addScore(){
		rightSound();
		correctQuestion++;
		txtTrueQuestion.setText(" "+correctQuestion +" ");
		totalScore = totalScore + 10;
		score = score + 10;
		txtScore.setText(" "+totalScore+" ");
		
		int rightAns = mListener.getGameData().getCountHowManyRightAnswareQuestion();
		rightAns++;
		unlockGoodAnswareAchivement(rightAns);
		/*mListener.getGameData().setCountHowManyRightAnswareQuestion(rightAns);
		mListener.getGameData().setTotalScore(totalScore);
		mListener.getGameData().saveDataLocal(settings);*/
		mListener.getGameData().setCountHowManyRightAnswareQuestion(rightAns);
		mListener.getGameData().setTotalScore(totalScore);
		mListener.getGameData().save(settings,MenuHomeScreenActivity.myshareprefkey);
		System.out.println("Right Answare: "+ mListener.getGameData().getCountHowManyRightAnswareQuestion());
	}

	
	private void wrongeQuestion(){
		playWrongSound();
		//saveScore();
		inCorrectQuestion++;
		totalScore = totalScore - 3;
		score = score - 3;
		txtFalseQuestion.setText(" "+ inCorrectQuestion +" ");
		
		if(btnOpt1.getText().toString().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns())){
			btnOpt1.setBackgroundResource(R.drawable.button_green);
			btnOpt1.setTextColor(getResources().getColor(R.color.White));
			btnOpt1.startAnimation(animation);
		}
		if(btnOpt2.getText().toString().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns())){
			btnOpt2.setBackgroundResource(R.drawable.button_green);
			btnOpt2.setTextColor(getResources().getColor(R.color.White));
			btnOpt2.startAnimation(animation);
		}	
		if(btnOpt3.getText().toString().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns())){
			btnOpt3.setBackgroundResource(R.drawable.button_green);
			btnOpt3.setTextColor(getResources().getColor(R.color.White));
			btnOpt3.startAnimation(animation);
		}
		if(btnOpt4.getText().toString().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns())){
			btnOpt4.setBackgroundResource(R.drawable.button_green);
			btnOpt4.setTextColor(getResources().getColor(R.color.White));
			btnOpt4.startAnimation(animation);
		}
		
		if(totalScore<0){
			totalScore=0;
		}
	}
	
	private void saveScore(){
			editor = settings.edit();
			mListener.getGameData().setTotalScore(totalScore);
			//editor.putInt(MenuHomeScreenActivity.TOTAL_SCORE, totalScore);
			editor.putInt(MenuHomeScreenActivity.LAST_LEVEL_SCORE, score);
			
		if(correctQuestion>=7){
			unlockLevelCompletedAchivement(levelNo);
			levelNo++;
			editor.putBoolean(MenuHomeScreenActivity.IS_LAST_LEVEL_COMPLETED, true);
			mListener.getGameData().setLevelCompleted(levelNo);
		}else{
			editor.putBoolean(MenuHomeScreenActivity.IS_LAST_LEVEL_COMPLETED, false);
		}
		mListener.getGameData().save(settings,MenuHomeScreenActivity.myshareprefkey);
		editor.commit();
		mListener.saveDataToCloud();
		
		
		
	}

	
	public void rightSound()
	 {
		 if(isSoundEffect){
		     AudioManager meng = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		     int volume = meng.getStreamVolume( AudioManager.STREAM_NOTIFICATION);
	
		     if (volume != 0)
		     {
		         if (rightAnsware == null)
		        	 rightAnsware = MediaPlayer.create(getActivity(), R.raw.right_ans);
		         if (rightAnsware != null)
		        	 rightAnsware.start();
		     }
		 }
	 }
	
	private void playWrongSound(){
		if(isSoundEffect){
			AudioManager meng = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		     int volume = meng.getStreamVolume( AudioManager.STREAM_NOTIFICATION);
	
		     if (volume != 0)
		     {
		         if (wrongeAnsware == null)
		        	 wrongeAnsware = MediaPlayer.create(getActivity(), R.raw.wronge_ans);
		         if (wrongeAnsware != null)
		        	 wrongeAnsware.start();
		     }
		}
		if(isVibration){
			Vibrator myVib = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);
			myVib.vibrate(200);
		}
		
	}

	private void resetAllValue(){
			
			isSoundOn = settings.getBoolean("silentMode", true);
			levelNo = mListener.getGameData().getLevelCompleted();
			txtQuestion = (TextView)v.findViewById(R.id.txt_question);
			
			txtLevel = (TextView)v.findViewById(R.id.txtLevel);
			txtLevel.setText(getString(R.string.level)+": "+levelNo);
			
			btnOpt1 = (Button)v.findViewById(R.id.btnOpt1);
			btnOpt2 = (Button)v.findViewById(R.id.btnOpt2);
			btnOpt3 = (Button)v.findViewById(R.id.btnOpt3);
			btnOpt3.setOnClickListener(this);
			btnOpt4 = (Button)v.findViewById(R.id.btnOpt4);
			btnOpt4.setOnClickListener(this);
			
			btnOpt1.setBackgroundResource(R.drawable.button_blue);
			btnOpt2.setBackgroundResource(R.drawable.button_blue);
			btnOpt3.setBackgroundResource(R.drawable.button_blue);
			btnOpt4.setBackgroundResource(R.drawable.button_blue);
			
			txtTrueQuestion = (TextView)v.findViewById(R.id.txtTrueQuestion);
			txtTrueQuestion.setText("0");
			txtFalseQuestion = (TextView)v.findViewById(R.id.txtFalseQuestion);
			txtFalseQuestion.setText("0");
			
			quizImage  = (TextView)v.findViewById(R.id.imgQuiz);
			
			animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
			animation.setDuration(500); // duration - half a second
			animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
			animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
			animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
		
			    
			isSoundEffect = settings.getBoolean(MenuHomeScreenActivity.SOUND_EFFECT, true);
			isVibration = settings.getBoolean(MenuHomeScreenActivity.VIBRATION, true);
			 
			totalScore  = mListener.getGameData().getTotalScore();
		    txtScore = (TextView)v.findViewById(R.id.txtScore);
			
		    playQuizmainLayout = (RelativeLayout)v.findViewById(R.id.playQuizmainLayout);
		    top_panel = (RelativeLayout)v.findViewById(R.id.top_panel);
		    
		    txtScore.setText(""+totalScore); 
		    
			Resources ress = getResources();
			boolean isQuestionFromWeb = ress.getBoolean(R.bool.isQuestionFormWeb); 
			if(isQuestionFromWeb){
				//load for web
				progress = new ProgressDialog(getActivity());
		        progress.setTitle("Please Wait!!");
		        progress.setMessage("Data Loading..");
		        progress.setCancelable(false);
		        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		        progress.show();
				LoadQuestions task = new LoadQuestions();
				task.execute(new String[] { ress.getString(R.string.question_bank_url)+levelNo });
			}else{
				level = new PlayQuizLevel(levelNo,NO_OF_QUESTION, getActivity());
				level.setQuestionRendomFromDatabase();
				nextQuizQuestion();
			}
		    
	}
	public void blankAllValue(){
		quextionIndex=0;
		isSoundEffect=false;
		isVibration=false;

		totalScore=0;
		score=0;
		correctQuestion=0;
		inCorrectQuestion=0;
	}
	private void unlockLevelCompletedAchivement(int levelNo){
		if(levelNo>=1 && levelNo<=2){
			mListener.unlockAchievement(R.string.achievement_started, "Started");
		}
		if(levelNo>=10 && levelNo<=11){
			mListener.unlockAchievement(R.string.achievement_right_track, "Right Track");
		}
		if(levelNo>=20 && levelNo<=21){
			mListener.unlockAchievement(R.string.achievement_good_in_gk, "Good in GK");
		}
		if(levelNo>=50 && levelNo<=51){
			mListener.unlockAchievement(R.string.achievement_master_in_gk, "Master in GK");
		}
		if(levelNo>=100 && levelNo<=101){
			mListener.unlockAchievement(R.string.achievement_expert_in_gk, "Expert in GK");
		}

	}
	private void unlockPlayTime(int playTime){
		if(playTime>=2 && playTime<=3){
			mListener.unlockAchievement(R.string.achievement_good_luck, "Good Luck");
		}
		if(playTime>=50 && playTime<=51){
			mListener.unlockAchievement(R.string.achievement_fifty, "Fifty");
		}
		if(playTime>=100 && playTime<=101){
			mListener.unlockAchievement(R.string.achievement_hundred, "Hundred");
		}
		if(playTime>=500 && playTime<=501){
			mListener.unlockAchievement(R.string.achievement_no_higher, "No Higher");
		}
	}
	private void unlockhowManyQuestionCompleted(int count_question_completed){
		if(count_question_completed>50 && count_question_completed<55){
			mListener.unlockAchievement(R.string.achievement_fifty_qustions, "fifty qustions");
		}
		if(count_question_completed>100 && count_question_completed<105){
			mListener.unlockAchievement(R.string.achievement_century_qustions, "century qustions");
		}
		if(count_question_completed>1000 && count_question_completed<1005){
			mListener.unlockAchievement(R.string.achievement_sustained_questions, "sustained questions");
		}
	}
	private void unlockGoodAnswareAchivement(int totalGoodAnsware){
		if(totalGoodAnsware>=50 && totalGoodAnsware<=52){
			mListener.unlockAchievement(R.string.achievement_fifly_good_answer, "Fifty Good Asware");
		}
		if(totalGoodAnsware>=300 && totalGoodAnsware<=301){
			mListener.unlockAchievement(R.string.achievement_batter_and_batter, "Batter and Batter");
		}
		if(totalGoodAnsware>=500 && totalGoodAnsware<=501){
			mListener.unlockAchievement(R.string.achievement_cool, "Cool");
		}
		if(totalGoodAnsware>=700 && totalGoodAnsware<=701){
			mListener.unlockAchievement(R.string.achievement_very_good, "Very Good");
		}
		if(totalGoodAnsware>=1000 && totalGoodAnsware<=1001){
			mListener.unlockAchievement(R.string.achievement_fentastic, "Fentastic");
		}
		if(totalGoodAnsware>=1200 && totalGoodAnsware<=1201){
			mListener.unlockAchievement(R.string.achievement_phenomenal, "Phenomenal");
		}
		if(totalGoodAnsware>=1500 && totalGoodAnsware<=1501){
			mListener.unlockAchievement(R.string.achievement_perfect, "Perfect");
		}
		
	}
	
	private class LoadQuestions extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			questionJson="";
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
					playQuizquestions = new ArrayList<PlayQuizQuestion>();
					JSONArray users = new JSONArray(questionJson);
					for (int i = 0; i < users.length(); i++) {
						JSONObject c = users.getJSONObject(i);
						PlayQuizQuestion tempQuestion = new PlayQuizQuestion(c.getString("question"));
						tempQuestion.addOption(c.getString("optiona"));
						tempQuestion.addOption(c.getString("optionb"));
						tempQuestion.addOption(c.getString("optionc"));
						tempQuestion.addOption(c.getString("optiond"));
						String rightAns = c.getString("rightans");
		         		
		         		if(rightAns.equalsIgnoreCase("A")){
		         			tempQuestion.setTrueAns(c.getString("optiona"));
		         		}else if(rightAns.equalsIgnoreCase("B")){
		         			tempQuestion.setTrueAns(c.getString("optionb"));
		         		}else if(rightAns.equalsIgnoreCase("C")){
		         			tempQuestion.setTrueAns(c.getString("optionc"));
		         		}else{
		         			tempQuestion.setTrueAns(c.getString("optiond"));
		         		}
						System.out.println(tempQuestion);
						playQuizquestions.add(tempQuestion);
						
					}
					level = new PlayQuizLevel(levelNo,NO_OF_QUESTION,getActivity());
					Collections.shuffle(playQuizquestions);
					level.setQuestion(playQuizquestions);
					
				} catch (JSONException e) {
					Log.e("JSON Parser", "Error parsing data" + e.toString());
				}
			}
			return questionJson;
		}

		@Override
		protected void onPostExecute(String result) {
			if(playQuizquestions.size()<=0){
				level = new PlayQuizLevel(levelNo,NO_OF_QUESTION,getActivity());
				level.setQuestionRendomFromDatabase();
			}
			if(playQuizquestions.size()<=0){
				if(playQuizquestions.size()<=9){
					level = new PlayQuizLevel(levelNo,NO_OF_QUESTION,getActivity());
					level.setQuestionRendomFromDatabase();
				}
			}
			progress.cancel();
			nextQuizQuestion();
		}
			
		

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
		}

	}
	

}
