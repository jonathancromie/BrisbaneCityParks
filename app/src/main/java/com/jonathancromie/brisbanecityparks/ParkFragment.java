package com.jonathancromie.brisbanecityparks;


import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;

import java.net.MalformedURLException;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ParkFragment extends Fragment {

    private static final String MOBILE_SERVICE_URL = "https://brisbanecityparks.azure-mobile.net/";
    private static final String MOBILE_SERICE_KEY = "zekjnWkJSxVYLuumxxydGozfpOSlBn97";

    private List<Review> reviews;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
//    private ReviewAdapter reviewAdapter;

    private MobileServiceClient mClient;
    private ProgressBar mProgressBar;
    private MobileServiceTable<Park> parkTable;

    private TextView textView;

    public ParkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_park, container, false);

//        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);
//        recyclerView.setHasFixedSize(true);
//        linearLayoutManager = new LinearLayoutManager(getContext());
//        recyclerView.setLayoutManager(linearLayoutManager);

//        reviews = new ArrayList<Review>();
//        reviewAdapter = new ReviewAdapter(getActivity().getLayoutInflater(), reviews);
//        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
//        recyclerView.setAdapter(reviewAdapter);

        textView = (TextView) rootView.findViewById(R.id.textView);

        try {
            mClient = new MobileServiceClient(
                    MOBILE_SERVICE_URL,
                    MOBILE_SERICE_KEY, getContext());

//            parksTable = mClient.getTable(Park.class);
        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("Error creating the Mobile Service. " +
                    "Verify the URL"), "Error");
        }

        // Get the Mobile Service Table instance to use
        parkTable = mClient.getTable(Park.class);

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
                    mClient.getTable(Park.class).execute(new TableQueryCallback<Park>() {
                        @Override
                        public void onCompleted(List<Park> parks, int totalCount, Exception exception, ServiceFilterResponse response) {
                            if (exception != null) {
                                textView.setText("Error: " + exception.toString());
                            } else {
                                StringBuilder sb = new StringBuilder();
//                                sb.append("All parks: size=" + parks.size() + "\n");
                                for (Park park : parks) {
                                    String name = park.getName();
                                    double distance = park.getDistance();
                                    sb.append("  " + name + " - " + distance + ": ");
                                    Review[] reviews = park.getReviews();
                                    for (Review review : reviews) {
                                        for (int i = 0; i < review.getStars(); i++) {
                                            sb.append('*');
                                        }
                                        sb.append(' ');
                                    }
                                    sb.append('\n');
                                }
                                textView.setText(sb.toString());
                            }
                        }
                    });
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {



                        }
                    });
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
