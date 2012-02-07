package com.obj;

import com.adventure.R;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
	public static enum MeasurementSystem {
		IMPERIAL,	// 0 in the SharedPreferences
		METRIC		// 1 in the SharedPreferences
	};
	
	Context context;
	
	private int location_update_interval;
	private int route_update_interval;
	
	private MeasurementSystem measurement_system;
	
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	
	public PreferencesManager(Context ctx) {
		context = ctx;
		
		refresh();
	}

	/**
	 * Commits changes sent to the preferences editor and refreshes the current working preferences 
	 */
	public void commit() {
		editor.commit();
		
		refresh();
	}
	
	/**
	 * Refreshes the preferences based on what's in persistent storage
	 */
	public void refresh() {
		preferences = context.getSharedPreferences(Global.SHARED_PREFS, Context.MODE_PRIVATE);
		editor = preferences.edit();
		
		location_update_interval = preferences.getInt(context.getResources().getString(R.string.location_update_interval), context.getResources().getInteger(R.integer.location_update_interval));
		route_update_interval = preferences.getInt(context.getResources().getString(R.string.route_update_interval), context.getResources().getInteger(R.integer.route_update_interval));
		
		measurement_system = preferences.getInt(context.getResources().getString(R.string.measurement_system), 0) == 0 ? MeasurementSystem.IMPERIAL : MeasurementSystem.METRIC;
	}

	public int getLocation_update_interval() {
		return location_update_interval;
	}

	public MeasurementSystem getMeasurement_system() {
		return measurement_system;
	}

	
	public int getRoute_update_interval() {
		return route_update_interval;
	}
	
	public void setLocation_update_interval(int v) {
		editor.putInt(context.getResources().getString(R.string.location_update_interval), v);
	}

	public void setMeasurement_system(MeasurementSystem ms) {
		editor.putInt(context.getResources().getString(R.string.measurement_system), ms == MeasurementSystem.IMPERIAL ? 0 : 1);
	}
	
	public void setRoute_update_interval(int v) {
		editor.putInt(context.getResources().getString(R.string.route_update_interval), v);
	}
}
