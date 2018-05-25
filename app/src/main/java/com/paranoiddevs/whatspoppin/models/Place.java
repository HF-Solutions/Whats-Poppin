package com.paranoiddevs.whatspoppin.models;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.paranoiddevs.whatspoppin.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Created by Alcha on Apr 02, 2018 @ 05:51.</p> Used to help with storing the proper information
 * for a place between platforms. There is no public constructor or setter methods, you must use the
 * static factory method {@link #buildNewPlace(String, String, double, double)} which will generate
 * a {@link Place} object for you and return it for use.
 */

public class Place {
    /** Represents the name of the current place. */
    private String mName;

    /** Represents the description of the current place (optional). */
    private String mDesc;

    /** Represents the latitude of the current place. */
    private double mLat;

    /** Represents the longitude of the current place. */
    private double mLng;

    public Place() {
    }

    /**
     * Generates a new {@link Place} object using the provided values and returns it.
     *
     * @param name Name of the place
     * @param desc Brief description of the place
     * @param lat  Latitude of the location
     * @param lng  Longitude of the location
     *
     * @return A newly built {@link Place} object.
     */
    public static Place buildNewPlace(String name, String desc, double lat, double lng) {
        Place place = new Place();

        place.setName(name);
        place.setDesc(desc);
        place.setLat(lat);
        place.setLng(lng);

        return place;
    }

    /**
     * Generates a new {@link Place} object using the provided {@link DocumentSnapshot} from the
     * database and returns it with the stored values.
     *
     * @param document The DocumentSnapshot containing the Places information
     *
     * @return A Place object populated with the given values
     */
    @SuppressWarnings("ConstantConditions") // Values are checked for safety before use
    public static Place buildNewPlace(DocumentSnapshot document) {
        Place place = new Place();

        if (document.getString("name") != null) place.setName(document.getString("name"));
        else place.setName("N/A");

        if (document.getString("desc") != null) place.setDesc(document.getString("desc"));
        else place.setDesc("N/A");

        if (document.getDouble("lat") != null) place.setLat(document.getDouble("lat"));
        else place.setLat(Constants.DEFAULT_LOCATION.latitude);

        if (document.getDouble("lng") != null) place.setLng(document.getDouble("lng"));
        else place.setLng(Constants.DEFAULT_LOCATION.longitude);

        return place;
    }

    @Override
    public String toString() {
        return mName + " - " + mDesc + " @ (" + mLat + ", " + mLng + ")";
    }

    /**
     * Converts the currently generated Place to a {@literal Map<String, Object>} with the stored
     * values and returns the map. Mostly used for passing the data to the FirebaseFirestore as this
     * is the default format it accepts.
     *
     * @return {@link Map}<{@link String}, {@link Object}> containing the name, desc, lat, and lng.
     */
    public Map<String, Object> convertToMap() {
        Map<String, Object> out = new HashMap<>();

        out.put(Constants.PLACE_NAME_KEY, mName);
        out.put(Constants.PLACE_DESC_KEY, mDesc);
        out.put(Constants.PLACE_LAT_KEY, mLat);
        out.put(Constants.PLACE_LNG_KEY, mLng);

        return out;
    }

    public List<HashMap<String, String>> parseJSON(JSONObject jsonObject) {
        JSONArray jPlaces = null;

        try {
            jPlaces = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jPlaces == null) return new ArrayList<>();
        else return getPlacesFromJSON(jPlaces);
    }

    private List<HashMap<String, String>> getPlacesFromJSON(JSONArray jPlaces) {
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> place;

        for (int x = 0; x < jPlaces.length(); x++) {
            try {
                place = getPlaceFromJSON((JSONObject) jPlaces.get(x));
                placesList.add(place);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;
    }

    private HashMap<String, String> getPlaceFromJSON(JSONObject jPlace) {
        HashMap<String, String> place = new HashMap<>();
        String placeName = "N/A";
        String vicinity = "N/A";
        String latitude;
        String longitude;
        String reference;

        try {
            if (!jPlace.isNull("name"))
                placeName = jPlace.getString("name");

            if (!jPlace.isNull("vicinity"))
                vicinity = jPlace.getString("vicinity");

            latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = jPlace.getString("reference");

            place.put("place_name", placeName);
            place.put("vicinity", vicinity);
            place.put("lat", latitude);
            place.put("lng", longitude);
            place.put("reference", reference);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return place;
    }

    public String getName() {
        return mName;
    }

    private void setName(String name) {
        mName = name;
    }

    public String getDesc() {
        return mDesc;
    }

    private void setDesc(String desc) {
        mDesc = desc;
    }

    public double getLat() {
        return mLat;
    }

    private void setLat(double lat) {
        mLat = lat;
    }

    public double getLng() {
        return mLng;
    }

    private void setLng(double lng) {
        mLng = lng;
    }

    /**
     * Adds the current place to the provided map as a {@link Marker} and returns the generated
     * Marker object for later use, such as removal or temporarily hiding it.
     *
     * @param map {@link GoogleMap} you wish to add the Marker to
     *
     * @return Marker that was added to the provided GoogleMap
     */
    public Marker addMarkerToMap(GoogleMap map) {
        return map.addMarker(new MarkerOptions().title(mName).snippet(mDesc).position(new LatLng(mLat, mLng)));
    }
}