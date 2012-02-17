package com.obj;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.maps.GeoPoint;

public class ParcelableGeoPoint implements Parcelable {
	public static final String COORDINATE_SEPARATOR = ";";
	public static final String LAT_LNG_SEPARATOR = ",";
	
    private GeoPoint geoPoint;

    public ParcelableGeoPoint(GeoPoint point) {
    	
        geoPoint = point;
    }

    public GeoPoint getGeoPoint() {
         return geoPoint;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(geoPoint.getLatitudeE6());
        out.writeInt(geoPoint.getLongitudeE6());
    }

    public static final Parcelable.Creator<ParcelableGeoPoint> CREATOR
            = new Parcelable.Creator<ParcelableGeoPoint>() {
        public ParcelableGeoPoint createFromParcel(Parcel in) {
            return new ParcelableGeoPoint(in);
        }

        public ParcelableGeoPoint[] newArray(int size) {
            return new ParcelableGeoPoint[size];
        }
    };

    private ParcelableGeoPoint(Parcel in) {
        int lat = in.readInt();
        int lon = in.readInt();
        geoPoint = new GeoPoint(lat, lon);
    }
    
    public static String arrayListToString(ArrayList<ParcelableGeoPoint> list) {
    	String coord_string = "";
    	
        for (int i = 0; i < list.size(); i++) {
        	//adventure_path.add(parcelable_adventure_list.get(i).getGeoPoint());
        	
        	coord_string += String.valueOf(list.get(list.size() - 1).getGeoPoint().getLatitudeE6());
        	coord_string += LAT_LNG_SEPARATOR;
        	coord_string += String.valueOf(list.get(list.size() - 1).getGeoPoint().getLongitudeE6());
        	coord_string += COORDINATE_SEPARATOR;
        }
        
        return coord_string.substring(0, coord_string.length() - 1);
    }
    
    public static ArrayList<ParcelableGeoPoint> stringToArrayList(String coord_string) {
    	ArrayList<ParcelableGeoPoint> list = new ArrayList<ParcelableGeoPoint>();
    	
    	String[] coord_list = coord_string.split(COORDINATE_SEPARATOR);
        for (int i = 0; i < coord_list.length; i++) {
        	String[] coords = coord_list[i].split(LAT_LNG_SEPARATOR);
        	
        	ParcelableGeoPoint gp = new ParcelableGeoPoint(new GeoPoint(Integer.valueOf(coords[0]), Integer.valueOf(coords[1])));
        	list.add(gp);
        }
        
        return list;
    }
}