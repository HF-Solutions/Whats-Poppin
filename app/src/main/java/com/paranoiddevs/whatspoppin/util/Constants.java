package com.paranoiddevs.whatspoppin.util;

import com.google.android.gms.maps.model.LatLng;

/**
 * <p>Created by Alcha on Apr 26, 2018 @ 22:03.</p> Contains various constants used throughout the
 * app.
 */
public class Constants {
    // Constants used by the {@link com.paranoiddevs.whatspoppin.models.Place} model.
    /** Represents the key value for a places name in their converted map. */
    public static final String PLACE_NAME_KEY = "name";

    /** Represents the key value for a places description in their converted map. */
    public static final String PLACE_DESC_KEY = "desc";

    /** Represents the key value for a places latitude in their converted map. */
    public static final String PLACE_LAT_KEY = "lat";

    /** Represents the key value for a places longitude in their converted map. */
    public static final String PLACE_LNG_KEY = "lng";

    /** Represents the default zoom for maps */
    public static final int DEFAULT_ZOOM = 15;

    /**
     * Represents the default location when one cannot be found. Resolves to <a
     * href="https://en.wikipedia.org/wiki/Pole_of_inaccessibility">Point Nemo</a>
     */
    public static final LatLng DEFAULT_LOCATION = new LatLng(-48.876667, -123.39333);
}
