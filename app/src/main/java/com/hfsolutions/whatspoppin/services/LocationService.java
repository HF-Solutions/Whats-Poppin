package com.hfsolutions.whatspoppin.services;

import android.annotation.SuppressLint;
import android.app.Notification;
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
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.hfsolutions.whatspoppin.util.PermissionHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.google.android.gms.location.places.Place.TYPE_BAR;
import static com.google.android.gms.location.places.Place.TYPE_NIGHT_CLUB;
import static com.hfsolutions.whatspoppin.util.Constants.PLACE_POPPIN_NOTI_ID;
import static com.hfsolutions.whatspoppin.util.Constants.PLACE_RESPONSE_RECEIVED;
import static com.hfsolutions.whatspoppin.util.Constants.PLACE_TIMESTAMP;
import static com.hfsolutions.whatspoppin.util.Constants.SHARED_PREFERENCES_ID;
import static com.hfsolutions.whatspoppin.util.NotificationHelper.buildNotification;

/**
 * <p>Created by Alcha on May 25, 2018 @ 14:46.</p>
 */
public class LocationService extends Service {
    private static final String LOG_TAG = "LocationService";
    private static final int MS_PER_MIN = 60000;
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private boolean mFirstTimestamp = true;

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
        initializePreferences();

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

    private void initializePreferences() {
        SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFERENCES_ID, MODE_PRIVATE).edit();

        editor.remove(PLACE_TIMESTAMP);
        editor.remove(PLACE_RESPONSE_RECEIVED);

        editor.apply();
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

                        if (currentPlace != null && checkNotificationPreReqs()) {
                            Notification notification = buildNotification(currentPlace, getApplicationContext());
                            NotificationManagerCompat.from(LocationService.this).notify(PLACE_POPPIN_NOTI_ID, notification);
                            setRespondedFlag();
                        }

                        likelyPlaces.release();
                    }
                }
            });
        }
    }

    private void setRespondedFlag() {
        SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFERENCES_ID, MODE_PRIVATE).edit();
        editor.putBoolean(PLACE_RESPONSE_RECEIVED, true);
        editor.apply();
    }

    public int getCurrTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HHmm", Locale.US);
        String formattedDate = formatter.format(new Date());

        return Integer.valueOf(formattedDate);
    }

    private boolean checkNotificationPreReqs() {
        SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_ID, MODE_PRIVATE);

        // User has already responded, don't notify them again
        if (userHasResponded(preferences)) return false;

        int timestamp = preferences.getInt(PLACE_TIMESTAMP, getCurrTime());
        int diff = getCurrTime() - timestamp;

        if (mFirstTimestamp) {
            mFirstTimestamp = false;

            updateTimestampPref(preferences, timestamp);
        } else if (diff >= 5) {
            updateTimestampPref(preferences, getCurrTime());
            return true;
        }

        return false;
    }

    private void updateTimestampPref(SharedPreferences preferences, int timestamp) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(PLACE_TIMESTAMP, timestamp);
        editor.apply();
    }

    private boolean userHasResponded(SharedPreferences preferences) {
        return preferences.getBoolean(PLACE_RESPONSE_RECEIVED, false);
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

        return MS_PER_MIN;
    }

    private void initializeLocationManager() {
        Log.e(LOG_TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
