package com.util;

public class LengthConverter {
	public static final double FEET_PER_METER = 3.2808399;
	public static final double FEET_PER_MILE = 5280.0;
	
	// Metric
	public static final String KILOMETERS = "km";
	public static final String METERS = "m";
	
	// Imperial
	public static final String FEET = "ft";
	public static final String MILES = "mi";
	
	public static double convert(double d, String in, String out) {
		if (in.equals(FEET)) {
			if (out.equals(METERS))
				return d / FEET_PER_METER;
			if (out.equals(KILOMETERS))
				return (d / FEET_PER_METER) / 1000;
			if (out.equals(MILES))
				return d / FEET_PER_MILE;
		}
		
		if (in.equals(KILOMETERS)) {
			if (out.equals(METERS))
				return d * 1000;
			if (out.equals(FEET))
				return (d * 1000) * FEET_PER_METER;
			if (out.equals(MILES))
				return ((d * 1000) * FEET_PER_METER) / FEET_PER_MILE;
		}
		
		if (in.equals(METERS)) {
			if (out.equals(KILOMETERS))
				return d / 1000;
			if (out.equals(FEET))
				return d * FEET_PER_METER;
			if (out.equals(MILES))
				return (d * FEET_PER_METER) / FEET_PER_MILE;
		}
		
		if (in.equals(MILES)) {
			if (out.equals(FEET))
				return d * FEET_PER_MILE;
			if (out.equals(METERS))
				return (d * FEET_PER_MILE) / FEET_PER_METER;
			if (out.equals(KILOMETERS))
				return ((d * FEET_PER_METER) / FEET_PER_METER) / 1000;
		}
		
		return d;
	}
}
