package com.adventure;

import java.util.ArrayList;

import android.content.ContentValues;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.components.IconOverlay;
import com.components.RouteOverlay;
import com.google.android.maps.MapView;
import com.obj.Adventure;
import com.obj.Global;
import com.obj.ParcelableGeoPoint;
import com.util.DataHelper;

public class ViewAdventureActivity extends BaseActivity {
	private int ROUTE_COLOR;					// The color of the route on the map
	
	// Ui Elements
	MapView mapview;
	TextView tv_adventure_name;
	TextView tv_distance_traveled;
	TextView tv_duration;
	TextView tv_start_time;
	
	// Says if the Activity received an Adventure
	// If it has, we'll save it to the database and use its Name field
	// If it hasn't, we'll use the destination intent and not save it
	boolean hasAdventure = true;
	
	Adventure adventure = null;								// Adventure that was passed into the activity
	ArrayList<ParcelableGeoPoint> adventure_path = null;	// Path the user took on this adventure passed into the activity
	
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_adventure_complete);
        
        // Get the adventure
        adventure = getIntent().getParcelableExtra(INTENT_ADVENTURE);
        
        // Error if the adventure is passed incorrectly
        if (adventure == null) {
        	hasAdventure = false;
        }
        
        // Get the adventure path
        adventure_path = getIntent().getParcelableArrayListExtra(INTENT_ADVENTURE_PATH);
        
        // Error if the adventure path is passed incorrectly
        if (adventure_path == null) {
        	Global.outputError(ViewAdventureActivity.this, Global.Error.INTENT_ADVENTURE_PATH);
        	
        	finish();
        	return;
        }
        
        // Convert the adventure path from a ParcelableGeoPoint to just a normal GeoPoint
        String coord_string = ParcelableGeoPoint.arrayListToString(adventure_path);

        // Initialize constants
        ROUTE_COLOR = getResources().getInteger(R.integer.route_color);
        
        initUI();
        
        if (hasAdventure) {
        	UpdateDatabaseTask update_database_task = new UpdateDatabaseTask();
        	update_database_task.execute(new String[] { coord_string });
        }
    }
    
    /**
     * Initialize the UI
     */
    private void initUI() {
    	/* MapView */
    	mapview = (MapView) findViewById(R.id.mapview);
    		
		mapview.getOverlays().clear();

		// Max and min latitudes of the route; we use these to find the span of the mapview
		int max_latitude = adventure_path.get(0).getGeoPoint().getLatitudeE6();
		int min_latitude = adventure_path.get(0).getGeoPoint().getLatitudeE6();
		
		// Max and min longitudes of the route; we use these to find the span of the mapview
		int max_longitude = adventure_path.get(0).getGeoPoint().getLongitudeE6();
		int min_longitude = adventure_path.get(0).getGeoPoint().getLongitudeE6();
		
		// Draw the route
	 	for (int i = 1; i < adventure_path.size(); i++) {
	 		mapview.getOverlays().add(new RouteOverlay(adventure_path.get(i - 1).getGeoPoint(), adventure_path.get(i).getGeoPoint(), ROUTE_COLOR));
	 		
	 		max_latitude = (max_latitude < adventure_path.get(i).getGeoPoint().getLatitudeE6()) ? adventure_path.get(i).getGeoPoint().getLatitudeE6() : max_latitude;
	 		min_latitude = (min_latitude > adventure_path.get(i).getGeoPoint().getLatitudeE6()) ? adventure_path.get(i).getGeoPoint().getLatitudeE6() : min_latitude;
	 		
	 		max_longitude = (max_longitude < adventure_path.get(i).getGeoPoint().getLongitudeE6()) ? adventure_path.get(i).getGeoPoint().getLongitudeE6() : max_longitude;
	 		min_longitude = (min_longitude > adventure_path.get(i).getGeoPoint().getLongitudeE6()) ? adventure_path.get(i).getGeoPoint().getLongitudeE6() : min_longitude;
	 	}
	    
	 	// Fit the route in the mapview
	 	mapview.getController().zoomToSpan(max_latitude - min_latitude, max_longitude - min_longitude);
	 	
	 	// Draw the start and end points
 		// TODO MAKE START ICON
 		mapview.getOverlays().add(new IconOverlay(adventure_path.get(0).getGeoPoint(), BitmapFactory.decodeResource(getResources(), R.drawable.loc_icon), 0.0f));
 		// TODO MAKE END ICON
 		mapview.getOverlays().add(new IconOverlay(adventure_path.get(adventure_path.size() - 1).getGeoPoint(), BitmapFactory.decodeResource(getResources(), R.drawable.loc_icon), 0.0f));
	 	
	 	mapview.invalidate();
	 	
	 	/* Adventure Name TextView */
	 	tv_adventure_name = (TextView) findViewById(R.id.tv_adventure_name);
	 	if (hasAdventure)
	 		tv_adventure_name.setText(adventure.getName());
	 	else
	 		tv_adventure_name.setText(getIntent().getStringExtra(INTENT_DESTINATION));
	 	
	 	/* Distance Traveled TextView */
	 	tv_distance_traveled = (TextView) findViewById(R.id.tv_distance_traveled);
	 	tv_distance_traveled.setText(getIntent().getStringExtra(INTENT_DISTANCE_TRAVELED));
	 	
	 	/* Duration TextView */
	 	tv_duration = (TextView) findViewById(R.id.tv_duration);
	 	tv_duration.setText(String.valueOf(getIntent().getLongExtra(INTENT_DURATION, 0)));
	 	
	 	/* Start Time TextView */
	 	tv_start_time = (TextView) findViewById(R.id.tv_start_time);
	 	tv_start_time.setText(getIntent().getStringExtra(INTENT_START_TIME));
    }
    
    private class UpdateDatabaseTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
	        ContentValues values = new ContentValues();
	        values.put(DataHelper.Column.DESTINATION, adventure.getName());
	        values.put(DataHelper.Column.DISTANCE_TRAVELED, getIntent().getStringExtra(INTENT_DISTANCE_TRAVELED));
	        values.put(DataHelper.Column.DURATION, String.valueOf(getIntent().getLongExtra(INTENT_DURATION, 0)));
	        values.put(DataHelper.Column.ROUTE, params[0]);
	        values.put(DataHelper.Column.START_TIME, getIntent().getStringExtra(INTENT_START_TIME));
	        
	        DataHelper dataHelper = new DataHelper(ViewAdventureActivity.this);
	        dataHelper.insert(DataHelper.Table.HISTORY, values);
	        dataHelper.close();
			return null;
		}
    }
}