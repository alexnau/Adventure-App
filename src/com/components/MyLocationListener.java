package com.components;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class MyLocationListener implements LocationListener {
	private Location location = null;
	
	public MyLocationListener(Location l) {
		location = l;
	}
	
	public void onLocationChanged(Location loc) {
        if (loc != null) {
        	location = loc;
        	
			Log.i(this.getClass().getCanonicalName(), "Location Updated");
        }
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    
    public GeoPoint getGeoPoint() {
    	if (location == null)
    		return null;

    	GeoPoint p = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
    	
    	return p;
    }
    
    public Location getLocation() {
    	return location;
    }
}