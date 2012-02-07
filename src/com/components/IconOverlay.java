package com.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class IconOverlay extends Overlay {
	private Bitmap icon = null;
	private GeoPoint geopoint = null;
	
	private float angle = 0.0f;

	public IconOverlay(GeoPoint p, Bitmap i, float a) {
		geopoint = p;
		
		icon = i;
		angle = a;
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
		
		// Rotate the icon
	    Matrix transform = new Matrix();
	    transform.postRotate(angle, icon.getWidth() / 2, icon.getHeight() / 2);
	    
	    Bitmap b = Bitmap.createBitmap(icon, 0, 0, icon.getWidth(), icon.getHeight(), transform, true);
	    
	    canvas.drawBitmap(b, point.x - (b.getWidth() / 2), point.y - (b.getHeight() / 2), null);
	}
	
	public GeoPoint getGeoPoint() {
		return geopoint;
	}
}