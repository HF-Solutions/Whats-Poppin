package com.paranoiddevs.whatspoppin.activities;

import android.content.DialogInterface;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * <p>Created by Alcha on Mar 27, 2018 @ 19:40.</p>
 */

public class BaseActivity extends AppCompatActivity {
    private static final String LOG_TAG = BaseActivity.class.getSimpleName();

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Checks to see if the user has granted the app access to the ACCESS_COARSE_LOCATION permission
     * and returns true or false.
     *
     * @return True/False - The user has granted access to COARSE_LOCATION info
     */
    protected boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                ACCESS_COARSE_LOCATION);
        return permissionState == PERMISSION_GRANTED;
    }

    protected void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                ACCESS_COARSE_LOCATION);

        if (shouldProvideRationale) {
            Log.i(LOG_TAG, "requestPermissions: Displaying permission rationale to provide context.");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your location is required in order for the app to properly function.")
                    .setTitle("What's Poppin'?");

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startLocationPermissionRequest();
                }
            });

            builder.create().show();
        } else {
            Log.i(LOG_TAG, "requestPermissions: Requesting permission.");

            startLocationPermissionRequest();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainText The text to show.
     * @param action   The text of the action item.
     * @param listener The listener associated with the Snackbar action.
     */
    protected void showSnackbar(final String mainText, final String action, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content), mainText,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(action, listener).show();
    }

    public void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Converts the provided {@link Location} object to a {@link LatLng} for use with the
     * {@link com.google.android.gms.maps.GoogleMap} and it's markers.
     *
     * @param location A {@link Location} object of the users current location
     *
     * @return A {@link LatLng} object of the users current location.
     */
    protected LatLng getCurrLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    /**
     * Retrieves the text content from the provided {@link EditText} view and trims any excess
     * whitespace.
     *
     * @param editText The {@link EditText} you wish to retrieve the content from
     *
     * @return A {@link String} containing the content of the provided {@link EditText}
     */
    protected String getTextContent(EditText editText) {
        return editText.getText().toString().trim();
    }
}
