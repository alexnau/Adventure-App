package com.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class CircleOverlay extends Overlay {

	private GeoPoint geopoint;
	private int rad;
	private int color;

	public CircleOverlay(GeoPoint point, double d, int c) {
		geopoint = point;
		rad = (int) d;
		color = c;
	}


	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// Transform geo-position to Point on canvas
		Projection projection = mapView.getProjection();
		Point point = new Point();
		
		// Store the transformed geopoint into a point with pixel values
		projection.toPixels(geopoint, point);

		// The circle to mark the spot
		Paint circlePaint = new Paint();
		circlePaint.setAntiAlias(true);
		
		// Fill region
		circlePaint.setColor(color);
		circlePaint.setAlpha(90);
		circlePaint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(point.x, point.y, rad, circlePaint);

		// Border region
		circlePaint.setColor(Color.WHITE);
		circlePaint.setAlpha(255);
		circlePaint.setStyle(Paint.Style.STROKE);
		circlePaint.setStrokeWidth(3);
		canvas.drawCircle(point.x, point.y, rad, circlePaint);
	}
}
