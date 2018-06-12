package com.hfsolutions.whatspoppin.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.hfsolutions.whatspoppin.R;
import com.hfsolutions.whatspoppin.models.Place;
import com.hfsolutions.whatspoppin.util.Constants;
import com.hfsolutions.whatspoppin.util.MapInfoAdapter;
import com.hfsolutions.whatspoppin.util.PermissionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import static com.hfsolutions.whatspoppin.util.Constants.KEY_CAMERA_POSITION;
import static com.hfsolutions.whatspoppin.util.Constants.KEY_LOCATION;
import static com.hfsolutions.whatspoppin.util.DBHelper.getCollectionName;
import static com.hfsolutions.whatspoppin.util.RequestHelper.downloadUrl;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    private static final String LOG_TAG = "MainActivity";
    private final Context mContext = this;
    private NavigationView mNavView;
    private DrawerLayout mDrawer;
    private GoogleMap mMap;

    private CameraPosition mCameraPosition;

    private boolean mFirstRun = true;

    /** {@link FusedLocationProviderClient} used to retrieve the users location */
    private FusedLocationProviderClient mFusedLocationProviderClient;

    /** The FirebaseFirestore connection to our pin data */
    private FirebaseFirestore mDB;

    /** The last known {@link LatLng} for where the device is located. */
    private LatLng mLastKnownLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrieveInstanceState(savedInstanceState);

        setupBaseVars();
        setupNavBar();
        setupFab();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START))
            mDrawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        updateUI();

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            getDeviceLocation();

            startLocationService();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new MapInfoAdapter(this));
        if (mCameraPosition != null)
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));

        if (PermissionHelper.checkPermissions(this)) {
            updateUI();

            getDeviceLocation();

            startLocationService();
        } else PermissionHelper.requestPermissions(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                mNavView.setCheckedItem(R.id.nav_home);
                System.out.println("nav_home selected.");
                break;

            case R.id.nav_list:
                mNavView.setCheckedItem(R.id.nav_list);
                System.out.println("nav_list selected.");
                break;

            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.nav_feedback:
                startActivity(new Intent(this, FeedbackActivity.class));
                break;

            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Updates the map based on the current permission settings.
     */
    @SuppressLint("MissingPermission")
    private void updateUI() {
        if (mMap == null) return;

        if (PermissionHelper.checkPermissions(this)) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            PermissionHelper.requestPermissions(this);
        }
    }

    /**
     * Gets the current location using a {@link FusedLocationProviderClient} and then stores the
     * value as the {@link #mLastKnownLatLng} variable if it was successfully retrieved. If no
     * location was retrieved, the camera is moved to a {@link Constants#DEFAULT_LOCATION} which is
     * <a href="https://en.wikipedia.org/wiki/Pole_of_inaccessibility">Point Nemo</a>.
     */
    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        if (PermissionHelper.checkPermissions(this)) {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, getOnCompleteListener());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLatLng);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Builds and returns the {@link OnCompleteListener} used when retrieving the users location. If
     * the task is successful, store the user location as {@link #mLastKnownLatLng} place a marker
     * and move the camera there. If the task is <i>not</i> successful, move the camera to our
     * {@link Constants#DEFAULT_LOCATION}.
     *
     * @return {@link OnCompleteListener}
     */
    private OnCompleteListener<Location> getOnCompleteListener() {
        return new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    mLastKnownLatLng = getCurrLatLng(task.getResult());
                    updateCamera();

                    addLocationsToMap();
                    mFirstRun = false;
                } else {
                    Log.d(LOG_TAG, "onComplete: Current location is null.");
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Constants.DEFAULT_LOCATION, Constants.DEFAULT_ZOOM));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }
        };
    }

    private void updateCamera() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLastKnownLatLng, Constants.DEFAULT_ZOOM));
    }

    private void addLocationsToMap() {
        mDB.collection(getCollectionName(mLastKnownLatLng)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        // Build a place object with the given document
                        Place place = Place.buildNewPlace(doc);

                        // Add a marker to the map
                        place.addMarkerToMap(mMap);
                    }
                } else
                    Log.w(LOG_TAG, "onComplete: error getting documents...", task.getException());
            }
        });
    }

    /**
     * Setup the basic variables used by the {@link MainActivity}, such as the {@link
     * #mFusedLocationProviderClient}, {@link #mDB}, and the {@link SupportMapFragment}.
     */
    private void setupBaseVars() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDB = FirebaseFirestore.getInstance();
    }

    /**
     * Setup the {@link FloatingActionButton} by adding the {@link android.view.View.OnClickListener}
     * responsible for adding new locations. If the FAB is clicked, an AlertDialog appears asking
     * the user for information regarding the location.
     */
    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(getFabClickListener());
    }

    /**
     * Builds and returns an {@link android.view.View.OnClickListener} that's used by the {@link
     * FloatingActionButton} on the {@link MainActivity}. When a user clicks the FAB, the camera is
     * animated to the users current location, a screenshot is taken, and the new location
     * AlertDialog is displayed.
     *
     * @return {@link android.view.View.OnClickListener} for the FAB
     */
    private View.OnClickListener getFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Display toast so the user knows something is happening
                Toast.makeText(mContext, "Loading...", Toast.LENGTH_SHORT).show();

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLastKnownLatLng, Constants.DEFAULT_ZOOM), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        // Display the AlertDialog for a new location and associated info
                        showNewLocationDialog();
                    }

                    @Override
                    public void onCancel() {
                        // Do nothing
                    }
                });
            }
        };
    }

    /**
     * Takes a snapshot of the current location displayed on the map and displays an AlertDialog to
     * accept input about the location in order to store it in our DB.
     *
     * TODO: Use a custom dialog instead of the standard AlertDialog.
     */
    private void showNewLocationDialog() {
        mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                View promptsView = getLayoutInflater().inflate(R.layout.new_entry, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                alertDialogBuilder.setView(promptsView);

                final EditText locationName = promptsView.findViewById(R.id.edit_text_location_name);
                final EditText locationDesc = promptsView.findViewById(R.id.edit_text_location_desc);
                final ImageView screenshotImage = promptsView.findViewById(R.id.map_screenshot);
                screenshotImage.setImageBitmap(bitmap);

                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(getDialogOKListener(locationName, locationDesc, dialogInterface));
                    }
                });

                alertDialog.show();
            }
        });
    }

    /**
     * Builds and returns the {@link android.view.View.OnClickListener} used by the AlertDialog for
     * information about a new location.
     *
     * @param locationName    The {@link EditText} containing the users input for location name
     * @param locationDesc    The {@link EditText} containing the users input for location desc
     * @param dialogInterface The {@link DialogInterface} for the AlertDialog in order to dismiss
     *                        it
     *
     * @return {@link android.view.View.OnClickListener}
     */
    private View.OnClickListener getDialogOKListener(final EditText locationName, final EditText locationDesc, final DialogInterface dialogInterface) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getTextContent(locationName).length() == 0) {
                    Toast.makeText(getBaseContext(), "You must provide a location name.", Toast.LENGTH_SHORT).show();
                } else {
                    Place place = Place.buildNewPlace(
                            getTextContent(locationName), getTextContent(locationDesc),
                            mLastKnownLatLng.latitude, mLastKnownLatLng.longitude);

                    mDB.collection(getCollectionName(place.getLat(), place.getLng())).add(place.convertToMap())
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(getBaseContext(), "Congrats, you've added your location to the database!", Toast.LENGTH_LONG).show();
                                }
                            });

                    dialogInterface.dismiss();
                }
            }
        };
    }

    /**
     * Setup the {@link Toolbar} and {@link DrawerLayout} by setting the custom font and adding
     * action listeners.
     */
    private void setupNavBar() {
        Toolbar toolbar = buildToolbar();
        setSupportActionBar(buildToolbar());

        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavView = findViewById(R.id.nav_view);
        mNavView.setNavigationItemSelectedListener(this);
        mNavView.setCheckedItem(R.id.nav_home);
    }

    /**
     * Builds and returns the {@link Toolbar} used by the {@link MainActivity}. Mostly just sets the
     * font to the custom Lobster font requested by the client.
     *
     * @return {@link Toolbar}
     */
    private Toolbar buildToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

        TextView title = toolbar.findViewById(R.id.appbar_title);
        title.setTypeface(Typeface.createFromAsset(getAssets(), "font/Lobster.ttf"));

        return toolbar;
    }

    private void retrieveInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mLastKnownLatLng = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
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
            ParserTask task = new ParserTask();
            task.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            List<HashMap<String, String>> places = null;
            Place place = new Place();

            try {
                jObject = new JSONObject(strings[0]);
                places = place.parseJSON(jObject);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "doInBackground: ", e);
            }

            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {
            if (!mFirstRun) mMap.clear();

            mFirstRun = false;

            for (int x = 0; x < list.size(); x++) {
                MarkerOptions opts = new MarkerOptions();
                HashMap<String, String> hmPlace = list.get(x);

                double lat = Double.parseDouble(hmPlace.get("lat"));
                double lng = Double.parseDouble(hmPlace.get("lng"));

                String name = hmPlace.get("place_name");
                String vicinity = hmPlace.get("vicinity");
                LatLng latLng = new LatLng(lat, lng);

                opts.position(latLng);
                opts.title(name + " : " + vicinity);
                opts.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

                mMap.addMarker(opts);
            }
        }
    }
}
