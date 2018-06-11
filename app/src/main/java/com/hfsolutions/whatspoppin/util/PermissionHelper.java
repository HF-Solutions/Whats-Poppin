package com.hfsolutions.whatspoppin.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * <p>Created by Alcha on May 04, 2018 @ 17:02.</p>
 */
public class PermissionHelper {
    /**
     * Request code for location permission request.
     *
     * @see android.support.v7.app.AppCompatActivity#onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String LOG_TAG = "PermissionHelper";

    /**
     * Checks to see if the user has granted the app access to the ACCESS_COARSE_LOCATION permission
     * and returns true or false.
     *
     * @return True/False - The user has granted access to COARSE_LOCATION info
     */
    public static boolean checkPermissions(Context context) {
        int permissionState = ActivityCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) +
                ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION);
        return permissionState == PERMISSION_GRANTED;
    }

    public static void requestPermissions(Activity activity) {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_COARSE_LOCATION)
                && ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Log.i(LOG_TAG, "requestPermissions: Displaying permission rationale to provide context.");

            AlertDialog dialog = getRationaleDialog(activity);
            dialog.show();
        } else {
            Log.i(LOG_TAG, "requestPermissions: Requesting permission.");

            startLocationPermissionRequest(activity);
        }
    }

    private static AlertDialog getRationaleDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Your location is required in order for the app to properly function.")
                .setTitle("What's Poppin'?");

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startLocationPermissionRequest(activity);
            }
        });

        return builder.create();
    }

    private static void startLocationPermissionRequest(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }
}
