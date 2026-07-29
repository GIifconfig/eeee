package com.example.aabbg.handlers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.TextView;

public class LocationHandler {
    private Context context;
    private TextView txtLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public LocationHandler(Context ctx, TextView txtLocation) {
        this.context = ctx;
        this.txtLocation = txtLocation;
        init();
    }

    private void init() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // LocationListener 需在 Activity 中检查并申请运行时权限
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    txtLocation.setText("Lat:" + location.getLatitude() + " Lon:" + location.getLongitude());
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, android.os.Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
    }

    public void startLocationUpdates() {
        // Activity 必须在调用前检查权限
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        } catch (SecurityException e) {
            // 权限未授权
        }
    }

    public void stopLocationUpdates() {
        try {
            locationManager.removeUpdates(locationListener);
        } catch (SecurityException e) {}
    }
}
