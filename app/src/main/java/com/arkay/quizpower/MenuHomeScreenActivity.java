package com.arkay.quizpower;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Random;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.arkay.quizpower.bean.GameData;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.Snapshots;


/**
 * Home Screen of this apps. Display Button to play quiz, Leaderboard, achievement, setting etc.
 * @author Arkay Apps
 *
 */
public class MenuHomeScreenActivity extends FragmentActivity implements
		View.OnClickListener,   QuizPlayActivity.Listener, QuizCompletedActivity.Listener{

	private Button btnPlay,  btnLearning,btnSetting,  btnAbout, btnHelp;

	/** The interstitial ad. */
	public static final String PREFS_NAME = "preferences";
	private static final String DATABASE_NAME = "database.db";
	 public static final String myshareprefkey = "quizpower";
	 
	 public static final String SOUND_EFFECT = "sound_effect";
	 public static final String VIBRATION = "vibration";
	 
	 public static final String TOTAL_SCORE = "total_score";
	 public static final String LEVEL ="level";
	 
	 //Achivement
	 public static final String LEVEL_COMPLETED = "level_completed";
	 public static final String IS_LAST_LEVEL_COMPLETED = "is_last_level_completed";
	 public static final String LAST_LEVEL_SCORE = "last_level_score";
	 public static final String HOW_MANY_TIMES_PLAY_QUIZ = "how_many_time_play_quiz";
	 public static final String COUNT_QUESTION_COMPLETED = "count_question_completed";
	 public static final String COUNT_RIGHT_ANSWARE_QUESTIONS = "count_right_answare_questions";
	 
	 public static final String VERY_CURIOUS_UNLOCK="is_very_curious_unlocked";
	 
	 final int RC_RESOLVE = 5000, RC_UNUSED = 5001;
	 
	 QuizPlayActivity quizPlayActivity;
	 SharedPreferences settings;
	 QuizPlayActivity mQuizPlayFragment;
	 QuizCompletedActivity quizCompletedFragment;
	 private InterstitialAd interstitial;
	 private static final int OUR_STATE_KEY = 2;
	 
	 Context context;
	 public static final String REG_ID = "regId";
	 static final String TAG = "MenuHomeScreenActivity";
	 AsyncTask<Void, Void, String> shareRegidTask;
	 
	 ProgressDialog progress;
	 private GameData gameData;
	 private final Handler mHandler = new Handler();
	 
	// Request code used to invoke sign in user interactions.
	private static final int RC_SIGN_IN = 9001;

	// Request code for listing saved games
	private static final int RC_LIST_SAVED_GAMES = 9002;

	// Client used to interact with Google APIs.
	
	private String currentSaveName = "snapshotTemp";

	// whether we already loaded the state the first time (so we don't reload
	// every time the activity goes to the background and comes back to the
	// foreground)
	boolean mAlreadyLoadedState = false;

	// Are we currently resolving a connection failure?
	private boolean mResolvingConnectionFailure = false;

	// Has the user clicked the sign-in button?
	private boolean mSignInClicked = false;

	// Members related to the conflict resolution chooser of Snapshots.
	final static int MAX_SNAPSHOT_RESOLVE_RETRIES = 3;

	// Set to true to automatically start the sign in flow when the Activity
	// starts.  Set to false to require the user to click the button in order to sign in.
	private boolean mAutoStartSignInFlow = true;
	
	public RelativeLayout main_home_layout;
	
	public enum TrackerName {
	    APP_TRACKER, // Tracker used only in this app.
	    GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
	    ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
	  }
	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_home);
		 getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		context = getApplicationContext();
				
		settings = getSharedPreferences(MenuHomeScreenActivity.PREFS_NAME, 0);
		gameData = new GameData(settings,myshareprefkey);
		// Create the Google Api Client with access to Plus and Games
       
		
		btnPlay = (Button) findViewById(R.id.btnPlay);
		btnPlay.setOnClickListener(this);
		
		btnLearning = (Button)findViewById(R.id.btnLearning);
		btnLearning.setOnClickListener(this);
		
		btnSetting = (Button)findViewById(R.id.btnSetting);
		btnSetting.setOnClickListener(this);
		
		btnAbout = (Button)findViewById(R.id.btnAbout);
		btnAbout.setOnClickListener(this);
		
		btnHelp = (Button)findViewById(R.id.btnHelp);
		btnHelp.setOnClickListener(this);
		
		
		mQuizPlayFragment = new QuizPlayActivity();
		mQuizPlayFragment.setListener(this);
		
		quizCompletedFragment = new QuizCompletedActivity();
		 quizCompletedFragment.setListener(this);

		checkDB();

		
	  	    progress = new ProgressDialog(this);
        progress.setTitle("Please Wait!!");
        progress.setMessage("Data Loading..");
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        Handler delayhandler = new Handler();
        
		delayhandler.postDelayed(stopLoadDataDialogSomeTime, 5000);
		
       
        
        // Create the interstitial.
	    interstitial = new InterstitialAd(this);
	    interstitial.setAdUnitId(getString(R.string.admob_intersitital));

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

	    // Begin loading your interstitial.
	    interstitial.loadAd(adRequest);
	    
	    mHandler.postDelayed(mUpdateUITimerTask, 10 * 1000);
	    
	    
		    
	    
	    
	    // Get tracker.
        Tracker t = getTracker(TrackerName.APP_TRACKER);
        // Set screen name. Where path is a String representing the screen name.
	    t.setScreenName("com.arkay.gkingujarati.MenuHomeScreenActivity");
        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());
	}
	
	@Override
    protected void onStop() {
        super.onStop();
    }


	 Runnable stopLoadDataDialogSomeTime = new Runnable()
		{   public void run(){   
			progress.dismiss();
		    }
		};
		
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnPlay:
			getSupportFragmentManager().beginTransaction().replace( R.id.fragment_container, mQuizPlayFragment ).addToBackStack( "tag" ).commit();
			    
			break;
		case R.id.btnLearning:
			Intent intPlay = new Intent(this, LevelActivity.class);
			startActivity(intPlay);
			break;
		case R.id.btnSetting:
			Intent playQuiz = new Intent(this, SettingActivity.class);
			startActivity(playQuiz);
			break;
		case R.id.btnAbout:
			Intent intAbout = new Intent(this, AboutUsActivity.class);
			startActivity(intAbout);
			break;
		case R.id.btnHelp:
			Intent howtoplay = new Intent(this, HowToPlayActivity.class);
			startActivity(howtoplay);
			break;
		}
		

	}

	public void checkDB() {
		try {
			String packageName = this.getPackageName();
			String destPath = "/data/data/" + packageName + "/databases";
			String fullPath = "/data/data/" + packageName + "/databases/"+ DATABASE_NAME;
			// this database folder location
			File f = new File(destPath);
			// this database file location
			File obj = new File(fullPath);
			// check if databases folder exists or not. if not create it
			if (!f.exists()) {
				f.mkdirs();
				f.createNewFile();
			}else{
				boolean isDelete = f.delete();
				Log.i("Delete", "Delete"+isDelete);
				
			}
			// check database file exists or not, if not copy database from
			// assets
			if (!obj.exists()) {
				this.CopyDB(fullPath);
			}else{
				this.CopyDB(fullPath);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void CopyDB(String path) throws IOException {

		InputStream databaseInput = null;
		String outFileName = path;
		OutputStream databaseOutput = new FileOutputStream(outFileName);
		
		byte[] buffer = new byte[1024];
		int length;

		// open database file from asset folder
		databaseInput = this.getAssets().open(DATABASE_NAME);
		while ((length = databaseInput.read(buffer)) > 0) {
			databaseOutput.write(buffer, 0, length);
			databaseOutput.flush();
		}
		databaseInput.close();

		databaseOutput.flush();
		databaseOutput.close();
	}

	


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                    + resultCode + ", intent=" + intent);
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            
        }
        // the standard snapshot selection intent
        else if (requestCode == RC_LIST_SAVED_GAMES) {
            if (intent != null) {
                if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_METADATA)) {
                    // Load a snapshot.
                    SnapshotMetadata snapshotMetadata =
                            intent.getParcelableExtra(Snapshots.EXTRA_SNAPSHOT_METADATA);
                    currentSaveName = snapshotMetadata.getUniqueName();
                } else if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_NEW)) {
                    // Create a new snapshot named with a unique string
                    // TODO: check for existing snapshot, for now, add garbage text.
                    String unique = new BigInteger(281, new Random()).toString(13);
                    currentSaveName = "snapshotTemp-" + unique;
                   
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
        
		
	}

	
	@Override
	public void onStartGameRequested(boolean hardMode) {
		 getSupportFragmentManager().popBackStack();
		 this.findViewById(R.id.linearLayout1).setVisibility(View.VISIBLE);
	}

	

	
	
	boolean addList = false;

	@Override
	public void onBackPressed() {
		getSupportFragmentManager().popBackStack();
		this.findViewById(R.id.linearLayout1).setVisibility(View.VISIBLE);
		if(getSupportFragmentManager().getBackStackEntryCount()==0){
			super.onBackPressed();
		}
		
	}

		
	@Override
	public void displyHomeScreen() {
		getSupportFragmentManager().popBackStack();
		this.findViewById(R.id.linearLayout1).setVisibility(View.VISIBLE);
	}

	
	public static byte[] intToByteArray(int a) {
	    return BigInteger.valueOf(a).toByteArray();
	}

	public static int byteArrayToInt(byte[] b) {
	    return new BigInteger(b).intValue();
	}
    
	


		@Override
		public GameData getGameData() {
			return this.gameData;
		}

		@Override
		public QuizCompletedActivity getQuizCompletedFragment() {
			return quizCompletedFragment;
		}

		
		public boolean isConnectingToInternet(){
	        ConnectivityManager connectivity = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
	          if (connectivity != null)
	          {
	              NetworkInfo[] info = connectivity.getAllNetworkInfo();
	              if (info != null)
	                  for (int i = 0; i < info.length; i++)
	                      if (info[i].getState() == NetworkInfo.State.CONNECTED)
	                      {
	                          return true;
	                      }
	 
	          }
	          return false;
	    }

		@Override
		public void playAgain() {
			// TODO Auto-generated method stub
			getSupportFragmentManager().popBackStack();
			getSupportFragmentManager().beginTransaction().replace( R.id.fragment_container, mQuizPlayFragment ).addToBackStack( "tag" ).commit();
		}
		
		
		
	
		/** Prints a log message (convenience method). */
		void log(String message) {
			Log.d(TAG, message);
		}
		  public void displayInterstitial() {
			    if (interstitial.isLoaded()) {
			      interstitial.show();
			    }
			  }
		 private final Runnable mUpdateUITimerTask = new Runnable() {
			    public void run() {
			    	displayInterstitial();
			    	
			    }
			};
			
			synchronized  Tracker getTracker(TrackerName trackerId) {
			    if (!mTrackers.containsKey(trackerId)) {
			      GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			      Tracker t =      analytics.newTracker(R.xml.global_tracker);
			      mTrackers.put(trackerId, t);
			    }
			    return mTrackers.get(trackerId);
			  }
}
		
		
