package com.jonathancromie.brisbanecityparks;


import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.maps.android.SphericalUtil;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.UserAuthenticationCallback;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceJsonTable;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;


public class TrendingFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String MOBILE_SERVICE_URL = "https://brisbanecityparks.azure-mobile.net/";
    private static final String MOBILE_SERVICE_KEY = "zekjnWkJSxVYLuumxxydGozfpOSlBn97";

    private List<Park> parks;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ParkAdapter parkAdapter;

    private MobileServiceClient mClient;
    private MobileServiceTable<Park> parkTable;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastKnownLocation;
    private String locationProvider;

    private ProgressBar mProgressBar;

    MobileServiceJsonTable userTable;

    public TrendingFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        getActivity().setTitle(R.string.whatshot);

        View rootView = inflater.inflate(R.layout.fragment_whats_hot, container, false);

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
        parks = new ArrayList<Park>();
        parkAdapter = new ParkAdapter(getActivity().getLayoutInflater(), parks);
        recyclerView.setAdapter(parkAdapter);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        // Initialize the progress bar
        mProgressBar.setVisibility(ProgressBar.GONE);

        try {
            mClient = new MobileServiceClient(
                    MOBILE_SERVICE_URL,
                    MOBILE_SERVICE_KEY, getContext())
                    .withFilter(new ProgressFilter());

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("Error creating the Mobile Service. " +
                    "Verify the URL"), "Error");
        }

        parkTable = mClient.getTable("park", Park.class);

        userTable = mClient.getTable("Identities");

        // Load the items from the Mobile Service
        refreshItemsFromTable();
//        authenticate(false);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.trending);
    }





    private void refreshItemsFromTable() {
        // Get the items that weren't marked as completed and add them in the adapter
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    parkTable.where().field("suburb").eq("Sandgate").execute(new TableQueryCallback<Park>() {
                        @Override
                        public void onCompleted(final List<Park> result, int count, Exception exception, ServiceFilterResponse response) {
                            if (exception != null) {
                                createAndShowDialog(exception, "Error");
                            } else {
                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
                                        LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                                        for (Park park : result) {
                                            LatLng parkLocation = new LatLng(park.getLatitude(), park.getLongitude());
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

//                    userTable.execute(new TableJsonQueryCallback() {
//                        @Override
//                        public void onCompleted(JsonElement result, Exception exception, ServiceFilterResponse response) {
//
//                        }
//                    });

                } catch (Exception exception) {
                    createAndShowDialog(exception, "Error");
                }
                return null;
            }
        }.execute();
    }

//    private void serialise() {
//        mClient.registerSerializer(Review[].class, new ReviewArraySerializer());
//        mClient.registerDeserializer(Review[].class, new ReviewArraySerializer());
//    }

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
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



    /**
     * The ProgressFilter class renders a progress bar on the screen during the time the App is waiting for the response of a previous request.
     * the filter shows the progress bar on the beginning of the request, and hides it when the response arrived.
     */
    private class ProgressFilter implements ServiceFilter {
        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();

            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
            });

            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
                @Override
                public void onFailure(Throwable e) {
                    resultFuture.setException(e);
                }

                @Override
                public void onSuccess(ServiceFilterResponse response) {
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
                        }
                    });

                    resultFuture.set(response);
                }
            });

            return resultFuture;
        }
    }
}
