package com.adventure;

import java.util.ArrayList;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.TextView;

import com.components.IconOverlay;
import com.components.RouteOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.obj.Adventure;
import com.obj.Global;
import com.obj.ParcelableGeoPoint;

public class AdventureCompleteActivity extends BaseActivity {
	private int ROUTE_COLOR;					// The color of the route on the map
	
	// Ui Elements
	MapView mapview;
	TextView tv_adventure_name;
	
	Adventure adventure = null;					// Adventure that was passed into the activity
	ArrayList<GeoPoint> adventure_path = null;	// Path the user took on this adventure passed into the activity
	
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_adventure_complete);
        
        // Get the adventure
        adventure = getIntent().getParcelableExtra(INTENT_ADVENTURE);
        
        // Error if the adventure is passed incorrectly
        if (adventure == null) {
        	Global.outputError(AdventureCompleteActivity.this, Global.Error.INTENT_ADVENTURE_ERROR);
        	finish();
        }
        
        // Get the adventure path
        ArrayList<ParcelableGeoPoint> parcelable_adventure_list = getIntent().getParcelableArrayListExtra(INTENT_ADVENTURE_PATH);
        
        // Error if the adventure path is passed incorrectly
        if (parcelable_adventure_list == null) {
        	Global.outputError(AdventureCompleteActivity.this, Global.Error.INTENT_ADVENTURE_PATH);
        	finish();
        }
        
        // Convert the adventure path from a ParcelableGeoPoint to just a normal GeoPoint
        adventure_path = new ArrayList<GeoPoint>();
        for (int i = 0; i < parcelable_adventure_list.size(); i++)
        	adventure_path.add(parcelable_adventure_list.get(i).getGeoPoint());

        // Initialize constants
        ROUTE_COLOR = getResources().getInteger(R.integer.route_color);
        
        initUI();
    }
    
    /**
     * Initialize the UI
     */
    private void initUI() {
    	/* MapView */
    	mapview = (MapView) findViewById(R.id.mapview);
    		
		mapview.getOverlays().clear();

		// Max and min latitudes of the route; we use these to find the span of the mapview
		int max_latitude = adventure_path.get(0).getLatitudeE6();
		int min_latitude = adventure_path.get(0).getLatitudeE6();
		
		// Max and min longitudes of the route; we use these to find the span of the mapview
		int max_longitude = adventure_path.get(0).getLongitudeE6();
		int min_longitude = adventure_path.get(0).getLongitudeE6();
		
		// Draw the route
	 	for (int i = 1; i < adventure_path.size(); i++) {
	 		mapview.getOverlays().add(new RouteOverlay(adventure_path.get(i - 1), adventure_path.get(i), ROUTE_COLOR));
	 		
	 		max_latitude = (max_latitude < adventure_path.get(i).getLatitudeE6()) ? adventure_path.get(i).getLatitudeE6() : max_latitude;
	 		min_latitude = (min_latitude > adventure_path.get(i).getLatitudeE6()) ? adventure_path.get(i).getLatitudeE6() : min_latitude;
	 		
	 		max_longitude = (max_longitude < adventure_path.get(i).getLongitudeE6()) ? adventure_path.get(i).getLongitudeE6() : max_longitude;
	 		min_longitude = (min_longitude > adventure_path.get(i).getLongitudeE6()) ? adventure_path.get(i).getLongitudeE6() : min_longitude;
	 	}
	    
	 	// Fit the route in the mapview
	 	mapview.getController().zoomToSpan(max_latitude - min_latitude, max_longitude - min_longitude);
	 	
	 	// Draw the start and end points
 		// TODO MAKE START ICON
 		mapview.getOverlays().add(new IconOverlay(adventure_path.get(0), BitmapFactory.decodeResource(getResources(), R.drawable.loc_icon), 0.0f));
 		// TODO MAKE END ICON
 		mapview.getOverlays().add(new IconOverlay(adventure_path.get(adventure_path.size() - 1), BitmapFactory.decodeResource(getResources(), R.drawable.loc_icon), 0.0f));
	 	
	 	mapview.invalidate();
	 	
	 	/* Adventure Name TextView */
	 	tv_adventure_name = (TextView) findViewById(R.id.tv_adventure_name);
	 	tv_adventure_name.setText(adventure.getName());
    }
}