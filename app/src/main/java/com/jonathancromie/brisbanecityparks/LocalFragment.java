package com.jonathancromie.brisbanecityparks;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class LocalFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
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

    public LocalFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_local, container, false);

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getContext().getSystemService(getContext().LOCATION_SERVICE);

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

        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
//
        parks = new ArrayList<Parks>();
        parkAdapter = new ParkAdapter(getActivity().getLayoutInflater(), parks);
        RecyclerView recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(parkAdapter);

        try {
            mClient = new MobileServiceClient(
                    "https://brisbanecityparks.azure-mobile.net/",
                    "zekjnWkJSxVYLuumxxydGozfpOSlBn97",
                    getContext()
            );
            parksTable = mClient.getTable(Parks.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // Load the items from the Mobile Service
        refreshItemsFromTable();

        return rootView;
    }

    private void refreshItemsFromTable() {
        // Get the items that weren't marked as completed and add them in the adapter
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
//	                final MobileServiceList<Parks> result = parksTable.where().field("complete").eq(false).execute().get();
//                    final MobileServiceList<Parks> result = parksTable.where().field("active").eq(true).execute().get();
                    final MobileServiceList<Parks> result = parksTable.top(250).execute().get();
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
//	                        parkAdapter.clear();
                            locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
                            LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                            int counter = 0;
                            for (final Parks park : result) {
                                
                                LatLng parkLocation = new LatLng(park.latitude, park.longitude);
                                park.distance = SphericalUtil.computeDistanceBetween(userLocation, parkLocation);
                                parks.add(park);
                                counter++;

                            }

                            for (Parks park : parks) {
                                parkAdapter.addPark(park);
                                parkAdapter.notifyItemInserted(parkAdapter.parks.size() - 1);
                            }


                        }
                    });
                } catch (Exception exception) {
//	                createAndShowDialog(exception, "Error");
                }
                return null;
            }
        }.execute();
    }

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
