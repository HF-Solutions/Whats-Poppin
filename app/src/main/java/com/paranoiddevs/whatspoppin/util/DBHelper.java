package com.paranoiddevs.whatspoppin.util;

/**
 * <p>Created by Alcha on Apr 27, 2018 @ 02:53.</p>
 */
public class DBHelper {
    public static String getCollectionName(double lat, double lng) {
        return convertCoordToStr(lat) + ";" + convertCoordToStr(lng);
    }

    private static String convertCoordToStr(double input) {
        String str = String.valueOf(input);

        return str.substring(0, str.indexOf("."));
    }
}
