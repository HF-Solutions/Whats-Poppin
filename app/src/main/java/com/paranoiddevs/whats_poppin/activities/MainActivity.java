package com.paranoiddevs.whats_poppin.activities;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.paranoiddevs.whats_poppin.R;
import com.paranoiddevs.whats_poppin.models.PlaceJSON;
import com.paranoiddevs.whats_poppin.utils.BasicListeners;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.paranoiddevs.whats_poppin.utils.RequestHelper.buildRequest;
import static com.paranoiddevs.whats_poppin.utils.RequestHelper.downloadUrl;

public class MainActivity extends BaseActivity
        implements OnMapReadyCallback {
  private static final String LOG_TAG = "MainActivity";

  GoogleMap mGoogleMap;
  SupportMapFragment mMapFragment;
  LocationRequest mLocationRequest;
  GoogleApiClient mGoogleApiClient;
  boolean mFirstRun = true;

  Location mLastLocation;
  Marker mCurrLocationMarker;

  /**
   * Request code for location permission request.
   *
   * @see #onRequestPermissionsResult(int, String[], int[])
   */
  public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

  /**
   * For the new FusedLocationProviderClient support.
   */
  private FusedLocationProviderClient mClient;

  LocationCallback mLocationCallback = new LocationCallback() {
    @Override
    public void onLocationResult(LocationResult locationResult) {
      if (mFirstRun) {
        for (Location location : locationResult.getLocations()) {
          Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
          mLastLocation = location;
          if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
          }

          //Place current location marker
          LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
          MarkerOptions markerOptions = new MarkerOptions();
          markerOptions.position(latLng);
          markerOptions.title("Current Position");
          markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
          mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

          //move map camera
          mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));// Query places w/ curr location

          StringBuilder builder = buildRequest(location);
          PlacesTask task = new PlacesTask();
          task.execute(builder.toString());
        }
      }
    }
  };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);

    mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    mMapFragment.getMapAsync(this);

    mClient = LocationServices.getFusedLocationProviderClient(this);

    setupActionBar();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    Log.i(LOG_TAG, "onRequestPermissionsResult()");

    if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
      if (grantResults.length <= 0) {
        Log.i(LOG_TAG, "onRequestPermissionsResult: User interaction was cancelled.");
      } else if (grantResults[0] == PERMISSION_GRANTED) {
        toggleLocationUpdates();
      } else {
        // Permission denied
        showSnackbar("Permission denied explanation.", "Settings?", BasicListeners.permissionDeniedSnackbarListener(this));
      }
    }
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mGoogleMap = googleMap;

    mLocationRequest = new LocationRequest();
    mLocationRequest.setInterval(120000); // two minute interval
    mLocationRequest.setFastestInterval(60000);
    mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

    if (checkPermissions()) {
      //Location Permission already granted
      toggleLocationUpdates();
    } else {
      //Request Location Permission
      requestPermissions();
    }
  }

  @SuppressLint("MissingPermission")
  private void toggleLocationUpdates() {
    if (mGoogleMap.isMyLocationEnabled()) {
      mClient.removeLocationUpdates(mLocationCallback);
      mGoogleMap.setMyLocationEnabled(false);
    } else {
      mClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
      mGoogleMap.setMyLocationEnabled(true);
    }
  }

  private boolean checkPermissions() {
    int permissionState = ActivityCompat.checkSelfPermission(this,
            ACCESS_COARSE_LOCATION);
    return permissionState == PERMISSION_GRANTED;
  }

  private void requestPermissions() {
    boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
            ACCESS_COARSE_LOCATION);

    if (shouldProvideRationale) {
      Log.i(LOG_TAG, "requestPermissions: Displaying permission rationale to provide context.");

      showSnackbar("Permission rationale.", getString(android.R.string.ok),
              new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  startLocationPermissionRequest();
                }
              });
    } else {
      Log.i(LOG_TAG, "requestPermissions: Requesting permission.");

      startLocationPermissionRequest();
    }
  }

  private void startLocationPermissionRequest() {
    ActivityCompat.requestPermissions(this,
            new String[]{ACCESS_COARSE_LOCATION},
            LOCATION_PERMISSION_REQUEST_CODE);
  }

  private class PlacesTask extends AsyncTask<String, Integer, String> {
    String data = null;

    @Override
    protected String doInBackground(String... strings) {
      try {
        data = downloadUrl(strings[0]);
      } catch (Exception e) {
        Log.e(LOG_TAG, "doInBackground: ", e);
      }

      return data;
    }

    @Override
    protected void onPostExecute(String result) {
      ParserTask parserTask = new ParserTask();

      parserTask.execute(result);
    }
  }

  private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
    JSONObject jObject;

    @Override
    protected List<HashMap<String, String>> doInBackground(String... strings) {
      List<HashMap<String, String>> places = null;
      PlaceJSON placeJSON = new PlaceJSON();

      try {
        jObject = new JSONObject(strings[0]);
        places = placeJSON.parse(jObject);
      } catch (JSONException e) {
        e.printStackTrace();
      }

      return places;
    }

    @Override
    protected void onPostExecute(List<HashMap<String, String>> list) {
      if (!mFirstRun)
        mGoogleMap.clear();

      mFirstRun = false;

      for (int x = 0; x < list.size(); x++) {
        // Creating a marker
        MarkerOptions options = new MarkerOptions();

        // Getting a place from the places list
        HashMap<String, String> hmPlace = list.get(x);

        // Getting LatLng
        double lat = Double.parseDouble(hmPlace.get("lat"));
        double lng = Double.parseDouble(hmPlace.get("lng"));

        // Getting name
        String name = hmPlace.get("place_name");
        String vicinity = hmPlace.get("vicinity");
        LatLng latLng = new LatLng(lat, lng);

        options.position(latLng);
        options.title(name + " : " + vicinity);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

        mGoogleMap.addMarker(options);
      }
    }
  }
}
