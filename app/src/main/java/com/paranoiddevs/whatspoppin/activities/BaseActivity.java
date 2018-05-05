package com.paranoiddevs.whatspoppin.activities;

import android.annotation.SuppressLint;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

/**
 * <p>Created by Alcha on Mar 27, 2018 @ 19:40.</p>
 */

@SuppressLint("Registered") // Not used as an actual Activity
public class BaseActivity extends AppCompatActivity {
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String LOG_TAG = "BaseActivity";

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

    /**
     * Converts the provided {@link Location} object to a {@link LatLng} for use with the {@link
     * com.google.android.gms.maps.GoogleMap} and it's markers.
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
