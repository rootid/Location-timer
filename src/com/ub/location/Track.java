package com.ub.location;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import android.R.bool;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Geocoder;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
/**
 * This class fetches co-ordinates continuously from service. 
 * @author vikram
 *
 */
public class Track extends Activity implements OnClickListener, ServiceConnection{

	private TextView textView;
	private TextView timerView;
	private Button timerButton;
	private  long startTime = 0L;
	private Location src;
	double dist = 0;
	private boolean isFirstTime = true;
	private Geocoder geoCoder;
	private Handler handler;
	private boolean isRegistered = false ;
	private double startLat;
	private double startLong;
	private Intent intent ;
	public static final String TRACK_TAG = "track";
	
	private static final String SERVICE_BROADCAST_ACTION = "com.ub.location.LOCATION_SERVICES";

	public Track() {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.track);

//		locationFetch = new LocationFetch(this);
		//locationFetch.addObserver(this);

		textView = (TextView) findViewById(R.id.locTextView);
		timerButton = (Button) findViewById(R.id.timerButton);
		timerView  = (TextView) findViewById(R.id.timer_Text_View);
		timerButton.setOnClickListener(this);

		geoCoder = new Geocoder(this,Locale.getDefault());	

	}



	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.timerButton) {
			if(timerButton.getText().equals("Start")) {
				startTimer();
			}
			else if(timerButton.getText().equals("Stop")){
				stopTimer();			
			}
		}

	}



	private void startTimer() {

		if(startTime == 0L) {	
			//Load the current location
			boolean isWifiAvailable = isOnline(); 
			textView.setText("Loading location ....");
			timerView.setText("Timer starting...");
			handler = new Handler();
			//			LocationManager locMgr = (LocationManager) this. getSystemService(Context.LOCATION_SERVICE);
			//			locTask = new LocationTask(locMgr, handler,updateUI,isWifiAvailable);
			//			locTask.getLocationUpdate();


			intent = new Intent(Track.this,LocationService.class);
			//bindService(intent, this, Context.BIND_AUTO_CREATE); 
			RegisterIntentRecv();
			startService(intent); 
		
			timerButton.setText("Stop");
		}

	}

	BroadcastReceiver serviceReceiver;
	private void RegisterIntentRecv() {
		final IntentFilter serviceActiveFilter = new IntentFilter( SERVICE_BROADCAST_ACTION );

		serviceActiveFilter.addCategory( "com.yourtld.android.CATEGORY" );

		this.serviceReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive( final Context context, final Intent intent ) {
				if( intent != null ) {
					double [] test = intent.getDoubleArrayExtra("latlong");
					if(test.length >0 && isFirstTime) {
						startLat = test[0];
						startLong = test[1];
						textView.setText(" "+"Lattitude : "+startLat +", \n Longitude : "+startLong);
						isFirstTime = false;
						
						//start timer
						startTime = SystemClock.uptimeMillis();
						handler.removeCallbacks(timerThread);
						handler.postDelayed(timerThread, 100);
						isRegistered = true;
					}
					else  if(test.length > 0){
						double lat = test[0];
						double longs = test[1];
					//	getDistanceBetween(lat,longs);
						dist  = distFrom(startLat, startLong, lat, longs);
						 DecimalFormat df = new DecimalFormat("#.##");
						textView.setText(" From :\n"+"Lat : "+startLat +", Long : "+startLong
								+ "\n To :\n"+"Lat: "+lat + ", Long :"+longs //);
								+"\n Distance (in meters) :\n "+df.format(dist));
						if(dist > Utils.MIN_DISTANCE) {
							stopTimer();
						}
					}
				}
			}

		};
		this.registerReceiver( this.serviceReceiver, serviceActiveFilter );
		
	}

	public  double distFrom(double lat1, double lng1, double lat2, double lng2) {
		double miles = 1609.344;
	    double earthRadius = 3958.75;
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;
	    dist = dist * miles;
	    return dist;
	    }


	public boolean isGPSAvailable() {
		String gpsProvider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(gpsProvider.equalsIgnoreCase("")) {
			return false;
		}
		else {
			return true;
		}

	}

	public boolean isOnline() {
		boolean var = false;
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		if ( cm.getActiveNetworkInfo() != null ) {
			var = true;
		}
		return var;
	} 

	private void stopTimer() {
		handler.removeCallbacks(timerThread);
		timerButton.setText("Start");
		startTime = 0L;
		if(isRegistered) {
			this.unregisterReceiver(this.serviceReceiver);
			stopService(intent);
			isRegistered = false;
		}
		isFirstTime = true;
		
		//stop the service
		//stopService(intent);
		//unbindService(this);
		//locTask.stopLocationUpdate();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}


	@Override
	protected void onPause() {
		super.onPause();		
		stopTimer();
	}

	@Override
	protected void onStop() {
		super.onStop();
		//stopTimer();
	}


	private Runnable timerThread =  new Runnable() {

		public void run() {
			long time = SystemClock.uptimeMillis() - startTime;
			int sec = (int) (time / 1000);
			int min = sec / 60;
			sec = sec % 60;
			timerView.setText("Elapsed Time : "+ min + "  "+ String.format("%02d", sec) );
			handler.postDelayed(this, 200);
		}

	};



	private Address getAddressInfoFromLocation (Location loc) {
		List<Address> addressList;
		Address addressInfo = null;
		try {
			addressList = geoCoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);

			if(addressList != null && addressList.size() > 0) {
				addressInfo = addressList.get(0);
				//				address = addressInfo.getAddressLine(0);
				//				locality = addressInfo.getLocality();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return addressInfo;
	}

//	Runnable updateUI = new Runnable() {
//
//		@Override
//		public void run() {
//			textView.setText("Location updated");
//			Location loc = locTask.getLocation();
//			Address addressInfo;
//			if(loc != null && isFirstTime  ) {
//				src = loc;
//				getDistanceFromSource(loc);
//				addressInfo= getAddressInfoFromLocation(loc);
//				if(addressInfo != null) {
//					textView.setText(" \n" +addressInfo.getAddressLine(0) + ", " + addressInfo.getLocality()+ "\n distance :" + distance);
//				}
//				else {
//					textView.setText(" \n" +loc.getLatitude() + ", " +loc.getLongitude()+ "\n distance :" + distance);
//				}
//				isFirstTime = false;
//				locTask.stopLocationUpdate();
//
//				//Start the timer
//				startTime = SystemClock.uptimeMillis();
//				handler.removeCallbacks(timerThread);
//				handler.postDelayed(timerThread, 100);
//				locTask.getPeriodicUpdate();
//			}
//			else if (loc != null){
//				getDistanceFromSource(loc);
//				addressInfo = getAddressInfoFromLocation(loc);
//				Address addressInfo1 = getAddressInfoFromLocation(src);
//				if(addressInfo1 != null) {
//					textView.setText("From  : " +addressInfo.getAddressLine(0) + "," +
//							" " + addressInfo.getLocality()+ "\n To  : " + addressInfo1.getAddressLine(0) +"," +
//							""+addressInfo1.getLocality() + "\n Distance (in meters) : "+distance);
//				}
//				else {
//					textView.setText("From  : " +loc.getLatitude() + "," +
//							" " + loc.getLongitude()+ "\n To  : " + src.getLatitude() +"," +
//							""+src.getLongitude() + "\n Distance (in meters) : "+distance);
//				}
//				if(distance > 50.0) {
//					stopTimer();
//				}
//
//			}
//		}
//	};
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TRACK_TAG,"service connected");

	}

	@Override
	public void onServiceDisconnected(ComponentName name) {


	}
}
