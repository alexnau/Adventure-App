package com.obj;

public class Placemark {
	/* http://stackoverflow.com/questions/3109158/how-to-draw-a-path-on-a-map-using-kml-file */
	
	String title = "";
	String description = "";
	String address = "";
	String coordinates = "";
	String time_left = "";
	
	double latitude = 0.0;
	double longitude = 0.0;
	
	@Override
	public boolean equals(Object o) {
		try {
			Placemark other = (Placemark) o;
			if (this.title.equals(other.title) && this.description.equals(other.description) && this.address.equals(other.address) && this.coordinates.equals(other.coordinates) && this.latitude == other.latitude && this.longitude == other.longitude)
				return true;
			else
				return false;
		} catch (ClassCastException e) {
			return false;
		}
	}

	public String getTitle() {
	    return title;
	}
	
	public void setTitle(String title) {
	    this.title = title;
	}
	
	public String getDescription() {
	    return description;
	}
	
	public void setDescription(String description) {
	    this.description = description;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public String getCoordinates() {
	    return coordinates;
	}
	
	public void setCoordinates(String coordinates) {
	    this.coordinates = coordinates;
	}
	
	public String getAddress() {
	    return address;
	}
	
	public void setAddress(String address) {
	    this.address = address;
	}
	
	public String getTimeLeft() {
	    return time_left;
	}
	
	public void setTimeLeft(String time_left) {
	    this.time_left = time_left;
	}
}