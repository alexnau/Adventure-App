package com.obj;

public class StoredAdventure {
	String destination = "";
	String distance_traveled = "";
	String duration = "";
	String route = "";
	String start_time = "";
	
	public StoredAdventure(String de, String di, String du, String r, String s) {
		destination = de;
		distance_traveled = di;
		duration = du;
		route = r;
		start_time = s;
	}

	public String getDestination() {
		return destination;
	}

	public String getDistance_traveled() {
		return distance_traveled;
	}

	public String getDuration() {
		return duration;
	}

	public String getRoute() {
		return route;
	}

	public String getStart_time() {
		return start_time;
	}
}
