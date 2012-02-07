package com.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
import android.provider.BaseColumns;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

/**
 * 
 * GLOBAL TODO LIST
 * Fix radius calculations when showing the map in ChooseRadiusActivity
 * Design/implement start page
 *
 */

public class Global extends Application {
	public static final int EARTH_RADIUS_METERS = 6371000;
	
	public static final String SHARED_PREFS = "adventure_app_prefs";
	
	public static final String HTML_ENCODED_SPACE = "&#160;";

	public static PreferencesManager preferences_manager;

	public static ArrayList<GeoPoint> decodePolyline(String polyline) {
		ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();
		
		int len = polyline.length();
		int index = 0;
		
		int lat = 0;
		int lng = 0;
		
		while (index < len) {
			int c;
			int shift = 0;
			int result = 0;
			
			do {
				c = polyline.charAt(index++) - 63;
				result |= (c & 0x1f) << shift;
				shift += 5;
			} while (c >= 0x20);
			
			int dlat = (((result & 1) != 0) ? ~(result >> 1) : (result >> 1));
			lat += dlat;
			shift = 0;
			result = 0;
			
			do {
				c = polyline.charAt(index++) - 63;
				result |= (c & 0x1f) << shift;
				shift += 5;
			} while (c >= 0x20);
			
			int dlng = (((result & 1) != 0) ? ~(result >> 1) : (result >> 1));
			lng += dlng;
			points.add(new GeoPoint(lat * 10, lng * 10));
		}
		
		return points;
	}
	
	/**
	 * Haversine formula for computing the distance between two lat/long points
	 * @param p1 - the first point
	 * @param p2 - the second point
	 * @return - the number of meters between the two points adjusted for the Earth's curve
	 */
	public static double distFrom(GeoPoint p1, GeoPoint p2) {
		double lat1 = p1.getLatitudeE6() * 1E-6;
		double lat2 = p2.getLatitudeE6() * 1E-6;
		
		double lng1 = p1.getLongitudeE6() * 1E-6;
		double lng2 = p2.getLongitudeE6() * 1E-6;
		
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +  Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng/2) * Math.sin(dLng/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = EARTH_RADIUS_METERS * c;

		return dist;
	}
	
	public static ArrayList<GeoPoint> kmlStringToArrayList(String coords) {
		ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();
		
		String[] pairs = coords.trim().split(" ");
		
		for (int i = 0; i < pairs.length; i++) {
			String[] latlng = pairs[i].split(",");
			points.add(new GeoPoint((int) (Double.parseDouble(latlng[1]) * 1E6), (int) (Double.parseDouble(latlng[0]) * 1E6)));
		}

		return points;		
	}

	/**
	 * Outputs an error via Toast on the UI thread
	 * @param caller - the calling Activity
	 * @param error - the error to output
	 */
	public static void outputError(final Activity caller, final String error) {
		caller.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(caller, error, Toast.LENGTH_SHORT).show();	
			}
		});
	}
	
	/**
	 * Reads all the data from a BufferedReader with each line delimited by a '\n'
	 * @param br - the BufferedReader to read from
	 * @return - a String containing all the data from the BufferedReader
	 * @throws IOException - if there's an error reading the BufferedReader
	 */
	public static String readAll(BufferedReader br) throws IOException {
		String s = "";
		String temp;
		
		do {
			temp = br.readLine();
			if (temp != null)
				s += temp + "\n";
		} while (temp != null);
		
		return s;
	}
	
	public static final class Error implements BaseColumns {
		public static final String INTENT_ADVENTURE_PARAMS_ERROR = "Error passing Adventure parameters across activities";
		public static final String INTENT_ADVENTURE_ERROR = "Error passing Adventure object across activities";
		public static final String INTENT_ADVENTURE_PATH = "Error passing Adventure Path across activities";
		public static final String JSON_PARSE_ERROR = "JSON parsing error: ";
		public static final String NETWORK_ERROR = "Network error: ";
		public static final String NO_ADVENTURES_FOUND = "No Adventures found";
	}
	
	public static final class Google implements BaseColumns {
		public static final class Directions implements BaseColumns {
			public static final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/json";
			
			public static final class Avoid implements BaseColumns {
				public static final String HIGHWAYS = "highways";
				public static final String TOLLS = "tolls";
			}
			
			public static final class Param implements BaseColumns {
				// Required
				public static final String DESTINATION = "destination";
				public static final String ORIGIN = "origin";
				public static final String SENSOR = "sensor";
				
				// Optional
				public static final String ALTERNATIVES = "alternatives";
				public static final String AVOID = "avoid";
				public static final String MODE = "mode";
				public static final String REGION = "region";
				public static final String UNITS = "units";
				public static final String WAYPOINTS = "waypoints";
			}
			
			public static final class Result implements BaseColumns {
				public static final String STATUS = "status";
				
				public static final class Routes implements BaseColumns {
					public static final String ROUTES = "routes";
				
					public static final String COPYRIGHTS = "copyrights";
					public static final String SUMMARY = "summary";
					public static final String WARNINGS = "warnings";
					public static final String WAYPOINT_ORDER = "waypoint_order";
					
					public static final class Bounds implements BaseColumns {
						public static final String BOUNDS = "bounds";
						
						public static final class Direction implements BaseColumns {
							public static final String NORTHEAST = "northeast";
							public static final String SOUTHWEST = "southwest";
							
							public static final String LAT = "lat";
							public static final String LNG = "lng";
						}
					}
					
					public static final class Distance implements BaseColumns {
						public static final String DISTANCE = "distance";
						
						public static final String TEXT = "text";
						public static final String VALUE = "value";
					}
					
					public static final class Duration implements BaseColumns {
						public static final String DURATION = "duration";
						
						public static final String TEXT = "text";
						public static final String VALUE = "value";
					}
					
					public static final class Legs implements BaseColumns {
						public static final String LEGS = "legs";
						
						public static final String END_ADDRESS = "end_address";
						public static final String START_ADDRESS = "start_address";
						
						public static final class Steps implements BaseColumns {
							public static final String STEPS = "steps";
							
							public static final String HTML_INSTRUCTIONS = "html_instructions";
							public static final String TRAVEL_MODE = "travel_mode";
						}
					}
					
					public static final class Location implements BaseColumns {
						public static final String END_LOCATION = "end_location";
						public static final String START_LOCATION = "start_location";
						
						public static final String LAT = "lat";
						public static final String LNG = "lng";
					}
				
					public static final class Polyline implements BaseColumns {
						public static final String OVERVIEW_POLYLINE = "overview_polyline";
						public static final String POLYLINE = "polyline";
						
						public static final String POINTS = "points";
					}
				}
			}
		
			public static final class TravelMode implements BaseColumns {
				public static final String BICYCLING = "BICYCLING";
				public static final String DRIVING = "DRIVING";
				public static final String WALKING = "WALKING";
			}
			
			public static final class Units implements BaseColumns {
				public static final String IMPERIAL = "imperial";
				public static final String METRIC = "metric";
			}
		}
		
		public static final class Maps implements BaseColumns {
			public static final String BASE_URL = "https://maps.google.com/maps";
			
			public static final class Param implements BaseColumns {
				// Reference:  http://querystring.org/google-maps-query-string-parameters/
				public static final String DADDR = "daddr";		// Destination Address
				public static final String F = "f";				// Query Style, should be 'd' for our purposes
				public static final String HL = "hl";			// Host Language, should be 'en' for our purposes
				public static final String IE = "ie";			// Input Encoding, should be UTF-8 for our purposes
				public static final String OUTPUT = "output";	// Output Format, should be 'kml' for our purposes
				public static final String SADDR = "saddr";		// Start Address
			}
			
			public static final class Result implements BaseColumns {
				public static final String KML = "kml";
				
				public static final class Document implements BaseColumns {
					public static final String DOCUMENT = "Document";
					
					public static final String NAME = "name";
					
					public static final class Placemark implements BaseColumns {
						public static final String PLACEMARK = "Placemark";
						
						public static final String ADDRESS = "address";
						public static final String DESCRIPTION = "description";
						public static final String NAME = "name";
						
						public static final class GeometryCollection implements BaseColumns {
							public static final String GEOMETRY_COLLECTION = "GeometryCollection";
							
							public static final class LineString implements BaseColumns {
								public static final String LINE_STRING = "LineString";
								
								public static final String COORDINATES = "coordinates";
							}
						}
						
						public static final class Point implements BaseColumns {
							public static final String POINT = "Point";
							
							public static final String COORDINATES = "coordinates";
						}
					}
				}
			}
		}

		public static final class Places implements BaseColumns {
			public static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/search/json";
			
			public static final class Param implements BaseColumns {
				// Required
				public static final String KEY = "key";
				public static final String LOCATION = "location";
				public static final String RADIUS = "radius";
				public static final String SENSOR = "sensor";
				
				// Optional
				public static final String KEYWORD = "keyword";
				public static final String LANGUAGE = "language";
				public static final String NAME = "name";
				public static final String TYPES = "types";
			}
			
			public static final class Result implements BaseColumns {
				public static final String HTML_ATTRIBUTIONS = "html_attributions";
				
				public static final String STATUS = "status";
				
				public static final class Results implements BaseColumns {
					public static final String RESULTS = "results";
					
					public static final String GEOMETRY = "geometry";
					public static final String LOCATION = "location";
					public static final String LAT = "lat";
					public static final String LNG = "lng";
					public static final String ICON = "icon";
					public static final String ID = "id";
					public static final String NAME = "name";
					public static final String RATING = "rating";
					public static final String REFERENCE = "reference";
					public static final String TYPES = "types";
					public static final String VICINITY = "vicinity";
					
					public static final class Geometry implements BaseColumns {
						public static final String GEOMETRY = "geometry";
						
						public static final String VIEWPORT = "viewport";
						
						public static final class Location implements BaseColumns {
							public static final String LOCATION = "location";
							
							public static final String LAT = "lat";
							public static final String LNG = "lng";
						}
					}	
				}
			}
		}
	}
}
