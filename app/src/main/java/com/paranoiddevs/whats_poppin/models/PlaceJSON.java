package com.paranoiddevs.whats_poppin.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>Created by Alcha on Feb 16, 2018 @ 22:54.</p>
 */

public class PlaceJSON {
    public List<HashMap<String, String>> parse(JSONObject jsonObject) {
        JSONArray jPlaces = null;
        try {
            jPlaces = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPlaces(jPlaces);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> place;

        for (int x = 0; x < jPlaces.length(); x++) {
            try {
                place = getPlace((JSONObject) jPlaces.get(x));
                placesList.add(place);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;
    }

    private HashMap<String, String> getPlace(JSONObject jPlace) {
        HashMap<String, String> place = new HashMap<>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
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
}
