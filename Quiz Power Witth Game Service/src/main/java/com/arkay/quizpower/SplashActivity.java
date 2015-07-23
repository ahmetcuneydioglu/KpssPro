package com.arkay.quizpower;

import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Splash Screen activity
 * @author Arkay
 *
 */
		
		
public class SplashActivity extends Activity {
	protected boolean _active=true;
	protected int _splashTime=2500;
	InputStream inputStream=null;
	public RelativeLayout main_home_layout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		
		main_home_layout = (RelativeLayout)findViewById(R.id.main_home_layout);
		main_home_layout.setBackgroundColor(getResources().getColor(R.color.app_background_color_1));
			
		Thread t = new Thread(){
        	@Override
        	public void run(){
        		try {
					Thread.sleep(200);
					_active = true;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
      	     
        		
        	}
        };
        t.start();

        Thread splshThread = new Thread(){
			@Override
			public void run(){

				try{
					int wainted = 0;
					while(_active && (wainted<_splashTime)){
						sleep(100);
						if(_active){
							wainted+=100;
						}
					}
				}catch(InterruptedException ex){
					
				}finally{
					
					startMainScreen();
					inputStream=null;
					finish();
					
				}
			}
		};
		splshThread.start();
		
		
		
	}
	public void startMainScreen(){
		Intent inst = new Intent(this,MenuHomeScreenActivity.class);
		inst.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(inst);
		
	}
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			_active = false;
		}
		return true;
	}
}
