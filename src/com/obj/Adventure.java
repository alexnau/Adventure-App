package com.obj;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;


public class Adventure implements Parcelable {
	private double rating = 0.0;			// Rating based on user reviews	
	
	private String icon = "";					// Icon URL
	private String id = "";						// ID used to identify place
	private String name = "";					// Place Name
	private String reference = "";				// ID used to reference place and possibly retrieve additional data
	private String vicinity = "";				// General vicinity of place
	
	private String[] types = null;				// List of types associated with this place
	
	private char mode = 'd';
	
	private double lat = 0.0;		// Latitude
	private double lng = 0.0;		// Longitude
	
	private String viewport = "";	// TODO THIS PARAMETER IS OPTIONAL FIGURE OUT ITS TYPE
	
	public Adventure(JSONObject data) {
		try {
			rating = data.getDouble(Global.Google.Places.Result.Results.RATING);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			icon = data.getString(Global.Google.Places.Result.Results.ICON);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			id = data.getString(Global.Google.Places.Result.Results.ID);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			name = data.getString(Global.Google.Places.Result.Results.NAME);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			reference = data.getString(Global.Google.Places.Result.Results.REFERENCE);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			vicinity = data.getString(Global.Google.Places.Result.Results.VICINITY);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			JSONArray json_types = data.getJSONArray(Global.Google.Places.Result.Results.TYPES);
			types = new String[json_types.length()];
			for (int i = 0; i < json_types.length(); i++)
				types[i] = json_types.getString(i);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			JSONObject geo = data.getJSONObject(Global.Google.Places.Result.Results.GEOMETRY);
			JSONObject location = geo.getJSONObject(Global.Google.Places.Result.Results.Geometry.Location.LOCATION);
			
			try {
				lat = location.getDouble(Global.Google.Places.Result.Results.Geometry.Location.LAT);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				lng = location.getDouble(Global.Google.Places.Result.Results.Geometry.Location.LNG);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				viewport = geo.getString(Global.Google.Places.Result.Results.Geometry.VIEWPORT);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public double getRating() {
		return rating;
	}

	public String getIcon() {
		return icon;
	}

	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public String getReference() {
		return reference;
	}

	public String getVicinity() {
		return vicinity;
	}

	public String[] getTypes() {
		return types;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}
	
	public char getMode() {
		return mode;
	}
	
	public void setMode(char mode) {
		this.mode = mode;
	}

	public String getViewport() {
		return viewport;
	}

	public void setViewport(String viewport) {
		this.viewport = viewport;
	}

	public static Parcelable.Creator<Adventure> getCreator() {
		return CREATOR;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public void setVicinity(String vicinity) {
		this.vicinity = vicinity;
	}

	public void setTypes(String[] types) {
		this.types = types;
	}
	
	// Parcelling
	public Adventure(Parcel in) {
		rating = in.readDouble();
		
		icon = in.readString();
		id = in.readString();
		name = in.readString();
		reference = in.readString();
		vicinity = in.readString();
		
		mode = (char) in.readInt();
		
		int types_size = in.readInt();
		if (types_size != -1) {
			types = new String[types_size];
			in.readStringArray(types);
		}
		
		lat = in.readDouble();
		lng = in.readDouble();
		viewport = in.readString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(rating);
		
		dest.writeString(icon);
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(reference);
		dest.writeString(vicinity);
		
		dest.writeInt((int) mode);
		
		if (types == null) {
			dest.writeInt(-1);
		} else {
			dest.writeInt(types.length);
			dest.writeStringArray(types);
		}
		
		dest.writeDouble(lat);
		dest.writeDouble(lng);
		dest.writeString(viewport);
	}
	
	public static final Parcelable.Creator<Adventure> CREATOR = new Parcelable.Creator<Adventure>() {
		public Adventure createFromParcel(Parcel in) {
			return new Adventure(in);
		}

		@Override
		public Adventure[] newArray(int size) {
			return new Adventure[size];
		}
	};
}
