package com.ub.location;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
/**
 * This class is used to provides settings for location timer like set distance,
 * modify location,etc
 * @author vikram
 *
 */
public class Setting extends Activity{

	//TODO : fetch data from settings instead of Utils.java
	ProgressBar myBar;
	TextView lblTopCaption;
	EditText   txtBox1;
	Button btnDoSomething;
	int globalVar = 0; // to be used by threads to exchange data
	int accum = 0;
	long startingMills = System.currentTimeMillis();
	boolean    isRunning = false;
	String     PATIENCE = "Some important data is being collected now. " + 
			"\nPlease be patient...wait... ";
	Handler     myHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		lblTopCaption = (TextView)findViewById(R.id.lblTopCaption);

		myBar = (ProgressBar) findViewById(R.id.myBar);
		myBar.setMax(100); // range goes from 0..100

		txtBox1 = (EditText) findViewById(R.id.txtBox1);
		txtBox1.setHint("Foreground distraction. Enter some data here"); 

		btnDoSomething = (Button)findViewById(R.id.btnDoSomething);
		btnDoSomething.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Editable txt = txtBox1.getText();
				Toast.makeText(getBaseContext(), 
						"You said >> " + txt, 1).show();
			}//onClick        
		});//setOnClickListener
	}

	@Override
	protected void onStart() {
		super.onStart();
		// create & execute background thread were the busy work will be done
		Thread myThreadBack = new Thread(backgroundTask, "backAlias1"  );
		myThreadBack.start();
		myBar.incrementProgressBy(0);
	}

	private Runnable foregroundTask = new Runnable() {
		@Override
		public void run() {
			try {
				int progressStep = 5;
				double totalTime = (System.currentTimeMillis() - startingMills)/1000;

				synchronized(this) {
					globalVar += 100;
				}

				lblTopCaption.setText(PATIENCE + totalTime + " -- " + globalVar);
				myBar.incrementProgressBy(progressStep);
				accum += progressStep;
				if (accum >= myBar.getMax()){
					lblTopCaption.setText("Background work is OVER!");
					myBar.setVisibility(View.INVISIBLE);
				}
			} catch (Exception e) {
				Log.e("<<foregroundTask>>", e.getMessage());

			}
		}

	}; //foregroundTask

	//this is the "Runnable" object that executes the background thread
	private Runnable backgroundTask = new Runnable () {
		@Override
		public void run() {
			//busy work goes here...
			try {
				for (int n=0; n<20; n++) {
					//this simulates 1 sec. of busy activity
					Thread.sleep(1000);
					// now talk to the main thread
					// optionally change some global variable such as: globalVar
					synchronized(this) {
						globalVar += 1;
					}
					myHandler.post(foregroundTask);
				}    
			} catch (InterruptedException e) {
				Log.e("<<foregroundTask>>", e.getMessage());
			}    
		}//run    
	};//backgroundTask

} //Setting Activity
