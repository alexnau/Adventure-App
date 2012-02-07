package com.obj;

import android.os.Parcel;
import android.os.Parcelable;

public class AdventureParams implements Parcelable {
	private String types = "";
	private String keywords = "";
	private String mode = "";
	private String name = "";
	
	private double lat = 0;
	private double lng = 0;
	
	private int radius = 0;
	
	public AdventureParams() {}
	
	public String getTypes() {
		return types;
	}

	public String getKeywords() {
		return keywords;
	}

	public String getMode() {
		return mode;
	}
	
	public String getName() {
		return name;
	}

	public double getLat() {
		return lat;
	}

	public double getLng() {
		return lng;
	}

	public int getRadius() {
		return radius;
	}

	public static Parcelable.Creator<AdventureParams> getCreator() {
		return CREATOR;
	}
	
	public void setTypes(String types) {
		this.types = types;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	// Parcelling
	public AdventureParams(Parcel in) {
		types = in.readString();
		keywords = in.readString();
		mode = in.readString();
		name = in.readString();
		
		lat = in.readDouble();
		lng = in.readDouble();
		
		radius = in.readInt();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(types);
		dest.writeString(keywords);
		dest.writeString(mode);
		dest.writeString(name);
		
		dest.writeDouble(lat);
		dest.writeDouble(lng);
		
		dest.writeInt(radius);
	}
	
	public static final Parcelable.Creator<AdventureParams> CREATOR = new Parcelable.Creator<AdventureParams>() {
		public AdventureParams createFromParcel(Parcel in) {
			return new AdventureParams(in);
		}

		@Override
		public AdventureParams[] newArray(int size) {
			return new AdventureParams[size];
		}
	};
}
