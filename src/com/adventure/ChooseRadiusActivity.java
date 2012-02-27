package com.adventure;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ZoomButtonsController.OnZoomListener;

import com.components.CircleOverlay;
import com.components.IconOverlay;
import com.components.MyLocationListener;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.obj.AdventureParams;
import com.obj.Global;
import com.obj.PreferencesManager.MeasurementSystem;
import com.util.LengthConverter;
import com.view.AdventureButton;

public class ChooseRadiusActivity extends BaseActivity {
	// Progress update code for UpdateOverlayTask
	private static enum UPDATE_OVERLAY_STATUS {
		ENABLE_ADVENTURE_START,
		ENABLE_GPS_UPDATES,
		DISABLE_GPS_UPDATES,
		UPDATE_OVERLAY
	}
	
	private int MAX_GPS_ERROR;					// Maximum GPS error when saving the adventure path (in meters)
	private int ROUTE_COLOR;					// Color of the radius to be drawn on the map
	
	private static final int START_ZOOM_LEVEL = 15;
	
	// Ui Elements
	AdventureButton btn_find_adventure;
	MapView mapview;
	Spinner spr_radius;
	
    MapController map_controller;
	
	AdventureParams params;		// Adventure parameters passed into the activity

    // AsyncTasks
    UpdateOverlayTask update_overlay_task;		// Updates the map overlay
	
	// Location objects
    LocationManager location_manager;
    MyLocationListener location_listener;
    
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_choose_radius);
        
        // Get the adventure parameters
        params = getIntent().getParcelableExtra(INTENT_ADVENTURE_PARAMS);
        
        // Make sure the params are passed in correctly
        if (params == null) {
        	Global.outputError(ChooseRadiusActivity.this, Global.Error.INTENT_ADVENTURE_PARAMS_ERROR);
        	finish();
        }

        // Initialize some constants
        MAX_GPS_ERROR = getResources().getInteger(R.integer.max_gps_error);
        ROUTE_COLOR = getResources().getInteger(R.integer.route_color);
        
        initUI();
    }

	@Override
	protected void onPause() {
		super.onPause();

		// Disable overlay updates
        update_overlay_task.cancel(true);
        update_overlay_task = null;
	}



	@Override
	protected void onResume() {
		super.onResume();
		
		// Enable location updates
        update_overlay_task = new UpdateOverlayTask();
        update_overlay_task.execute((Void[]) null);
	}

    /**
     * Initialize the UI
     */
    private void initUI() {
    	/* Radius Spinner */
    	spr_radius = (Spinner) findViewById(R.id.spr_radius);
    	int array_id = (Global.preferences_manager.getMeasurement_system() == MeasurementSystem.IMPERIAL) ? R.array.radius_choices_imperial : R.array.radius_choices_metric;
    	spr_radius.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, getResources().getStringArray(array_id)));
    	spr_radius.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedTextView, int position, long id) {
				updateOverlay();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {				
			}
		});
    	
    	/* Find Adventure */
    	btn_find_adventure = (AdventureButton) findViewById(R.id.btn_find_adventure);
    	btn_find_adventure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ChooseRadiusActivity.this, FindAdventureActivity.class);
				
				// Pass the params
				params.setRadius(radiusToMeters());
				params.setLat(location_listener.getLocation().getLatitude());
				params.setLng(location_listener.getLocation().getLongitude());
				i.putExtra(INTENT_ADVENTURE_PARAMS, params);
				
				startActivityForResult(i, INTENT_SETTINGS_RESULTCODE);
			}
		});
    	
    	// Disable the Start Adventure button initially
    	btn_find_adventure.setEnabled(false);
    	
    	/* MapView */
    	mapview = (MapView) findViewById(R.id.mapview);
    	
    	mapview.setBuiltInZoomControls(true);
    	map_controller = mapview.getController();
    	map_controller.setZoom(START_ZOOM_LEVEL);
    	mapview.getZoomButtonsController().setOnZoomListener(new OnZoomListener() {
			@Override
			public void onZoom(boolean zoomIn) {
				// Zoom in/out
				if (zoomIn)
					map_controller.zoomIn();
				else
					map_controller.zoomOut();
				
				// Update the overlay
				updateOverlay();
			}
			
			@Override
			public void onVisibilityChanged(boolean visible) {
				mapview.getZoomButtonsController().setVisible(visible);
			}
		});
    	
        location_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        // Get starting location
    	Location init_user_position = location_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        location_listener = new MyLocationListener(init_user_position);
        
        updateOverlay();
    }
    
    /**
     * Converts a distance in meters to projected pixels on a MapView
     * @param d - the distance, in meters
     * @param latitude - the latitude of the position, in degrees
     * @return - the number of pixels of the radius at the specified latitude
     */
    private float metersToProjectedPixels(int d, double latitude) {
        return mapview.getProjection().metersToEquatorPixels((float) (d * (1/ Math.cos(Math.toRadians(latitude)))));
    }
    
    private int radiusToMeters() {
		// If we're in the imperial measurement system, convert the radius to kilometers
		int radius = Integer.valueOf(spr_radius.getSelectedItem().toString());
		if (Global.preferences_manager.getMeasurement_system() == MeasurementSystem.IMPERIAL)
			radius = (int) LengthConverter.convert(radius, LengthConverter.MILES, LengthConverter.METERS);
		
		return radius;
    }
    
    /**
     * Updates the overlay of the search area on the map
     */
    private void updateOverlay() {
    	// We need to make sure we have a locatino for the user before drawing an overlay
    	if (location_listener.getGeoPoint() == null)
    		return;
    	
    	// Clear the current overlays
        mapview.getOverlays().clear();
        
        // Draw the circle overlay
        int circleWidth = (int) metersToProjectedPixels(radiusToMeters(), location_listener.getLocation().getLatitude());
        
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getHeight();

        //Pixel threshold for when the circle goes off the screen (width returned seems to be very high)
        while(circleWidth > (width/2.5)-(2*40)) {
        	if(mapview.getZoomLevel() > 1) {
        		map_controller.setZoom(mapview.getZoomLevel()-1);
        		circleWidth = (int) metersToProjectedPixels(radiusToMeters(), location_listener.getLocation().getLatitude());
        	}
        	else
        		break;
        }
      //Check to make sure the circle isn't too small
        while(circleWidth < width/7.5) {	//This seven seems to work very well, as 5 makes it too large and 10 makes it too small (find better way?)
        	if(mapview.getZoomLevel() < 15) {	//Check to see if were fully zoomed in
        		map_controller.setZoom(mapview.getZoomLevel()+1);
        		circleWidth = (int) metersToProjectedPixels(radiusToMeters(), location_listener.getLocation().getLatitude());
        	}
        	else
        		break;
        }
        
        // Draw the circle overlay
        CircleOverlay radiusOverlay = new CircleOverlay(location_listener.getGeoPoint(), (int) metersToProjectedPixels(radiusToMeters(), location_listener.getLocation().getLatitude()), ROUTE_COLOR);
        mapview.getOverlays().add(radiusOverlay);
        map_controller.animateTo(location_listener.getGeoPoint());
        
        // Draw the icon overlay
		IconOverlay positionOverlay = new IconOverlay(location_listener.getGeoPoint(), BitmapFactory.decodeResource(getResources(), R.drawable.loc_icon), 0.0f);
		mapview.getOverlays().add(positionOverlay);
		
        mapview.invalidate();
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
    
    private class UpdateOverlayTask extends AsyncTask<Void, UPDATE_OVERLAY_STATUS, Void> {
    	boolean found_good_start = false;
    	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... p) {
			while (!isCancelled()) {
				publishProgress(UPDATE_OVERLAY_STATUS.ENABLE_GPS_UPDATES);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// We don't care about interrupts
				}
				
				if (location_listener.getLocation() != null) {
					if (location_listener.getLocation().getAccuracy() <= MAX_GPS_ERROR) {
						found_good_start = true;
						publishProgress(UPDATE_OVERLAY_STATUS.ENABLE_ADVENTURE_START);
					}
					
					publishProgress(UPDATE_OVERLAY_STATUS.UPDATE_OVERLAY);
				}
				
				if (found_good_start) {
					publishProgress(UPDATE_OVERLAY_STATUS.DISABLE_GPS_UPDATES);
				
					try {
						Thread.sleep(Global.preferences_manager.getLocation_update_interval());
					} catch (InterruptedException e) {
						// We don't care about interrupts
					}
				}
			}
			
			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			
			location_manager.removeUpdates(location_listener);
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(UPDATE_OVERLAY_STATUS... values) {
			super.onProgressUpdate(values);
			
			switch (values[0]) {
			case ENABLE_ADVENTURE_START:
				btn_find_adventure.setEnabled(true);
				break;
			case ENABLE_GPS_UPDATES:
				location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, location_listener);	
				break;
			case DISABLE_GPS_UPDATES:
				location_manager.removeUpdates(location_listener);
				break;
			case UPDATE_OVERLAY:
				updateOverlay();
				break;
			}
		}
    }
}
