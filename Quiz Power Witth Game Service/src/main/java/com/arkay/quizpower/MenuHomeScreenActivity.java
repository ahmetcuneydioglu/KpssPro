package com.arkay.quizpower;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.arkay.quizpower.bean.GameData;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameActivity;


/**
 * Home Screen of this apps. Display Button to play quiz, Leaderboard, achievement, setting etc.
 * @author Arkay Apps
 *
 */
public class MenuHomeScreenActivity extends BaseGameActivity implements
		View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,  QuizPlayActivity.Listener, QuizCompletedActivity.Listener{

	private Button btnPlay, btnLeaderboard, btnAchievement, btnLearning,btnSetting,  btnAbout, btnHelp;

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
	private GoogleApiClient mGoogleApiClient;
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
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                .build();
        
		
		btnPlay = (Button) findViewById(R.id.btnPlay);
		btnPlay.setOnClickListener(this);
		btnLeaderboard = (Button) findViewById(R.id.btnLeaderboard);
		btnLeaderboard.setOnClickListener(this);
		
		btnAchievement = (Button) findViewById(R.id.btnAchievement);
		btnAchievement.setOnClickListener(this);
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

		findViewById(R.id.sign_in_button).setOnClickListener(this);
		findViewById(R.id.sign_out_button).setOnClickListener(this);
		
	  	    progress = new ProgressDialog(this);
        progress.setTitle("Please Wait!!");
        progress.setMessage("Data Loading..");
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        Handler delayhandler = new Handler();
        
		delayhandler.postDelayed(stopLoadDataDialogSomeTime, 5000);
		
        SignInButton mSignInButton = (SignInButton)findViewById(R.id.sign_in_button);
        
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start the asynchronous sign in flow
            	System.out.println("Click on Sign-in");
            	mGoogleApiClient.connect();
            }
        });
        mGoogleApiClient.connect();
        
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
			if(gameData.getLevelCompleted()==0 || gameData.getLevelCompleted()==1){
				if(isSignedIn()){
					unlockAchievement(R.string.achievement_beginner, "Beginner");
				}
			}
			getSupportFragmentManager().beginTransaction().replace( R.id.fragment_container, mQuizPlayFragment ).addToBackStack( "tag" ).commit();
			    
			break;
		case R.id.btnLeaderboard:
			if (isSignedIn()) {
				SharedPreferences.Editor edit = settings.edit();
				edit.putInt(VERY_CURIOUS_UNLOCK, 1);
				edit.commit();
				unlockAchievement(R.string.achievement_very_curious,"Very Curious");
				startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getApiClient()),RC_UNUSED);
			}
			break;
		case R.id.btnAchievement:
			if (isSignedIn()) {
				unlockAchievement(R.string.achievement_curious, "Curious");
				startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()),RC_UNUSED);
			}
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
		if (v.getId() == R.id.sign_in_button) {
			findViewById(R.id.sign_in_button).setVisibility(View.GONE);
			findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
			findViewById(R.id.sign_in_bar).setVisibility(View.GONE);
			findViewById(R.id.sign_out_bar).setVisibility(View.VISIBLE);
		} else if (v.getId() == R.id.sign_out_button) {
			// sign out.
			signOut();
			// show sign-in button, hide the sign-out button
			findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
			findViewById(R.id.sign_out_button).setVisibility(View.GONE);
			findViewById(R.id.sign_out_bar).setVisibility(View.GONE);
			findViewById(R.id.sign_in_bar).setVisibility(View.VISIBLE);
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
	public void onSignInFailed() {
		System.out.println("Sing In Fail");
		findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
		findViewById(R.id.sign_out_button).setVisibility(View.GONE);
		
		findViewById(R.id.sign_out_bar).setVisibility(View.GONE);
		findViewById(R.id.sign_in_bar).setVisibility(View.VISIBLE);
		progress.cancel();
	}

	@Override
	public void onSignInSucceeded() {
		System.out.println("Sing In Succcess");
		findViewById(R.id.sign_in_button).setVisibility(View.GONE);
		findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
		
		findViewById(R.id.sign_in_bar).setVisibility(View.GONE);
		findViewById(R.id.sign_out_bar).setVisibility(View.VISIBLE);

		
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                    + resultCode + ", intent=" + intent);
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } 
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
                    saveSnapshot(null);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
        
		
	}

	public void unlockAchievement(int achievementId, String fallbackString) {
		if (isSignedIn()) {
			Games.Achievements.unlock(getApiClient(), getString(achievementId));
		} else {
			Toast.makeText(this,
					getString(R.string.achievement) + ": " + fallbackString,
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onStartGameRequested(boolean hardMode) {
		 getSupportFragmentManager().popBackStack();
		 this.findViewById(R.id.linearLayout1).setVisibility(View.VISIBLE);
	}

	@Override
	public void onShowAchievementsRequested() {
		if (isSignedIn()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), RC_UNUSED);
        }
	}

	
	
	@Override
	public void onShowLeaderboardsRequested() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInButtonClicked() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignOutButtonClicked() {
		// TODO Auto-generated method stub
		
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

	 /**
     * Update leaderboards with the user's score.
     *
     * @param finalScore The score the user got.
     */
	@Override
    public void updateLeaderboards(int finalScore) {
		if (isSignedIn()) {
	    	if (finalScore >= 0) {
	            Games.Leaderboards.submitScore(getApiClient(), getString(R.string.leaderboard_quiz_power),
	                   finalScore);
	        }
		}
    }
	public static byte[] intToByteArray(int a) {
	    return BigInteger.valueOf(a).toByteArray();
	}

	public static int byteArrayToInt(byte[] b) {
	    return new BigInteger(b).intValue();
	}
    
	 public   void saveToCloud() {
		 if(isSignedIn()){
	        if(isConnected()){
	        	// 	save new data to cloud
	        		saveSnapshot(null);
	        }
		 }
	    }


		@Override
		public GameData getGameData() {
			return this.gameData;
		}

		@Override
		public void saveDataToCloud() {
			saveToCloud();
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
		
		
		@Override
		public void onConnected(Bundle connectionHint) {
	
			// Sign-in worked!
			log("Sign-in successful! Loading game state from cloud.");
			showSignOutBar();
			loadFromSnapshot(null);
			
		}
		private boolean isConnected() {
	        return mGoogleApiClient == null || !mGoogleApiClient.isConnected();
	    }

	    /**
	     * Loads a Snapshot from the user's synchronized storage.
	     */
	    void loadFromSnapshot(final SnapshotMetadata snapshotMetadata) {
	      

	        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
	            @Override
	            protected Integer doInBackground(Void... params) {
	                 Snapshots.OpenSnapshotResult result;
	                if (snapshotMetadata != null && snapshotMetadata.getUniqueName() != null) {
	                    Log.i(TAG, "Opening snapshot by metadata: " + snapshotMetadata);
	                    result = Games.Snapshots.open(mGoogleApiClient,snapshotMetadata).await();
	                } else {
	                    Log.i(TAG, "Opening snapshot by name: " + currentSaveName);
	                    result = Games.Snapshots.open(mGoogleApiClient, currentSaveName, true).await();
	                }

	                int status = result.getStatus().getStatusCode();

	                Snapshot snapshot = null;
	                if (status == GamesStatusCodes.STATUS_OK) {
	                    snapshot = result.getSnapshot();
	                } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONFLICT) {

	                    // if there is a conflict  - then resolve it.
	                    snapshot = processSnapshotOpenResult( result, 0);

	                    // if it resolved OK, change the status to Ok
	                    if (snapshot != null) {
	                        status = GamesStatusCodes.STATUS_OK;
	                    }
	                    else {
	                        Log.w(TAG,"Conflict was not resolved automatically");
	                    }
	                } else {
	                    Log.e(TAG, "Error while loading: " + status);
	                }

	                if (snapshot != null) {
	                    readSavedGame(snapshot);
	                }
	                return status;
	            }

	            @Override
	            protected void onPostExecute(Integer status) {
	                Log.i(TAG, "Snapshot loaded: " + status);

	                // Note that showing a toast is done here for debugging. Your application should
	                // resolve the error appropriately to your app.
	                if (status == GamesStatusCodes.STATUS_SNAPSHOT_NOT_FOUND) {
	                    Log.i(TAG, "Error: Snapshot not found");
	                    Toast.makeText(getBaseContext(), "Error: Snapshot not found",
	                            Toast.LENGTH_SHORT).show();
	                } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONTENTS_UNAVAILABLE) {
	                    Log.i(TAG, "Error: Snapshot contents unavailable");
	                    Toast.makeText(getBaseContext(), "Error: Snapshot contents unavailable",
	                            Toast.LENGTH_SHORT).show();
	                } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_FOLDER_UNAVAILABLE) {
	                    Log.i(TAG, "Error: Snapshot folder unavailable");
	                    Toast.makeText(getBaseContext(), "Error: Snapshot folder unavailable.",
	                            Toast.LENGTH_SHORT).show();
	                }

	              
	            }
	        };

	        task.execute();
	    }

	    private void readSavedGame(com.google.android.gms.games.snapshot.Snapshot snapshot) {
	    	GameData temp = gameData.clone();
	    	//gameData = new GameData(snapshot.readFully());
	    	try {
				gameData = new GameData(snapshot.getSnapshotContents().readFully());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	System.out.println(temp);
	    	System.out.println(gameData);
	    	
	    	gameData = gameData.unionWith(temp);
	    	//snapshot.writeBytes(gameData.toBytes());
	    	snapshot.getSnapshotContents().writeBytes(gameData.toBytes());
	    	writeSnapshot(snapshot);
	    	
	        mAlreadyLoadedState = true;
	    }
	    /**
	     * Conflict resolution for when Snapshots are opened.
	     * @param result The open snapshot result to resolve on open.
	     * @return The opened Snapshot on success; otherwise, returns null.
	     */
	    Snapshot processSnapshotOpenResult(Snapshots.OpenSnapshotResult result, int retryCount){
	        Snapshot mResolvedSnapshot = null;
	        retryCount++;
	        int status = result.getStatus().getStatusCode();

	        Log.i(TAG, "Save Result status: " + status);

	        if (status == GamesStatusCodes.STATUS_OK) {
	            return result.getSnapshot();
	        } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONTENTS_UNAVAILABLE) {
	            return result.getSnapshot();
	        } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONFLICT){
	            Snapshot snapshot = result.getSnapshot();
	            Snapshot conflictSnapshot = result.getConflictingSnapshot();

	            // Resolve between conflicts by selecting the newest of the conflicting snapshots.
	            mResolvedSnapshot = snapshot;

	            if (snapshot.getMetadata().getLastModifiedTimestamp() <
	                    conflictSnapshot.getMetadata().getLastModifiedTimestamp()){
	                mResolvedSnapshot = conflictSnapshot;
	            }

	            Snapshots.OpenSnapshotResult resolveResult = Games.Snapshots.resolveConflict(
	            		mGoogleApiClient, result.getConflictId(), mResolvedSnapshot)
	                    .await();
	            

	            if (retryCount < MAX_SNAPSHOT_RESOLVE_RETRIES){
	                return processSnapshotOpenResult(resolveResult, retryCount);
	            }else{
	                String message = "Could not resolve snapshot conflicts";
	                Log.e(TAG, message);
	                Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG);
	            }

	        }
	        // Fail, return null.
	        return null;
	    }

	    /**
	     * Prepares saving Snapshot to the user's synchronized storage, conditionally resolves errors,
	     * and stores the Snapshot.
	     */
	    void saveSnapshot(final SnapshotMetadata snapshotMetadata) {
	    	
	    	
	        AsyncTask<Void, Void, Snapshots.OpenSnapshotResult> task =
	                new AsyncTask<Void, Void, Snapshots.OpenSnapshotResult>() {
	                    @Override
	                    protected Snapshots.OpenSnapshotResult doInBackground(Void... params) {
	                        if (snapshotMetadata == null) {
	                        	try{
	                            return Games.Snapshots.open(mGoogleApiClient, currentSaveName, true)
	                                    .await();
	                        	}catch(Exception e){
	                        		return null;
	                        	}
	                        }
	                        else {
	                            return Games.Snapshots.open(mGoogleApiClient, snapshotMetadata)
	                                    .await();
	                        }
	                    }

	                    @Override
	                    protected void onPostExecute(Snapshots.OpenSnapshotResult result) {
	                    	if(result!=null){
		                        Snapshot toWrite = processSnapshotOpenResult( result, 0);
		                       // writeSnapshot(toWrite);
		                        writeSnapshot(toWrite);
	                        }
	                    }
	                };

	             
	        	task.execute();
	        
	    }

	    /**
	     * Generates metadata, takes a screenshot, and performs the write operation for saving a
	     * snapshot.
	     */
	    private String writeSnapshot(Snapshot snapshot){
	        // Set the data payload for the snapshot.
	        gameData.save(settings, myshareprefkey);
	        // Save the snapshot.
	        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
	                .setCoverImage(getScreenShot())
	                .setDescription("Modified data at: " + Calendar.getInstance().getTime())
	                .build();
	        Games.Snapshots.commitAndClose(mGoogleApiClient, snapshot, metadataChange);
	        return snapshot.toString();
	    }
	    
	    /**
	     * Gets a screenshot to use with snapshots. Note that in practice you probably do not want to
	     * use this approach because tablet screen sizes can become pretty large and because the image
	     * will contain any UI and layout surrounding the area of interest.
	     */
	    Bitmap getScreenShot() {
	        View root = findViewById(R.id.main_home_layout);
	        Bitmap coverImage;
	        try {
	            root.setDrawingCacheEnabled(true);
	            Bitmap base = root.getDrawingCache();
	            coverImage = base.copy(base.getConfig(), false /* isMutable */);
	        } catch (Exception ex) {
	            Log.i(TAG, "Failed to create screenshot", ex);
	            coverImage = null;
	        } finally {
	            root.setDrawingCacheEnabled(false);
	        }
	        return coverImage;
	    }
	    
	
		 /** Shows the "sign in" bar (explanation and button). */
	    private void showSignInBar() {
	        findViewById(R.id.sign_in_bar).setVisibility(View.VISIBLE);
	        findViewById(R.id.sign_out_bar).setVisibility(View.GONE);
	    }

	    /** Shows the "sign out" bar (explanation and button). */
	    private void showSignOutBar() {
	        findViewById(R.id.sign_in_bar).setVisibility(View.GONE);
	        findViewById(R.id.sign_out_bar).setVisibility(View.VISIBLE);
	    }
	    
		@Override
		public void onConnectionSuspended(int i) {
			Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
			mGoogleApiClient.connect();
		}
		  @Override
		    public void onConnectionFailed(ConnectionResult connectionResult) {
		        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

		        if (mResolvingConnectionFailure) {
		            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
		            return;
		        }

		        if (mSignInClicked || mAutoStartSignInFlow) {
		            mAutoStartSignInFlow = false;
		            mSignInClicked = false;
		        }
		        showSignInBar();
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
		
		
