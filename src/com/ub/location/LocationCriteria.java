package com.ub.location;

import android.location.Criteria;

/**
 * This class is used to provide criteria for location
 * @author vikram
 *
 */
public class LocationCriteria extends Criteria {

	public LocationCriteria() {
		super();
		setAccuracy(ACCURACY_FINE);		
//		setAltitudeRequired(false);
//		setSpeedRequired(false);
//		setCostAllowed(false);
	}
}
