package com.adventure;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import com.obj.Global;
import com.obj.PreferencesManager.MeasurementSystem;

public class PreferencesActivity extends PreferenceActivity {
	Preference location_update_interval;
	Preference measurement_system;
	Preference route_update_interval;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);

	    measurement_system = (Preference) findPreference(getResources().getString(R.string.measurement_system));	    
	    measurement_system.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String[] array = getResources().getStringArray(R.array.measurement_systems);
				Global.preferences_manager.setMeasurement_system(((String) newValue).equals(array[0]) ? MeasurementSystem.IMPERIAL : MeasurementSystem.METRIC);
				
				return true;
			}
		});
	    
	    location_update_interval = (Preference) findPreference(getResources().getString(R.string.location_update_interval));	    
	    location_update_interval.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Global.preferences_manager.setLocation_update_interval(Integer.parseInt((String) newValue));
				
				return true;
			}
		});
	    
	    route_update_interval = (Preference) findPreference(getResources().getString(R.string.route_update_interval));	    
	    route_update_interval.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Global.preferences_manager.setRoute_update_interval(Integer.parseInt((String) newValue));
				
				return true;
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		Global.preferences_manager.commit();
	}
}