package com.paranoiddevs.whatspoppin.util;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static com.google.android.gms.location.places.Place.TYPE_BAR;
import static com.google.android.gms.location.places.Place.TYPE_NIGHT_CLUB;

/**
 * <p>Created by Alcha on May 25, 2018 @ 14:46.</p>
 */
public class LocationService extends Service {
    private static final String LOG_TAG = "LocationService";
    private static final int MS_PER_SEC = 60000;
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    /** {@link FusedLocationProviderClient} used to retrieve the users location */
    private FusedLocationProviderClient mFusedLocationProviderClient;

    /**
     * {@link com.google.android.gms.location.places.PlaceDetectionClient} used to retrieve the most
     * likely place the user is currently at.
     */
    private PlaceDetectionClient mPlaceDetectionClient;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        LocationListener(String provider) {
            Log.e(LOG_TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(LOG_TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            performPlaceCheck();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(LOG_TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(LOG_TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(LOG_TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(LOG_TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(LOG_TAG, "onCreate");
        initializeLocationManager();
        initializeLocationClient();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, getLocationInterval(), LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(LOG_TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(LOG_TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(LOG_TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(LOG_TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @SuppressLint("MissingPermission")
    private void initializeLocationClient() {
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
    }

    @SuppressLint("MissingPermission")  // Permission check is made before this is called
    private void performPlaceCheck() {
        if (PermissionHelper.checkPermissions(this)) {
            Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                    if (task.isSuccessful()) {
                        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                        Place currentPlace = getMostLikelyPlace(likelyPlaces);

                        if (currentPlace != null) {
                            System.out.println("WE'S AT A BAR, BITCHES!");
                            // TODO: Ask the user if this place is poppin'.
                        }

                        likelyPlaces.release();
                    }
                }
            });
        }
    }

    private Place getMostLikelyPlace(PlaceLikelihoodBufferResponse likelyPlaces) {
        float maxLikelihood = 0f;
        Place currentPlace = null;

        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
            if (placeLikelihood.getPlace().getPlaceTypes().contains(TYPE_NIGHT_CLUB) ||
                    placeLikelihood.getPlace().getPlaceTypes().contains(TYPE_BAR)) {

                if (maxLikelihood < placeLikelihood.getLikelihood()) {
                    maxLikelihood = placeLikelihood.getLikelihood();
                    currentPlace = placeLikelihood.getPlace();
                }
            }
        }

        return currentPlace;
    }

    @Override
    public void onDestroy() {
        Log.e(LOG_TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (LocationListener mLocationListener : mLocationListeners) {
                try {
                    mLocationManager.removeUpdates(mLocationListener);
                } catch (Exception ex) {
                    Log.i(LOG_TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
    }

    private int getLocationInterval() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String syncInterval = sharedPreferences.getString("sync_frequency", "15");

        return Integer.parseInt(syncInterval) * MS_PER_SEC;
    }

    private void initializeLocationManager() {
        Log.e(LOG_TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
