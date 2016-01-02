package com.jonathancromie.brisbanecityparks;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.UserAuthenticationCallback;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;

import java.io.IOException;
import java.net.MalformedURLException;

public class MainActivity extends AppCompatActivity  {

    static final String GOOGLE_SCOPE_TAKE2 = "audience:server:client_id:";
    static final String CLIENT_ID_WEB_APPS = "129603432412-77lpj8bs8nh8rfprninmdr359cbprh63.apps.googleusercontent.com";
    static final String GOOGLE_ID_TOKEN_SCOPE = GOOGLE_SCOPE_TAKE2 + CLIENT_ID_WEB_APPS;
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;

//    private RecyclerView recyclerView;
//    private LinearLayoutManager linearLayoutManager;

    private static final String MOBILE_SERVICE_URL = "https://brisbanecityparks.azure-mobile.net/";
    private static final String MOBILE_SERVICE_KEY = "zekjnWkJSxVYLuumxxydGozfpOSlBn97";

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    private MobileServiceClient mClient;

    FragmentManager fragmentManager;

    private TextView mAccountName;
    private TextView mAccountEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            LocalFragment fragment = new LocalFragment();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        }

        // Find our drawer view
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        // Setup drawer view
        setUpDrawerContent(navigationView);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
//        drawerToggle = setUpDrawerToggle();
        drawerToggle = setUpDrawerToggle();

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        View headerView = navigationView.getHeaderView(0);
        mAccountName = (TextView) headerView.findViewById(R.id.accountName);
        mAccountEmail = (TextView) headerView.findViewById(R.id.accountEmail);
        mAccountEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int isAvailableResult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(MainActivity.this);
                if (isAvailableResult == ConnectionResult.SUCCESS) {
                    Log.d("msg", "Result for isGooglePlayServicesAvailable: SUCCESS");
                    pickUserAccount();
                } else {
                    Log.e("error", "Google play services is not available: " + isAvailableResult);
                }

                drawerLayout.closeDrawers();
            }
        });

        try {
            mClient = new MobileServiceClient(
                    MOBILE_SERVICE_URL,
                    MOBILE_SERVICE_KEY, this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        mClient.registerSerializer(Review[].class, new ReviewArraySerializer());
        mClient.registerDeserializer(Review[].class, new ReviewArraySerializer());

//        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
//        recyclerView.setHasFixedSize(true);
//        linearLayoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(linearLayoutManager);
    }

    // Make sure this is the method with just `Bundle` as the signature
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        drawerToggle.syncState();
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.search_menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        }
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                Log.d("msg", "Account name: " + accountName);
                getTokenAndLogin(accountName);
            } else if (resultCode == RESULT_CANCELED) {
                Log.d("msg", "Activity cancelled by user");
            }
        } else if (requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR && resultCode == RESULT_OK) {
            getTokenAndLogin(mAccountName.getText().toString());
        }
    }

    private ActionBarDrawerToggle setUpDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    private void setUpDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                selectDrawerItem(item);
                return true;
            }
        });
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[] { "com.google" };
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    private void getTokenAndLogin(String accountName) {
        if (mAccountName == null) {
            pickUserAccount();
        } else {
            if (isDeviceOnline()) {
                new GetTokenAndLoginTask(this, GOOGLE_ID_TOKEN_SCOPE, accountName).execute((Void)null);
            } else {
                Toast.makeText(this, "Not Online", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager mgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mgr.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public void handleException(final Exception e) {
        // Because this call comes from the AsyncTask, we must ensure that the following
        // code instead executes on the UI thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException)e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            MainActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException)e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }

    private void updateTextView(String s) {
        mAccountName.setText(s);
    }

    public void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        Class fragmentClass = null;
//        LocalFragment localFragment;
//        WhatsHotFragment whatsHotFragment;
//        TrendingFragment trendingFragment;
//        ProfileFragment profileFragment;

        switch(menuItem.getItemId()) {
            case R.id.local_fragment:
                fragmentClass = LocalFragment.class;
//                localFragment = new LocalFragment();
//                fragmentManager.beginTransaction().replace(R.id.content_frame, localFragment).commit();
                break;
            case R.id.whats_hot_fragment:
                fragmentClass = WhatsHotFragment.class;
//                whatsHotFragment = new WhatsHotFragment();
//                fragmentManager.beginTransaction().replace(R.id.content_frame, whatsHotFragment).commit();
                break;
            case R.id.trending_fragment:
                fragmentClass = TrendingFragment.class;
//                trendingFragment = new TrendingFragment();
//                fragmentManager.beginTransaction().replace(R.id.content_frame, trendingFragment).commit();
                break;
            case R.id.logout:
                logout();
                fragmentClass = LoginFragment.class;
//                localFragment = new LocalFragment();
//                fragmentManager.beginTransaction().replace(R.id.content_frame, localFragment).commit();
                break;
            default:
                fragmentClass = LocalFragment.class;
                break;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        drawerLayout.closeDrawers();

    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences(Constants.SHAREDPREFFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
        mClient.logout();
    }

    class GetTokenAndLoginTask extends AsyncTask<Void, Void, Void> {

        MainActivity mActivity;
        String mScope;
        String mEmail;

        public GetTokenAndLoginTask(MainActivity activity, String scope, String email) {
            this.mActivity = activity;
            this.mScope = scope;
            this.mEmail = email;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                final String token = fetchIdToken();
                if (token != null) {
                    loginToMobileService(token);
                }
            } catch (IOException e) {
                Log.e("error", "Exception: " + e);
            }

            return null;
        }

        protected String fetchIdToken() throws IOException {
            try {
                return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
            } catch (UserRecoverableAuthException urae) {
                mActivity.handleException(urae);
            } catch (GoogleAuthException gae) {
                Log.e("error", "Unrecoverable exception: " + gae);
            }
            return null;
        }

        protected void loginToMobileService(final String idToken) {
            runOnUiThread(new Runnable(){

                @Override
                public void run() {
                    mActivity.updateTextView(idToken);
                    JsonObject loginBody = new JsonObject();
                    loginBody.addProperty("id_token", idToken);
                    mClient.login(MobileServiceAuthenticationProvider.Google, loginBody, new UserAuthenticationCallback() {

                        @Override
                        public void onCompleted(MobileServiceUser user, Exception error,
                                                ServiceFilterResponse response) {
                            if (error != null) {
                                Log.e("error", "Login error: " + error);
                            } else {
                                Log.d("msg", "Logged in to the mobile service as " + user.getUserId());
                            }
                        }
                    });
                }
            });
        }
    }
}
