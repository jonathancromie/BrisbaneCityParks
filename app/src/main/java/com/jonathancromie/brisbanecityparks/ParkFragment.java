package com.jonathancromie.brisbanecityparks;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponseCallback;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A simple {@link Fragment} subclass.
 */
public class ParkFragment extends Fragment {

    private static final String MOBILE_SERVICE_URL = "https://brisbanecityparks.azure-mobile.net/";
    private static final String MOBILE_SERICE_KEY = "zekjnWkJSxVYLuumxxydGozfpOSlBn97";

    private List<Review> reviews;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ReviewAdapter reviewAdapter;

    private MobileServiceClient mClient;
    private ProgressBar mProgressBar;
    private MobileServiceTable<Park> parkTable;
    private MobileServiceTable<Review> reviewTable;

//    private int id;

    private boolean mIsLargeLayout = false;

    public ParkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_park, container, false);

        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        reviews = new ArrayList<Review>();
        reviewAdapter = new ReviewAdapter(getActivity().getLayoutInflater(), reviews);
        recyclerView.setAdapter(reviewAdapter);

        try {
            mClient = new MobileServiceClient(
                    MOBILE_SERVICE_URL,
                    MOBILE_SERICE_KEY, getContext());

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("Error creating the Mobile Service. " +
                    "Verify the URL"), "Error");
        }


        // Get the Mobile Service Table instance to use
        parkTable = mClient.getTable("park", Park.class);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        // Load the items from the Mobile Service
        refreshItemsFromTable();

        return rootView;
    }

    public void showDialog() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        AddReviewFragment fragment = new AddReviewFragment();

        if (mIsLargeLayout) {
            // The device is using a large layout, so show the fragment as a dialog
            fragment.show(fragmentManager, "dialog");
        } else {
            fragment.setTargetFragment(this, 1); // how to return from dialogfragmet

            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.replace(R.id.content_frame, fragment).commit();



        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {

            String comment = data.getStringExtra("comment");
            int stars = (int) data.getFloatExtra("stars", 0);

            addReview(comment, stars, new Date());
            // show park fragment again
//            refreshItemsFromTable();
        }
    }

    /**
     * Lookup specific item from table and UI
     */
    public void addReview(final String comment, final int stars, final Date date) {
        final String ID = getArguments().getString("parkId");
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    final Park park = parkTable.lookUp(ID).get();
                    Review review = new Review(comment, stars, date);
                    park.setReviews(new Review[] {review});

                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            parkTable.update(park, new TableOperationCallback<Park>() {
                                @Override
                                public void onCompleted(Park entity, Exception exception, ServiceFilterResponse response) {
                                    if (exception != null) {
                                        createAndShowDialog(exception, "Error");
                                    } else {
                                        createAndShowDialog("Your review has been added", "Success");
                                    }
                                }
                            });
                        }
                    });
                } catch (Exception exception) {
                    createAndShowDialog(exception, "Error");
                }
                return null;
            }
        }.execute();
    }

    private void refreshItemsFromTable() {
        final String ID = getArguments().getString("parkId");
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                final Park result;
                try {
                    result = parkTable.lookUp(ID).get();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            getActivity().setTitle(result.getName());
                            Review[] reviews = result.getReviews();
                            for (Review review : reviews) {
                                reviewAdapter.addReview(review);
                                reviewAdapter.notifyItemInserted(reviewAdapter.reviews.size() - 1);
                            }
                        }
                    });
                }

                catch (Exception e) {
                    e.printStackTrace();
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

    /**
     * The ProgressFilter class renders a progress bar on the screen during the time the App is waiting for the response of a previous request.
     * the filter shows the progress bar on the beginning of the request, and hides it when the response arrived.
     */
//    private class ProgressFilter implements ServiceFilter {
//        @Override
//        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {
//
//            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();
//
//            getActivity().runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
//                }
//            });
//
//            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);
//
//            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
//                @Override
//                public void onFailure(Throwable e) {
//                    resultFuture.setException(e);
//                }
//
//                @Override
//                public void onSuccess(ServiceFilterResponse response) {
//                    getActivity().runOnUiThread(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
//                        }
//                    });
//
//                    resultFuture.set(response);
//                }
//            });
//
//            return resultFuture;
//        }
//    }
}
