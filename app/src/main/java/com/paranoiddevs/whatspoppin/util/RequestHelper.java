package com.paranoiddevs.whatspoppin.util;

import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.paranoiddevs.whatspoppin.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * <p>Created by Alcha on May 04, 2018 @ 17:41.</p>
 */
public class RequestHelper {
    private static final String LOG_TAG = "RequestHelper";

    public static StringBuilder buildRequest(LatLng latLng) {
        return new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?")
                .append("location=").append(latLng.latitude).append(",").append(latLng.longitude)
                .append("&radius=5000")
                .append("&types=").append(TextUtils.join(",", Constants.PLACE_TYPES))
                .append("&key=").append(BuildConfig.PLACES_API_KEY);
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
