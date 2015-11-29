package com.jonathancromie.brisbanecityparks;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOrder;

import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public List<Parks> parks = new ArrayList<>();

    private ParkAdapter parkAdapter;
    private MobileServiceClient mClient;
    private MobileServiceTable<Parks> parksTable;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buildGoogleApiClient();
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        parkAdapter = new ParkAdapter(parks);
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

        // Create an adapter to bind the items with the view
//        mAdapter = new ParkAdapter(this, R.layout.row_list_park);
//        ListView listView = (ListView) findViewById(R.id.listView);
//        listView.setAdapter(mAdapter);

        // Load the items from the Mobile Service
        refreshItemsFromTable();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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

	                        for (Parks park : result) {
                                parkAdapter.parks.add(park);
                                parkAdapter.notifyItemInserted(parkAdapter.parks.size()-1);
	                        }
	                    }
	                });
    	            } catch (Exception exception) {
	                createAndShowDialog(exception, "Error");
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

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (mLastLocation != null) {
//            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
//            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
//        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
