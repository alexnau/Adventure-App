package com.adventure;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.components.CircleOverlay;
import com.components.DirectionsAdapter;
import com.components.IconOverlay;
import com.components.MyLocationListener;
import com.components.MySensorListener;
import com.components.RouteOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.obj.Adventure;
import com.obj.Global;
import com.obj.NavigationDataSet;
import com.obj.NavigationSaxHandler;
import com.obj.ParcelableGeoPoint;
import com.obj.Placemark;
import com.obj.PreferencesManager.MeasurementSystem;
import com.util.LengthConverter;

public class AdventureActivity extends BaseActivity {
	// Progress update codes for UpdateIconOverlayTask
	private static enum UPDATE_ICON_OVERLAY_STATUS {
		UPDATE_ICON_OVERLAY	
	}
	
	// Progress update codes for UpdateLocationTask
	private static enum UPDATE_LOCATION_STATUS {
		ENABLE_GPS_UPDATES,
		DISABLE_GPS_UPDATES
	}
	
	// Progress update codes for UpdateAdventureProgressTask
	private static enum UPDATE_ADVENTURE_PROGRESS_STATUS {
		UPDATE_ADVENTURE	
	}
	
	// Constants
	private double ADVENTURE_COMPLETE_RADIUS;			// Defines the radius around the goal that the user must enter to complete the adventure (in meters)
	private int ICON_TIME_UPDATE_INTERVAL;				// Update time for the icon on the map and compass screens (in seconds)
	private int MAX_GPS_ERROR;							// Maximum GPS error when saving the adventure path (in meters)
	private int ROUTE_COLOR;							// The color of the route on the map
	
	private int compass_icon = R.drawable.nav_icon;		// The compass icon id
	
	// Ui Elements
	Button btn_compass;
	Button btn_directions;
	Button btn_mapview;
	FrameLayout fl_compass_frame;
	FrameLayout fl_content;
	ListView lv_directions;
	MapView mapview;
	Panel p_compass;
	TextView tv_distance_left;
	TextView tv_distance_travelled;
	TextView tv_time_left;
	TextView tv_time_travelled;
	ViewFlipper vf_flipper;
	
	MapController map_controller;
	
	Adventure adventure = null;							// The passed in adventure object
	
    // Adventure directions
	ArrayList<Placemark> placemarks_list = new ArrayList<Placemark>();		// The list of placemarks returned by UpdateAdventureProgressTask 
	DirectionsAdapter directions_adapter;									// The adapter for the directions ListView 

	// Adventure route
	ArrayList<GeoPoint> adventure_route = new ArrayList<GeoPoint>();								// The route of the adventure that is drawn on the map
	ArrayList<ParcelableGeoPoint> adventure_path_taken = new ArrayList<ParcelableGeoPoint>();		// The path taken by the user thus far
	
	// AsyncTasks
	AdventureCompleteTask adventure_complete_task = null;	// Checks for adventure completion
	UpdateIconOverlayTask update_icon_task = null;			// Updates the icon on the map
	UpdateLocationTask update_location_task = null;			// Updates the user's location in the location listener
	UpdateAdventureProgress update_route_task = null;		// Updates the progress of the adventure (route and directions)
	
	// Location objects
    LocationManager location_manager;
    MyLocationListener location_listener;
    
	NavigationDataSet directions = null;	// The set of directions returned by UpdateAdventureProgress
    
    // Sensor Objects
    MySensorListener sensor_listener;
    SensorManager sensor_manager;
	
	// Timer Objects
	private Handler adventure_timer = new Handler();		// The Handler for the timer
	private long start_time = -1;							// The start time of the adventure
	private long curr_time = -1;							// THe current time
	private Runnable update_timer = new Runnable() {		// Runnable for updating the timer
	   public void run() {
	       final long start = start_time;
	       curr_time = System.currentTimeMillis();
	       long millis = curr_time - start;
	       int seconds = (int) (millis / 1000);
	       int minutes = seconds / 60;
	       int hours   = minutes / 60;
	       seconds     = seconds % 60;
	
	       String text = "";
	       
	       if (hours > 0)
	    	   text += hours + ":";
	       
	       if (seconds < 10)
	    	   text += minutes + ":0" + seconds;
	       else
	    	   text += minutes + ":" + seconds;
	       
	       tv_time_travelled.setText(text);
	     
	       adventure_timer.postDelayed(update_timer, 1000);
	   }

	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_adventure);
        
        adventure = getIntent().getParcelableExtra(INTENT_ADVENTURE);
        
        if (adventure == null) {
        	Global.outputError(AdventureActivity.this, Global.Error.INTENT_ADVENTURE_ERROR);
        	finish();
        }
        
    	// Initialize LocationManager
        location_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        // Get starting location
    	Location init_user_position = location_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        location_listener = new MyLocationListener(init_user_position);
    	
        // Initialize SensorManager
        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor_listener = new MySensorListener(Sensor.TYPE_ORIENTATION);

        // Initialize some constants
        ADVENTURE_COMPLETE_RADIUS = (double) getResources().getInteger(R.integer.adventure_complete_radius);
		ICON_TIME_UPDATE_INTERVAL = getResources().getInteger(R.integer.icon_update_interval);
		MAX_GPS_ERROR = getResources().getInteger(R.integer.max_gps_error);
        ROUTE_COLOR = getResources().getInteger(R.integer.route_color);
		
        initUI();
    }
    
	@Override
	protected void onPause() {
		super.onPause();
		
		// Disable the SensorManager when not on this Activity
        sensor_manager.unregisterListener(sensor_listener);
		
		// Disable adventure complete check
        adventure_complete_task.cancel(true);
        adventure_complete_task = null; 
        
		// Disable orientation updates
		update_icon_task.cancel(true);
		update_icon_task = null; 
        
		// Disable location updates
		update_location_task.cancel(true);
		update_location_task = null;
		
		// Disable route updates
		update_route_task.cancel(true);
		update_route_task = null;
		
		adventure_route.clear();
		placemarks_list.clear();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Get orientation updates
        sensor_manager.registerListener(sensor_listener, sensor_manager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);

		// Disable adventure complete check
        adventure_complete_task = new AdventureCompleteTask();
        adventure_complete_task.execute((Void[]) null); 
        
		// Start getting orientation updates
		update_icon_task = new UpdateIconOverlayTask();
		update_icon_task.execute((Void[]) null); 
        
		// Start getting location updates
		update_location_task = new UpdateLocationTask();
		update_location_task.execute((Void[]) null); 
        
		// Start getting route updates
		update_route_task = new UpdateAdventureProgress();
		update_route_task.execute((Void[]) null); 
	}
    
    /**
     * Initialize the UI
     */
    private void initUI() {
    	/* Loading ViewFlipper */
    	vf_flipper = (ViewFlipper) findViewById(R.id.vf_flipper);
    	
    	/* MapView */
    	mapview = (MapView) findViewById(R.id.mapview);

    	mapview.setBuiltInZoomControls(false);
    	map_controller = mapview.getController();
    	map_controller.setZoom(19);

    	/* Directions ListView */
    	lv_directions = (ListView) findViewById(R.id.lv_directions);
    	lv_directions.setOnTouchListener(null);

    	directions_adapter = new DirectionsAdapter(AdventureActivity.this, R.layout.li_directions, placemarks_list);
    	lv_directions.setAdapter(directions_adapter);
    	
    	/* Content FrameLayout */
    	fl_content = (FrameLayout) findViewById(R.id.fl_content);
    	
    	/* Compass Frame */
    	fl_compass_frame = (FrameLayout) findViewById(R.id.fl_compass_frame);
    	p_compass = new Panel(AdventureActivity.this);
    	fl_compass_frame.addView(p_compass);
    	
    	/* Distance Left TextBox */
    	tv_distance_left = (TextView) findViewById(R.id.tv_distance_left);
    	
    	/* Time Left TextBox */
    	tv_time_left = (TextView) findViewById(R.id.tv_time_left);
    	
    	/* Distance Travelled TextBox */
    	tv_distance_travelled = (TextView) findViewById(R.id.tv_distance_travelled);
    	
    	/* Time Travelled TextBox */
    	tv_time_travelled = (TextView) findViewById(R.id.tv_time_travelled);
    	
    	Display display = getWindowManager().getDefaultDisplay();
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(display.getWidth() / 3, LinearLayout.LayoutParams.WRAP_CONTENT);
    	
    	/* Compass Button */
    	btn_compass = (Button) findViewById(R.id.btn_compass);
    	btn_compass.setLayoutParams(params);
    	btn_compass.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				// TODO THIS IS FOR DEBUG ONLY
				
				
				// Parse the start date/time into an actual date
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(start_time);
				java.util.Date date = cal.getTime();
				
    			Intent i = new Intent(AdventureActivity.this, ViewAdventureActivity.class);
    			i.putExtra(INTENT_ADVENTURE, adventure);
    			i.putExtra(INTENT_ADVENTURE_PATH, adventure_path_taken);
    			i.putExtra(INTENT_DISTANCE_TRAVELED, tv_distance_travelled.getText().toString());
    			i.putExtra(INTENT_DURATION, System.currentTimeMillis() - start_time);
    			i.putExtra(INTENT_START_TIME, date.getHours() + ":" + date.getMinutes() + " " + date.getDate() + "-" + (date.getMonth() + 1) + "-" + date.getYear());	// This is a placeholder for the line below it
    			// i.putExtra(INTENT_START_TIME, date.toString());		// TODO FORMAT DATE
    			startActivity(i);
			
    			finish();
				
    			// TODO THIS IS THE ACTUAL IMPLEMENTATION OF THIS METHOD
				/*fl_content.getChildAt(2).setVisibility(View.VISIBLE);
				
				fl_content.getChildAt(0).setVisibility(View.GONE);
				fl_content.getChildAt(1).setVisibility(View.GONE);*/
			}
		});
    	
    	/* Directions Button */
    	btn_directions = (Button) findViewById(R.id.btn_directions);
    	btn_directions.setLayoutParams(params);
    	btn_directions.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				fl_content.getChildAt(1).setVisibility(View.VISIBLE);
				
				fl_content.getChildAt(0).setVisibility(View.GONE);
				fl_content.getChildAt(2).setVisibility(View.GONE);
			}
		});
    	
    	/* MapView Button */
    	btn_mapview = (Button) findViewById(R.id.btn_mapview);
    	btn_mapview.setLayoutParams(params);
    	btn_mapview.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				fl_content.getChildAt(0).setVisibility(View.VISIBLE);
				
				fl_content.getChildAt(1).setVisibility(View.GONE);
				fl_content.getChildAt(2).setVisibility(View.GONE);
			}
		});
    }
    
    /**
     * Updates the directions if necessary
     * @param placemarks - an ArrayList of Placemarks that represent the new directions
     */
    private void updateDirections(ArrayList<Placemark> placemarks) {
    	if (placemarks_list.equals(placemarks))
    		return;
    	
    	directions_adapter.clear();
    	
    	placemarks_list.add(placemarks.get(0));
    	
    	directions_adapter.notifyDataSetChanged();
    }
    
    /**
     * Updates the icon overlay on the map
     * @param updated_route - true if the route overlay was updated, false otherwise
     * @param user_pos - the position to place the icon on the map
     */
    private void updateIconOverlay(boolean updated_route, GeoPoint pos) {   	
		// If the map overlay hasn't been updated, only remove the Navigation icon
		if (!updated_route && mapview.getOverlays().size() > 0)
			mapview.getOverlays().remove(mapview.getOverlays().size() - 1);
		
		// Draw the icon, either the Navigation arrow or just a positional point
		IconOverlay overlayLoc = null;
	    if (sensor_listener.getValues() != null)
	    	overlayLoc = new IconOverlay(pos, BitmapFactory.decodeResource(getResources(), R.drawable.nav_icon), sensor_listener.getValues()[0]);
	    else
	    	overlayLoc = new IconOverlay(pos, BitmapFactory.decodeResource(getResources(), R.drawable.loc_icon), 0.0f); 	    	
	    
	   mapview.getOverlays().add(overlayLoc);
	   
	   mapview.invalidate();
    }
	
    /**
     * Updates the route overlay on the map
     * @param points - an ArrayList of GeoPoints that represent the current route
     */
    private void updateRouteOverlay(ArrayList<GeoPoint> points, int color) {
    	// Get a GeoPoint of the user's location
    	Location user_position = location_listener.getLocation();
    	GeoPoint p = new GeoPoint((int) (user_position.getLatitude() * 1E6), (int) (user_position.getLongitude() * 1E6));

    	adventure_route = points;

		mapview.getOverlays().clear();

		mapview.getOverlays().add(new CircleOverlay(new GeoPoint(adventure_route.get(adventure_route.size() - 1).getLatitudeE6(), adventure_route.get(adventure_route.size() - 1).getLongitudeE6()), ADVENTURE_COMPLETE_RADIUS, ROUTE_COLOR));

		// Draw the route
		mapview.getOverlays().add(new RouteOverlay(p, points.get(0), color));
 	    for (int i = 1; i < points.size(); i++)
 	    	mapview.getOverlays().add(new RouteOverlay(points.get(i - 1), points.get(i), color));

 	    map_controller.animateTo(p);

 	    mapview.invalidate();

 	    updateIconOverlay(true, p);
    }

	@Override
	protected boolean isRouteDisplayed() {
		return true;
	}
	
	public class Panel extends SurfaceView implements SurfaceHolder.Callback {
        private CompassThread _thread;
 
        public Panel(Context context) {
            super(context);
            getHolder().addCallback(this);
            _thread = new CompassThread(getHolder(), this);
        }
 
        @Override
        public void onDraw(Canvas canvas) {
        	canvas.drawColor(Color.BLACK);

        	int compass_angle = 0;

        	// Get the Location of the Adventure
        	Location adventure_location = new Location(LocationManager.GPS_PROVIDER);
        	adventure_location.setLatitude(adventure_route.get(adventure_route.size() - 1).getLatitudeE6());
        	adventure_location.setLongitude(adventure_route.get(adventure_route.size() - 1).getLongitudeE6());

        	// If we get a new orientation, update the saved angle
        	if (sensor_listener.getValues() != null)
        		compass_angle = (int) (location_listener.getLocation().bearingTo(adventure_location) - sensor_listener.getValues()[0] + 0.5); 
        	
        	Bitmap icon = BitmapFactory.decodeResource(getResources(), compass_icon);
            
    		// Rotate the icon
    	    Matrix transform = new Matrix();
    	    transform.postRotate((float) compass_angle, icon.getWidth() / 2, icon.getHeight() / 2);
    	    
    	    Bitmap b = Bitmap.createBitmap(icon, 0, 0, icon.getWidth(), icon.getHeight(), transform, true);
    	    
    	    canvas.drawBitmap(b, (p_compass.getWidth() / 2) - (b.getWidth() / 2), (p_compass.getHeight() / 2) - (b.getHeight() / 2), null);
        }
 
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
 
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            _thread.setRunning(true);
            _thread.start();
        }
 
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // simply copied from sample application LunarLander:
            // we have to tell thread to shut down & wait for it to finish, or else
            // it might touch the Surface after we return and explode
            boolean retry = true;
            _thread.setRunning(false);
            while (retry) {
                try {
                    _thread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // we will try it again and again...
                }
            }
        }
    }
 
    class CompassThread extends Thread {
        private SurfaceHolder _surfaceHolder;
        private Panel _panel;
        private boolean _run = false;
 
        public CompassThread(SurfaceHolder surfaceHolder, Panel panel) {
            _surfaceHolder = surfaceHolder;
            _panel = panel;
        }
 
        public void setRunning(boolean run) {
            _run = run;
        }
 
        @Override
        public void run() {
            Canvas c;
            while (_run) {
                c = null;
                try {
                    c = _surfaceHolder.lockCanvas(null);
                    synchronized (_surfaceHolder) {
                        _panel.onDraw(c);
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        _surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }
    
    private class AdventureCompleteTask extends AsyncTask<Void, Void, Void> {
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    	}
    	
    	@Override
    	protected Void doInBackground(Void... p) {
    		while(!isCancelled()) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// We don't care about interrupts
				}
				
				// Parse the start date/time into an actual date
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(start_time);
				java.util.Date date = cal.getTime();
				
	    		if (Global.distFrom(location_listener.getGeoPoint(), new GeoPoint(adventure_route.get(adventure_route.size() - 1).getLatitudeE6(), adventure_route.get(adventure_route.size() - 1).getLongitudeE6())) < ADVENTURE_COMPLETE_RADIUS) {
	    			Intent i = new Intent(AdventureActivity.this, ViewAdventureActivity.class);
	    			i.putExtra(INTENT_ADVENTURE, adventure);
	    			i.putExtra(INTENT_ADVENTURE_PATH, adventure_path_taken);
	    			i.putExtra(INTENT_DISTANCE_TRAVELED, Double.parseDouble(tv_distance_travelled.getText().toString().split(" ")[0]));
	    			i.putExtra(INTENT_DURATION, System.currentTimeMillis() - start_time);
	    			i.putExtra(INTENT_START_TIME, date.getDate() + "-" + (date.getMonth() + 1) + "-" + date.getYear());	// This is a placeholder for the line below it
	    			// i.putExtra(INTENT_START_TIME, date.toString());		// TODO FORMAT DATE
	    			startActivity(i);
				
	    			finish();
	    		}
    		}
			
			return null;
    	}
    	
    	@Override
    	protected void onPostExecute(Void result) {
    		super.onPostExecute(result);
    	}
    }

    private class UpdateIconOverlayTask extends AsyncTask<Void, UPDATE_ICON_OVERLAY_STATUS, Void> {    	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... p) {
			while (!isCancelled()) {
				publishProgress(UPDATE_ICON_OVERLAY_STATUS.UPDATE_ICON_OVERLAY);
				
				try {
					Thread.sleep(ICON_TIME_UPDATE_INTERVAL);
				} catch (InterruptedException e) {
					// We don't care about interrupts
				}
			}
			
			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(UPDATE_ICON_OVERLAY_STATUS... values) {
			super.onProgressUpdate(values);
			
			switch (values[0]) {
			case UPDATE_ICON_OVERLAY:
		    	// Update the Icon Overlay from the current user position, don't get a new one
				if (mapview.getOverlays().size() > 0)
					updateIconOverlay(false, ((IconOverlay) mapview.getOverlays().get(mapview.getOverlays().size() - 1)).getGeoPoint());
				break;
			}
		}
    }
    
    private class UpdateLocationTask extends AsyncTask<Void, UPDATE_LOCATION_STATUS, Void> {    	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... p) {
			while (!isCancelled()) {
				publishProgress(UPDATE_LOCATION_STATUS.ENABLE_GPS_UPDATES);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// We don't care about interrupts
				}
				
				publishProgress(UPDATE_LOCATION_STATUS.DISABLE_GPS_UPDATES);
				
				try {
					Thread.sleep(Global.preferences_manager.getLocation_update_interval());
				} catch (InterruptedException e) {
					// We don't care about interrupts
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
		protected void onProgressUpdate(UPDATE_LOCATION_STATUS... values) {
			super.onProgressUpdate(values);
			
			switch (values[0]) {
			case ENABLE_GPS_UPDATES:
				location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, location_listener);
				break;
			case DISABLE_GPS_UPDATES:
				location_manager.removeUpdates(location_listener);
				break;
			}
		}
    }
	
    private class UpdateAdventureProgress extends AsyncTask<Void, UPDATE_ADVENTURE_PROGRESS_STATUS, String> {    	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... p) {
			while (!isCancelled()) {
				Location user_position = location_listener.getLocation();

				/* http://stackoverflow.com/questions/3109158/how-to-draw-a-path-on-a-map-using-kml-file */
				
				// TODO CONSTANTS
				StringBuilder urlString = new StringBuilder();
				urlString.append(Global.Google.Maps.BASE_URL);
				urlString.append("?");
				urlString.append(Global.Google.Maps.Param.F + "=d&");
				urlString.append(Global.Google.Maps.Param.HL + "=en&");
				urlString.append(Global.Google.Maps.Param.SADDR + "=");
				urlString.append(Double.toString(user_position.getLatitude()));
				urlString.append(",");
				urlString.append(Double.toString(user_position.getLongitude()));
				urlString.append("&");
				urlString.append("daddr=");
				urlString.append(Double.toString(adventure.getLat()));
				urlString.append(",");
				urlString.append(Double.toString(adventure.getLng()));
				urlString.append("&");
				urlString.append("dirflg=");
				urlString.append(adventure.getMode());
				urlString.append("&");
				urlString.append(Global.Google.Maps.Param.IE + "=UTF8&");
				urlString.append(Global.Google.Maps.Param.OUTPUT + "=kml");

				URL url;
				try {
					url = new URL(urlString.toString());								// setup the url

					SAXParserFactory factory = SAXParserFactory.newInstance();			// create the factory
					SAXParser parser = factory.newSAXParser();							// create a parser
					XMLReader xmlreader = parser.getXMLReader();						// create the reader (scanner)
					
					NavigationSaxHandler navSaxHandler = new NavigationSaxHandler();	// instantiate our handler
					xmlreader.setContentHandler(navSaxHandler);							// assign our handler
					
					InputSource is = new InputSource(url.openStream());					// get our data via the url class       
					xmlreader.parse(is);												// perform the synchronous parse
					
					directions = navSaxHandler.getParsedData();							// get the results - should be a fully populated RSSFeed instance, or null on error
					
					if (directions != null)
						publishProgress(UPDATE_ADVENTURE_PROGRESS_STATUS.UPDATE_ADVENTURE);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				try {
					Thread.sleep(Global.preferences_manager.getRoute_update_interval());
				} catch (InterruptedException e) {
					// We don't care about interrupts
				}
			}
			
			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(UPDATE_ADVENTURE_PROGRESS_STATUS... values) {
			switch (values[0]) {
			case UPDATE_ADVENTURE:
				updateDirections(directions.getPlacemarks());
				updateRouteOverlay(Global.kmlStringToArrayList(directions.getRoutePlacemark().getCoordinates()), ROUTE_COLOR);
				
				// Update adventure path
				if (adventure_path_taken.size() == 0)
					adventure_path_taken.add(new ParcelableGeoPoint(location_listener.getGeoPoint()));
				else if (!adventure_path_taken.get(adventure_path_taken.size() - 1).equals(location_listener.getGeoPoint()) && location_listener.getLocation().getAccuracy() <= MAX_GPS_ERROR)
					adventure_path_taken.add(new ParcelableGeoPoint(location_listener.getGeoPoint()));

				// A conversion factor based on the user's preferences for switching between imperial and metric
				MeasurementSystem system = Global.preferences_manager.getMeasurement_system();
				
				// Update the distance left
				// We'll keep these in meters until we're ready to display it
				double distance_left = 0.0;
				double distance_traveled = 0.0;
				
				// Round the distance to two decimals
				DecimalFormat decimal_format = new DecimalFormat("###.####");
				
				if (fl_compass_frame.getVisibility() == View.VISIBLE) {
					// Get the compass distance
					distance_left = Global.distFrom(location_listener.getGeoPoint(), new GeoPoint((int) (directions.getPlacemarks().get(directions.getPlacemarks().size() - 1).getLatitude() * 1E6), (int) (directions.getPlacemarks().get(directions.getPlacemarks().size() - 1).getLongitude() * 1E6)));
				} else {
					// Get route distance
					for (int i = 0; i < directions.getPlacemarks().size(); i++) {
						String[] parsed_distance = directions.getPlacemarks().get(i).getDescription().split(" ");
						
						// If we have valid distance data
						if (parsed_distance.length == 3)
							distance_left += LengthConverter.convert(Double.parseDouble(parsed_distance[1]), parsed_distance[2], LengthConverter.METERS);
					}
				}
				
				// Get traveled distance
				for (int i = 1; i < adventure_path_taken.size(); i++)
					distance_traveled += Global.distFrom(adventure_path_taken.get(i - 1).getGeoPoint(), adventure_path_taken.get(i).getGeoPoint());

				// Set Time Left
				tv_time_left.setText(directions.getRoutePlacemark().getTimeLeft());
				
				// Set Distance Left and Distance Traveled
				String unit = "";
				if (system.equals(MeasurementSystem.IMPERIAL)) {
					unit = LengthConverter.MILES;
					
				} else {
					unit = LengthConverter.KILOMETERS;
				}
				
				distance_left = LengthConverter.convert(distance_left, LengthConverter.METERS, unit);
				distance_traveled = LengthConverter.convert(distance_traveled, LengthConverter.METERS, unit);
				
				tv_distance_left.setText(String.valueOf(decimal_format.format(distance_left)) + " " + unit);
				tv_distance_travelled.setText(decimal_format.format(distance_traveled) + " " + unit);
				
				// Display the adventure and start
				if (start_time == -1) {
					vf_flipper.showNext();
					
					// Start the adventure timer
					start_time = System.currentTimeMillis();
					adventure_timer.postDelayed(update_timer, 1000);
				}
				
				break;
			}
			
			super.onProgressUpdate(values);
		}
    }
}
