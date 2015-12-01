package com.jonathancromie.brisbanecityparks;

import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private List<Parks> parks;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ParkAdapter parkAdapter;

    private MobileServiceClient mClient;
    private MobileServiceTable<Parks> parksTable;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastKnownLocation;
    private String locationProvider;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        // Setup drawer view
        setUpDrawerContent(navigationView);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerToggle = setUpDrawerToggle();

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {

            }

            public void onProviderDisabled(String provider) {}
        };

        locationProvider = LocationManager.GPS_PROVIDER;
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
        lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        //        parkAdapter = new ParkAdapter(parks);
        parks = new ArrayList<Parks>();
        parkAdapter = new ParkAdapter(getLayoutInflater(), parks);
        recyclerView.setAdapter(parkAdapter);

        try {
            mClient = new MobileServiceClient(
                    "https://brisbanecityparks.azure-mobile.net/",
                    "mycnswzLkAkAlVpcnBcPQhBKcjSEyT16",
                    this
            );
            parksTable = mClient.getTable(Parks.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // Load the items from the Mobile Service
        refreshItemsFromTable();
    }

    // Make sure this is the method with just `Bundle` as the signature
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
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
        switch (id) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                return true;
        }

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    private void refreshItemsFromTable() {
		// Get the items that weren't marked as completed and add them in the adapter
	    new AsyncTask<Void, Void, Void>() {

	        @Override
	        protected Void doInBackground(Void... params) {
	            try {
//	                final MobileServiceList<Parks> result = parksTable.where().field("complete").eq(false).execute().get();
                    final MobileServiceList<Parks> result = parksTable.where().execute().get();
	                runOnUiThread(new Runnable() {

	                    @Override
	                    public void run() {
//	                        parkAdapter.clear();
                            locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
                            LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
//                            Collections.sort(parks, new SortParks(userLocation));

	                        for (final Parks park : result) {
                                LatLng parkLocation = new LatLng(Double.parseDouble(park.latitude),
                                        Double.parseDouble(park.longitude));
                                park.distance = SphericalUtil.computeDistanceBetween(userLocation, parkLocation);
                                parks.add(park);

	                        }

                            for (Parks park : parks) {
                                parkAdapter.addPark(park);
                                parkAdapter.notifyItemInserted(parkAdapter.parks.size()-1);
                            }


	                    }
	                });
    	            } catch (Exception exception) {
//	                createAndShowDialog(exception, "Error");
	            }
	            return null;
	        }
	    }.execute();

//		TODO Comment out these lines to remove the in-memory store
//        mAdapter.clear();
//        for (Parks park : parks)
//        {
//            if (park.isComplete() == false)
//                mAdapter.add(park);
//        }
    }

    private ActionBarDrawerToggle setUpDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    private void setUpDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
//                item.setChecked(true);
//                drawerLayout.closeDrawers();
                selectDrawerItem(item);
                return true;
            }
        });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the planet to show based on
        // position
        Fragment fragment = null;

        Class fragmentClass;
        switch(menuItem.getItemId()) {
            case R.id.local_fragment:
                fragmentClass = LocalFragment.class;
                break;
            case R.id.whats_hot_fragment:
                fragmentClass = WhatsHotFragment.class;
                break;
            case R.id.trending_fragment:
                fragmentClass = TrendingFragment.class;
                break;
            default:
                fragmentClass = LocalFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit();

        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        drawerLayout.closeDrawers();
    }

//    /**
//     * Creates a dialog and shows it
//     *
//     * @param exception
//     *            The exception to show in the dialog
//     * @param title
//     *            The dialog title
//     */
//    private void createAndShowDialog(Exception exception, String title) {
//        createAndShowDialog(exception.toString(), title);
//    }
//
//    /**
//     * Creates a dialog and shows it
//     *
//     * @param message
//     *            The dialog message
//     * @param title
//     *            The dialog title
//     */
//    private void createAndShowDialog(String message, String title) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        builder.setMessage(message);
//        builder.setTitle(title);
//        builder.create().show();
//    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
