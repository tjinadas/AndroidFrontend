package com.goofood.gofood;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.goofood.gofood.com.goofood.gofood.geolocation.CustomMapFragment;
import com.goofood.gofood.com.goofood.gofood.geolocation.GeocodingLocation;
import com.goofood.gofood.com.goofood.gofood.geolocation.MapWrapperLayout;
import com.goofood.gofood.com.goofood.gofood.utilities.VolleySingleton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;


import com.goofood.gofood.com.goofood.gofood.androidsql.CustomerDatabaseHelper;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements MapWrapperLayout.OnDragListener, LocationListener,GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback {

    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    private static final String TAG = "MainActivity";

    // Google Map
    private GoogleMap googleMap;
    private CustomMapFragment mCustomMapFragment;

    private View mMarkerParentView;
    private ImageView mMarkerImageView;

    private int imageParentWidth = -1;
    private int imageParentHeight = -1;
    private int imageHeight = -1;
    private int centerX = -1;
    private int centerY = -1;

    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 500;

    private double serving_latitude;
    private double serving_longitude;

    //private static LocationService instance = null;

    private LocationManager locationManager;

    private TextView mLocationTextView;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUI();

        mDrawerList = (ListView)findViewById(R.id.navList);mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        addDrawerItems();
        setupDrawer();




        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        CustomerDatabaseHelper authTable = new CustomerDatabaseHelper(this);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            return true;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void addDrawerItems() {
        String[] osArray = { "Payment", "Active Reservations", "Order History", "Help", "Settings" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    //GoogleMap Settings

    private void initializeUI() {

        try {
            // Loading map
            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }
        mLocationTextView = (TextView) findViewById(R.id.location_text_view);
        mMarkerParentView = findViewById(R.id.marker_view_incl);
        mMarkerImageView = (ImageView) findViewById(R.id.marker_icon_view);
    }

    private void initilizeMap() {

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER


        if (googleMap == null) {
            mCustomMapFragment = ((CustomMapFragment) getFragmentManager()
                    .findFragmentById(R.id.food_map));
            mCustomMapFragment.setOnDragListener(MainActivity.this);
            mCustomMapFragment.getMapAsync(MainActivity.this);
            googleMap = mCustomMapFragment.getMap();
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }



        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        googleMap.animateCamera(cameraUpdate);

        Log.d(TAG, String.valueOf(location.getLatitude()));
        Log.d(TAG, String.valueOf(location.getLongitude()));

/*        googleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker))
                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .position(new LatLng(location.getLatitude(), location.getLongitude())));*/

        locationManager.removeUpdates(MainActivity.this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }


    @Override
    public void onDrag(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            Projection projection = (googleMap != null && googleMap
                    .getProjection() != null) ? googleMap.getProjection()
                    : null;
            if (projection != null) {
                LatLng centerLatLng = projection.fromScreenLocation(new Point(
                        centerX, centerY));
                updateLocation(centerLatLng);
            }
        }
    }

    private void updateLocation(LatLng centerLatLng) {
        if (centerLatLng != null) {
            Geocoder geocoder = new Geocoder(MainActivity.this,
                    Locale.getDefault());

            List<Address> addresses = new ArrayList<Address>();
            try {
                addresses = geocoder.getFromLocation(centerLatLng.latitude,
                        centerLatLng.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {

                String addressIndex0 = (addresses.get(0).getAddressLine(0) != null) ? addresses
                        .get(0).getAddressLine(0) : null;
                String addressIndex1 = (addresses.get(0).getAddressLine(1) != null) ? addresses
                        .get(0).getAddressLine(1) : null;
                String addressIndex2 = (addresses.get(0).getAddressLine(2) != null) ? addresses
                        .get(0).getAddressLine(2) : null;
                String addressIndex3 = (addresses.get(0).getAddressLine(3) != null) ? addresses
                        .get(0).getAddressLine(3) : null;

                String completeAddress = addressIndex0 + "," + addressIndex1;

                if (addressIndex2 != null) {
                    completeAddress += "," + addressIndex2;
                }
                if (addressIndex3 != null) {
                    completeAddress += "," + addressIndex3;
                }
                if (completeAddress != null) {
                    mLocationTextView.setText(completeAddress);
                }
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        imageParentWidth = mMarkerParentView.getWidth();
        imageParentHeight = mMarkerParentView.getHeight();
        imageHeight = mMarkerImageView.getHeight();

        centerX = imageParentWidth / 2;
        centerY = (imageParentHeight / 2) + (imageHeight / 2);
    }

    //Adding markers

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(TAG, "I am at the onMapReady");

        // Calling the Serving location API to get the locations

        final String endpointurl = "http://ec2-52-38-210-110.us-west-2.compute.amazonaws.com:8081/api/getlocations";
        RequestQueue requestqueue = VolleySingleton.getsInstance().getRequestQueue();

        Map<String,String> params = new HashMap<String, String>();

        JsonObjectRequest sr = new JsonObjectRequest(
                Request.Method.POST,
                endpointurl,
                new JSONObject(params),

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String token = response.getString("token");
                            String postalCode = response.getString("postalCode");
                            Log.d(TAG, token);
                            

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getBaseContext(), error.toString(), Toast.LENGTH_LONG).show();

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };


        String Address = "L1R1S5";

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(Address, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                serving_latitude = address.getLatitude();
                serving_longitude = address.getLongitude();

            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to connect to Geocoder", e);
        }


        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(serving_latitude, serving_longitude), 16));


        // You can customize the marker image using images bundled with
        // your app, or dynamically generated bitmaps.
        map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.add_marker))
                .anchor(0.0f, 1.0f)
                .title("Pasta")// Anchors the marker on the bottom left
                .snippet("Open: 12PM - 4PM")
                .position(new LatLng(serving_latitude, serving_longitude)));

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Info window clicked",
                Toast.LENGTH_SHORT).show();
    }

}
