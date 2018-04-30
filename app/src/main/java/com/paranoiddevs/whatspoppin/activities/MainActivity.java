package com.paranoiddevs.whatspoppin.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Location;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.paranoiddevs.whatspoppin.R;
import com.paranoiddevs.whatspoppin.models.Place;
import com.paranoiddevs.whatspoppin.util.Constants;
import com.paranoiddevs.whatspoppin.util.MapInfoAdapter;

import static com.paranoiddevs.whatspoppin.util.DBHelper.getCollectionName;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    private static final String LOG_TAG = "MainActivity";
    private final Context mContext = this;
    private DrawerLayout mDrawer;
    private NavigationView mNavView;
    private GoogleMap mMap;

    /** {@link FusedLocationProviderClient} used to retrieve the users location */
    private FusedLocationProviderClient mFusedLocationProviderClient;

    /** The FirebaseFirestore connection to our pin data */
    private FirebaseFirestore mDB;

    /** The geographical location where the device is currently located. */
    private Location mLastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupBaseVars();
        setupNavBar();
        setupFab();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        updateUI();

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
            getDeviceLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new MapInfoAdapter(this));

        if (checkPermissions()) {
            updateUI();

            getDeviceLocation();
        } else requestPermissions();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
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

        if (checkPermissions()) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            requestPermissions();
        }
    }

    /**
     * Gets the current location using a {@link FusedLocationProviderClient} and then stores the
     * value as the {@link #mLastKnownLocation} variable if it was successfully retrieved. If no
     * location was retrieved, the camera is moved to a {@link Constants#DEFAULT_LOCATION} which is
     * <a href="https://en.wikipedia.org/wiki/Pole_of_inaccessibility">Point Nemo</a>.
     */
    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        if (checkPermissions()) {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, getOnCompleteListener());
        }
    }

    /**
     * Builds and returns the {@link OnCompleteListener} used when retrieving the users location. If
     * the task is successful, store the user location as {@link #mLastKnownLocation} place a marker
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
                    mLastKnownLocation = task.getResult();
                    LatLng currLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLatLng, Constants.DEFAULT_ZOOM));
                    mMap.addMarker(new MarkerOptions().title("Current Location").position(currLatLng));
                } else {
                    Log.d(LOG_TAG, "onComplete: Current location is null.");
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Constants.DEFAULT_LOCATION, Constants.DEFAULT_ZOOM));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }
        };
    }

    /**
     * Setup the basic variables used by the {@link MainActivity}, such as the
     * {@link #mFusedLocationProviderClient}, {@link #mDB}, and the
     * {@link SupportMapFragment}.
     */
    private void setupBaseVars() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDB = FirebaseFirestore.getInstance();
    }

    /**
     * Setup the {@link FloatingActionButton} by adding the
     * {@link android.view.View.OnClickListener} responsible for adding new locations. If the FAB is
     * clicked, an AlertDialog appears asking the user for information regarding the location.
     */
    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng currLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLatLng, Constants.DEFAULT_ZOOM), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        Toast.makeText(mContext, "Loading...", Toast.LENGTH_SHORT).show();
                        showNewLocationDialog();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
            }
        });
    }

    /**
     * Takes a snapshot of the current location displayed on the map and displays an AlertDialog
     * to accept input about the location in order to store it in our DB.
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
     * @param dialogInterface The {@link DialogInterface} for the AlertDialog in order to dismiss it
     *
     * @return {@link android.view.View.OnClickListener}
     */
    private View.OnClickListener getDialogOKListener(final EditText locationName, final EditText locationDesc, final DialogInterface dialogInterface) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get user input and set it to result
                // edit text
                if (getTextContent(locationName).length() == 0) {
                    Toast.makeText(getBaseContext(), "You must provide a location name.", Toast.LENGTH_SHORT).show();
                } else {
                    Place place = Place.buildNewPlace(
                            getTextContent(locationName),
                            getTextContent(locationDesc),
                            mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude());

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView title = toolbar.findViewById(R.id.appbar_title);
        title.setTypeface(Typeface.createFromAsset(getAssets(), "font/Lobster.ttf"));

        setSupportActionBar(toolbar);

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
     * Retrieves the text content from the provided {@link EditText} view and trims any excess
     * whitespace.
     *
     * @param editText The {@link EditText} you wish to retrieve the content from
     *
     * @return A {@link String} containing the content of the provided {@link EditText}
     */
    private String getTextContent(EditText editText) {
        return editText.getText().toString().trim();
    }
}
