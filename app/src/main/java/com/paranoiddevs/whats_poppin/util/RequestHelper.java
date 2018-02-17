package com.paranoiddevs.whats_poppin.util;

import android.location.Location;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * <p>Created by Alcha on Feb 16, 2018 @ 22:56.</p>
 */

public class RequestHelper {
    private static final String LOG_TAG = RequestHelper.class.getName();

    public static StringBuilder buildRequest(Location currLocation) {
        double mLat = currLocation.getLatitude();
        double mLong = currLocation.getLongitude();

        return new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?")
                .append("location=").append(mLat).append(",").append(mLong)
                .append("&radius=5000")
                .append("&types=").append("bar,night_club")
                .append("&key=AIzaSyCU087XzYoivJlMKsvSzC7OoSgVHg1QftQ");
    }

    public static String downloadUrl(String strUrl) {
        String data = "";
        InputStream iStream;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            iStream = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            reader.close();
            iStream.close();
        } catch (IOException ioError) {
            Log.e(LOG_TAG, "downloadUrl: error...", ioError);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return data;
    }
}
