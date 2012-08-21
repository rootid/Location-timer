package com.ub.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
/**
 * This class creates location  service to monitor user location continuously.
 * @author vikram
 *
 */
//Bounded service
public class LocationService extends Service implements LocationListener{

	LocationManager locationMgr;
	int mStartMode;       // indicates how to behave if the service is killed
	IBinder mBinder;      // interface for clients that bind
	boolean mAllowRebind; // indicates whether onRebind should be used
	Handler handler ;
	double[] locArray = new double[2];

	public LocationService() {
		handler = new Handler();
	}

	public class MyBinder extends Binder{
		LocationService getService(){
			return LocationService.this;
		}
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return new MyBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		handler.removeCallbacks(getData);
		handler.post(getData);
		super.onStart(intent, startId);
	}
	
	@Override
	public void onDestroy() {
		handler.removeCallbacks(getData);
        locationMgr.removeUpdates(this);
		super.onDestroy();
	}

	private final Runnable getData = new Runnable() {
		public void run() {
			getDataFrame();      
		}
	};

	private void getDataFrame() {
		startGPS();
		handler.postDelayed(getData,10*1000);
	}

	@Override
	public void onLocationChanged(Location location) {
		if ((location != null))
		{
			Log.d("Latitude", String.valueOf(location.getLatitude()));
			Log.d("Longitude", String.valueOf(location.getLongitude()));
			sendServiceActiveBroadcast(location.getLatitude(),location.getLongitude());
		}


	}
	
	private final void sendServiceActiveBroadcast(final double latitude,final double longitude) {
		final Intent intent = new Intent();
		locArray[0] = latitude;
		locArray[1] = longitude;
		intent.setAction( "com.ub.location.LOCATION_SERVICES" );
		intent.addCategory( "com.yourtld.android.CATEGORY" );
		intent.putExtra( "latlong", locArray );
		this.sendBroadcast( intent );

	}

	@Override
	public void onProviderDisabled(String provider) {

	}
	@Override
	public void onProviderEnabled(String provider) {

	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	public void startGPS()
	{

		locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//		locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1,1,this);
		locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);

		Log.d("GPS", "GPS Activé");
	}

	public void stopGPS()
	{

		locationMgr.removeUpdates(this);

	}


}
