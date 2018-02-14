package com.paranoiddevs.whats_poppin;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.paranoiddevs.whats_poppin.PermissionUtils.isPermissionGranted;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMyLocationButtonClickListener {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;

    private RequestQueue mQueue;
    private StringRequest mStringRequest;
    private SupportMapFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        initVariables();

        //mQueue.add(mStringRequest);
    }

    private void initVariables() {
        /*mQueue = Volley.newRequestQueue(context);
        mStringRequest = new StringRequest(Request.Method.GET, "http://tron.ninja", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("Response is " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error!");
                System.err.println(error);
            }
        });*/

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Is this place poppin'?");

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.out.println("OK has been pressed!");
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.out.println("CANCEL has been pressed!");
            }
        });

        final AlertDialog dialog = builder.create();

        FloatingActionButton fab = findViewById(R.id.map_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        if (PermissionUtils.isLocationGranted(this)) {
            System.out.println("Location permission granted.");
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
            client.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return;

        if (isPermissionGranted(permissions, grantResults, ACCESS_FINE_LOCATION))
            enableMyLocation();
        else mPermissionDenied = true;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private boolean enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    ACCESS_FINE_LOCATION, true);
            return false;
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            return true;
        }

        return false;
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}
