package com.hfsolutions.whatspoppin.util;

import com.google.android.gms.maps.model.LatLng;

/**
 * <p>Created by Alcha on Apr 27, 2018 @ 02:53.</p> Contains methods meant to aid in working with
 * the {@link com.google.firebase.firestore.FirebaseFirestore} that stores all of our pin data.
 */
public class DBHelper {
    /**
     * Using the provided latitude and longitude, get the collection name where it would be stored
     * in the {@link com.google.firebase.firestore.FirebaseFirestore}.
     *
     * @param lat {@link Double} representation of the latitude
     * @param lng {@link Double} representation of the longitude
     *
     * @return {@link String} name of the collection to store the given data
     */
    public static String getCollectionName(double lat, double lng) {
        return convertCoordToStr(lat) + ";" + convertCoordToStr(lng);
    }

    /**
     * Using the provided latitude and longitude, get the collection name where it would be stored
     * in the {@link com.google.firebase.firestore.FirebaseFirestore}.
     *
     * @param latLng {@link LatLng} The latitude and longitude of the users location
     *
     * @return {@link String} Name of the collection to store the given data
     */

    public static String getCollectionName(LatLng latLng) {
        return convertCoordToStr(latLng.latitude) + ";" + convertCoordToStr(latLng.longitude);
    }

    /**
     * Convert the given coordinates to a {@link String} and strips everything following the
     * period.
     *
     * @param input {@link Double} The coord to convert to a {@link String}
     *
     * @return {@link String} Value of the coord without the numbers following the period
     */
    private static String convertCoordToStr(double input) {
        String str = String.valueOf(input);

        return str.substring(0, str.indexOf("."));
    }
}
