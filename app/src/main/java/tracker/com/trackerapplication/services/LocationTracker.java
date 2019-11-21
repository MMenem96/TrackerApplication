package tracker.com.trackerapplication.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import tracker.com.trackerapplication.activities.MainActivity;

public class LocationTracker extends Service implements LocationListener {
    //declaring Context variable
    private final Context con;
    //flag for gps
    boolean isGPSOn = false;
    //flag for network location
    boolean isNetWorkEnabled = false;
    //flag to getlocation
    boolean isLocationEnabled = false;
    //minimum distance to request for location update
    private static final long MIN_DISTANCE_TO_REQUEST_LOCATION = 0; // in meters
    // minimum time to request location updates
    private long MIN_TIME_FOR_UPDATES;
    //location
    Location location;
    //latitude and longitude
    double latitude, longitude;
    //Declaring a LocationManager
    LocationManager locationManager;

    public LocationTracker(Context context, long MIN_TIME_FOR_UPDATES) {
        this.con = context;
        this.MIN_TIME_FOR_UPDATES = MIN_TIME_FOR_UPDATES;

        checkIfLocationAvailable();
    }

    @SuppressLint("MissingPermission")
    public Location checkIfLocationAvailable() {
        try {
            locationManager = (LocationManager) con.getSystemService(LOCATION_SERVICE);
            //check for gps availability
            isGPSOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            //check for network availablity
            isNetWorkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSOn && !isNetWorkEnabled) {
                isLocationEnabled = false;
                // no location provider is available show toast to user
                Toast.makeText(con, "No Location Provider is Available", Toast.LENGTH_SHORT).show();
            } else {
                isLocationEnabled = true;
                // if network location is available request location update
                if (isNetWorkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_FOR_UPDATES, MIN_DISTANCE_TO_REQUEST_LOCATION, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                if (isGPSOn) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_FOR_UPDATES, MIN_DISTANCE_TO_REQUEST_LOCATION, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return location;
    }

    // call this to stop using location
    public void stopUsingLocation() {
        if (locationManager != null) {
            locationManager.removeUpdates(LocationTracker.this);
            this.onDestroy();
        }
    }

    // call this to getLatitude
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    //call this to getLongitude
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public boolean isLocationEnabled() {
        return this.isLocationEnabled;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Log.e("location", "locationChanged");
        //Submit location to the server
        ((MainActivity) con).submitLocationToServer(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}