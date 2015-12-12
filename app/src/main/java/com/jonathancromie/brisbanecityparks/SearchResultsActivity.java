package com.jonathancromie.brisbanecityparks;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RunnableFuture;

public class SearchResultsActivity extends AppCompatActivity {
    private static final String MOBILE_SERVICE_URL = "https://brisbanecityparks.azure-mobile.net/";
    private static final String MOBILE_SERVICE_KEY = "zekjnWkJSxVYLuumxxydGozfpOSlBn97";

    private List<Park> parks;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ParkAdapter parkAdapter;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    private MobileServiceClient mClient;
    private MobileServiceTable<Park> parkTable;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastKnownLocation;
    private String locationProvider;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(R.string.search_title);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }

        // Find our drawer view
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        // Setup drawer view
//        setUpDrawerContent(navigationView);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
//        drawerToggle = setUpDrawerToggle();
//        drawerToggle = setUpDrawerToggle();

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);

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

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
//
        parks = new ArrayList<Park>();
        parkAdapter = new ParkAdapter(getLayoutInflater(), parks);
        recyclerView.setAdapter(parkAdapter);

        try {
            mClient = new MobileServiceClient(
                    MOBILE_SERVICE_URL,
                    MOBILE_SERVICE_KEY, this);
        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("Error creating the Mobile Service. " +
                    "Verify the URL"), "Error");
        }

        parkTable = mClient.getTable(Park.class);

        searchParks(query);
    }

    private void searchParks(final String query) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String search = query.toLowerCase();
                    parkTable.where().toLower("suburb").eq(search).execute(new TableQueryCallback<Park>() {
                        @Override
                        public void onCompleted(final List<Park> result, int count, Exception exception, ServiceFilterResponse response) {
                            serialise();
                            if (exception != null) {
                                createAndShowDialog(exception, "Error");
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
                                        LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                                        for (Park park : result) {
                                            LatLng parkLocation = new LatLng(park.latitude, park.longitude);
                                            park.setDistance(SphericalUtil.computeDistanceBetween(userLocation, parkLocation));
                                            parks.add(park);
                                        }

                                        for (Park park : parks) {
                                            parkAdapter.addPark(park);
                                            parkAdapter.notifyItemInserted(parkAdapter.parks.size() - 1);
                                        }
                                    }
                                });
                            }
                        }
                    });

                } catch (Exception exception) {
                    createAndShowDialog(exception, "Error");
                }
                return null;
            }
        }.execute();


    }

    private void serialise() {
        mClient.registerSerializer(Review[].class, new ReviewArraySerializer());
        mClient.registerDeserializer(Review[].class, new ReviewArraySerializer());
    }

    /**
     * Creates a dialog and shows it
     *
     * @param exception
     *            The exception to show in the dialog
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(Exception exception, String title) {
        createAndShowDialog(exception.toString(), title);
    }

    /**
     * Creates a dialog and shows it
     *
     * @param message
     *            The dialog message
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(String message, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

}
