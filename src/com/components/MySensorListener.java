package com.components;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MySensorListener implements SensorEventListener {
	private float[] values = new float[3];	// Values taken from sensor
	
	private int accuracy = SensorManager.SENSOR_STATUS_UNRELIABLE;
	private int sensor = Sensor.TYPE_ALL;
	
	public MySensorListener(int s) {
		sensor = s;
	}
	
	@Override
	public void onAccuracyChanged(Sensor s, int accuracy) {
		if (sensor == s.getType())
			this.accuracy = accuracy;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == sensor)
			for (int i = 0; i < 3; i++)
				values[i] = event.values[i];	
	}

	public int getAccuracy() {
		return accuracy;
	}
	
	public float[] getValues() {
		return values;
	}
}