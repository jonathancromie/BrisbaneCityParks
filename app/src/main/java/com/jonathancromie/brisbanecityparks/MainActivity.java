package com.jonathancromie.brisbanecityparks;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public List<Parks> parks = new ArrayList<Parks>();

    private ParkAdapter mAdapter;
    private MobileServiceClient mClient;
    private MobileServiceTable<Parks> parksTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

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
        mAdapter = new ParkAdapter(this, R.layout.row_list_park);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(mAdapter);

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

    public void checkItem(final Parks park) {

        // Set the item as completed and update it in the table
//        park.setComplete(true);

	    new AsyncTask<Void, Void, Void>() {

	        @Override
	        protected Void doInBackground(Void... params) {
	            try {
	                parksTable.update(park).get();
	                runOnUiThread(new Runnable() {
	                    public void run() {
//	                        if (park.isComplete()) {
//	                            mAdapter.remove(park);
//	                        }
	                        refreshItemsFromTable();
	                    }
	                });
	            } catch (Exception exception) {
	                createAndShowDialog(exception, "Error");
	            }
	            return null;
	        }
	    }.execute();

        parks.add(park);
//        if (park.isComplete()) {
//            mAdapter.remove(park);
//        }
    }

    private void refreshItemsFromTable() {
		// Get the items that weren't marked as completed and add them in the adapter
	    new AsyncTask<Void, Void, Void>() {

	        @Override
	        protected Void doInBackground(Void... params) {
	            try {
//	                final MobileServiceList<Parks> result = parksTable.where().field("complete").eq(false).execute().get();
                    final MobileServiceList<Parks> result = parksTable.select("name").execute().get();
	                runOnUiThread(new Runnable() {

	                    @Override
	                    public void run() {
	                        mAdapter.clear();

	                        for (Parks park : result) {
	                            mAdapter.add(park);
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
        mAdapter.clear();
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
}
