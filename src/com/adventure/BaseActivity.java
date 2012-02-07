package com.adventure;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.maps.MapActivity;

public class BaseActivity extends MapActivity {
	static final int INTENT_SETTINGS_RESULTCODE = 0;
	
	static final String INTENT_ADVENTURE_PARAMS = "intent_adventure_params";
	static final String INTENT_ADVENTURE_PARAMS_COMPLETE = "intent_adventure_params_complete";
	static final String INTENT_ADVENTURE = "intent_adventure";
	static final String INTENT_ADVENTURE_PATH = "intent_adventure_path";
	static final String INTENT_DISTANCE_TRAVELED = "intent_distance_traveled";
	static final String INTENT_DURATION = "intent_duration";
	static final String INTENT_START_TIME = "intent_start_time";
	
	@Override
	protected void onResume() {
		super.onResume();
		
		checkGPS();
	}
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == INTENT_SETTINGS_RESULTCODE) {
			if (data != null && data.getBooleanExtra(INTENT_ADVENTURE_PARAMS_COMPLETE, false)) {
				setResult(Activity.RESULT_OK, data);
				finish();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = null;
		
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.settings:
	    	i = new Intent(BaseActivity.this, PreferencesActivity.class);
	    	startActivity(i);
	    	
	    	return true;
	    case R.id.about:
	    	//i = new Intent(BaseActivity.this, AboutActivity.class);
	    	//startActivity(i);

	        return true;
	    default:
	        return false;
	    }
	}
	
	/**
	 * Checks if GPS is enabled and prompts the user to enable it if necessary
	 */
	protected void checkGPS() {
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("This app requires that your GPS be enabled. Would you like to enable it now?")
					.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
								}})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
									builder.setMessage("This application will now exit.")
											.setPositiveButton("OK", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													// TODO EXIT APPLICATION HERE
													finish();
												}});
									
									AlertDialog alert = builder.create();
									alert.show();
								}});
			
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
 