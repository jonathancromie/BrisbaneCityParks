package com.jonathancromie.brisbanecityparks;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.maps.android.SphericalUtil;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponseCallback;
import com.microsoft.windowsazure.mobileservices.UserAuthenticationCallback;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.ProgressBar;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutionException;

import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOrder;

public class LocalFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String MOBILE_SERVICE_URL = "https://brisbanecityparks.azure-mobile.net/";
    private static final String MOBILE_SERICE_KEY = "zekjnWkJSxVYLuumxxydGozfpOSlBn97";

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

    public boolean bAuthenticating = false;
    public final Object mAuthenticationLock = new Object();

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
        parks = new ArrayList<Park>();
        parkAdapter = new ParkAdapter(getActivity().getLayoutInflater(), parks);
        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(parkAdapter);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        // Initialize the progress bar
        mProgressBar.setVisibility(ProgressBar.GONE);

        try {
            mClient = new MobileServiceClient(
                    MOBILE_SERVICE_URL,
                    MOBILE_SERICE_KEY, getContext())
                    .withFilter(new ProgressFilter())
                    .withFilter(new RefreshTokenCacheFilter());

//            parkTable = mClient.getTable(Park.class);
        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("Error creating the Mobile Service. " +
                    "Verify the URL"), "Error");
        }

        mClient.registerSerializer(Review[].class, new ReviewArraySerializer());
        mClient.registerDeserializer(Review[].class, new ReviewArraySerializer());
        mClient.withFilter(new ServiceFilter() {
            @Override
            public ListenableFuture<ServiceFilterResponse> handleRequest(final ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        request.addHeader("My-Header", "value");
                    }
                });

                SettableFuture<ServiceFilterResponse> result = SettableFuture.create();
                try {
                    ServiceFilterResponse response = nextServiceFilterCallback.onNext(request).get();
                    result.set(response);
                } catch (Exception exc) {
                    result.setException(exc);
                }
                return null;
            }
        });

        // Load the items from the Mobile Service
//        refreshItemsFromTable();
        authenticate(false);

        return rootView;
    }

    /**
     * Authenticates with the desired login provider. Also caches the token.
     *
     * If a local token cache is detected, the token cache is used instead of an actual
     * login unless bRefresh is set to true forcing a refresh.
     *
     * @param bRefreshCache
     *            Indicates whether to force a token refresh.
     */
    private void authenticate(boolean bRefreshCache) {

        bAuthenticating = true;

        if (bRefreshCache || !loadUserTokenCache(mClient))
        {
            // New login using the provider and update the token cache.
            mClient.login(MobileServiceAuthenticationProvider.Google,
                    new UserAuthenticationCallback() {
                        @Override
                        public void onCompleted(MobileServiceUser user,
                                                Exception exception, ServiceFilterResponse response) {

                            synchronized (mAuthenticationLock) {
                                if (exception == null) {
                                    cacheUserToken(mClient.getCurrentUser());
                                    createTable();
                                } else {
                                    createAndShowDialog(exception.getMessage(), "Login Error");
                                }
                                bAuthenticating = false;
                                mAuthenticationLock.notifyAll();
                            }
                        }
                    });
        }
        else
        {
            // Other threads may be blocked waiting to be notified when
            // authentication is complete.
            synchronized(mAuthenticationLock)
            {
                bAuthenticating = false;
                mAuthenticationLock.notifyAll();
            }
            createTable();
        }
    }

    private void createTable() {

        // Get the Mobile Service Table instance to use
        parkTable = mClient.getTable("park", Park.class);

//        mTextNewToDo = (EditText) findViewById(R.id.textNewToDo);

        // Create an adapter to bind the items with the view


        // Load the items from the Mobile Service
        refreshItemsFromTable();
    }

    private void cacheUserToken(MobileServiceUser user)
    {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHAREDPREFFILE, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(Constants.USERIDPREF, user.getUserId());
        editor.putString(Constants.TOKENPREF, user.getAuthenticationToken());
        editor.commit();
    }

    private boolean loadUserTokenCache(MobileServiceClient client)
    {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHAREDPREFFILE, Context.MODE_PRIVATE);
        String userId = prefs.getString(Constants.USERIDPREF, "undefined");
        if (userId == "undefined")
            return false;
        String token = prefs.getString(Constants.TOKENPREF, "undefined");
        if (token == "undefined")
            return false;

        MobileServiceUser user = new MobileServiceUser(userId);
        user.setAuthenticationToken(token);
        client.setCurrentUser(user);

        return true;
    }

    /**
     * Detects if authentication is in progress and waits for it to complete.
     * Returns true if authentication was detected as in progress. False otherwise.
     */
    public boolean detectAndWaitForAuthentication()
    {
        boolean detected = false;
        synchronized(mAuthenticationLock)
        {
            do
            {
                if (bAuthenticating == true)
                    detected = true;
                try
                {
                    mAuthenticationLock.wait(1000);
                }
                catch(InterruptedException e)
                {}
            }
            while(bAuthenticating == true);
        }
        if (bAuthenticating == true)
            return true;

        return detected;
    }

    /**
     * Waits for authentication to complete then adds or updates the token
     * in the X-ZUMO-AUTH request header.
     *
     * @param request
     *            The request that receives the updated token.
     */
    private void waitAndUpdateRequestToken(ServiceFilterRequest request)
    {
        MobileServiceUser user = null;
        if (detectAndWaitForAuthentication())
        {
            user = mClient.getCurrentUser();
            if (user != null)
            {
                request.removeHeader("X-ZUMO-AUTH");
                request.addHeader("X-ZUMO-AUTH", user.getAuthenticationToken());
            }
        }
    }

    private void refreshItemsFromTable() {
        // Get the items that weren't marked as completed and add them in the adapter
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
//                    final MobileServiceList<Park> result = parkTable.execute().get();
                    final MobileServiceList<Park> result = parkTable.execute().get();
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
                            LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                            int counter = 0;
                            for (final Park park : result) {

                                LatLng parkLocation = new LatLng(park.latitude, park.longitude);
                                park.setDistance(SphericalUtil.computeDistanceBetween(userLocation, parkLocation));
                                parks.add(park);
                                counter++;

                            }

                            for (Park park : parks) {
                                parkAdapter.addPark(park);
                                parkAdapter.notifyItemInserted(parkAdapter.parks.size() - 1);
                            }


                        }
                    });

//                    parkTable.execute(new TableQueryCallback<Park>() {
//                        @Override
//                        public void onCompleted(final List<Park> result, int count, Exception exception, ServiceFilterResponse response) {
//                            if (exception != null) {
//                                createAndShowDialog(exception, "Error");
//                            }
//                            else {
//                                getActivity().runOnUiThread(new Runnable() {
//
//                                    @Override
//                                    public void run() {
//                                        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
//                                        LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
//
//                                        int counter = 0;
//                                        for (final Park park : result) {
//
//                                            LatLng parkLocation = new LatLng(park.latitude, park.longitude);
//                                            park.setDistance(SphericalUtil.computeDistanceBetween(userLocation, parkLocation));
//                                            parks.add(park);
//                                            counter++;
//
//                                        }
//
//                                        for (Park park : parks) {
//                                            parkAdapter.addPark(park);
//                                            parkAdapter.notifyItemInserted(parkAdapter.parks.size() - 1);
//                                        }
//
//
//                                    }
//                                });
//                            }
//                        }
//                    });

                } catch (Exception exception) {
	                createAndShowDialog(exception, "Error");
                }
                return null;
            }
        }.execute();
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
     * The RefreshTokenCacheFilter class filters responses for HTTP status code 401.
     * When 401 is encountered, the filter calls the authenticate method on the
     * UI thread. Out going requests and retries are blocked during authentication.
     * Once authentication is complete, the token cache is updated and
     * any blocked request will receive the X-ZUMO-AUTH header added or updated to
     * that request.
     */
    private class RefreshTokenCacheFilter implements ServiceFilter {

        AtomicBoolean mAtomicAuthenticatingFlag = new AtomicBoolean();

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(
                final ServiceFilterRequest request,
                final NextServiceFilterCallback nextServiceFilterCallback
        )
        {
            // In this example, if authentication is already in progress we block the request
            // until authentication is complete to avoid unnecessary authentications as
            // a result of HTTP status code 401.
            // If authentication was detected, add the token to the request.
            waitAndUpdateRequestToken(request);

            // Send the request down the filter chain
            // retrying up to 5 times on 401 response codes.
            ListenableFuture<ServiceFilterResponse> future = null;
            ServiceFilterResponse response = null;
            int responseCode = 401;
            for (int i = 0; (i < 5 ) && (responseCode == 401); i++)
            {
                future = nextServiceFilterCallback.onNext(request);
                try {
                    response = future.get();
                    responseCode = response.getStatus().getStatusCode();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    if (e.getCause().getClass() == MobileServiceException.class)
                    {
                        MobileServiceException mEx = (MobileServiceException) e.getCause();
                        responseCode = mEx.getResponse().getStatus().getStatusCode();
                        if (responseCode == 401)
                        {
                            // Two simultaneous requests from independent threads could get HTTP status 401.
                            // Protecting against that right here so multiple authentication requests are
                            // not setup to run on the UI thread.
                            // We only want to authenticate once. Requests should just wait and retry
                            // with the new token.
                            if (mAtomicAuthenticatingFlag.compareAndSet(false, true))
                            {
                                // Authenticate on UI thread
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Force a token refresh during authentication.
                                        authenticate(true);
                                    }
                                });
                            }

                            // Wait for authentication to complete then update the token in the request.
                            waitAndUpdateRequestToken(request);
                            mAtomicAuthenticatingFlag.set(false);
                        }
                    }
                }
            }
            return future;
        }
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


