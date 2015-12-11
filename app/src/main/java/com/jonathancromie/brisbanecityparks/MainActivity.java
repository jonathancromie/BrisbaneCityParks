package com.jonathancromie.brisbanecityparks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.UserAuthenticationCallback;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import layout.LoginFragment;

public class MainActivity extends AppCompatActivity {

//    private RecyclerView recyclerView;
//    private LinearLayoutManager linearLayoutManager;

    private static final String MOBILE_SERVICE_URL = "https://brisbanecityparks.azure-mobile.net/";
    private static final String MOBILE_SERVICE_KEY = "zekjnWkJSxVYLuumxxydGozfpOSlBn97";

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    private MobileServiceClient mClient;
//
//    public boolean bAuthenticating = false;
//    public final Object mAuthenticationLock = new Object();
//
//    private Button facebookLogin;
//    private Button googleLogin;

//    private MobileServiceAuthenticationProvider provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            LoginFragment fragment = new LoginFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            setTitle("Login");
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

        try {
            mClient = new MobileServiceClient(
                    MOBILE_SERVICE_URL,
                    MOBILE_SERVICE_KEY, this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


//
//        facebookLogin = (Button) findViewById(R.id.facebook);
//        facebookLogin.setOnClickListener(this);
//
//        googleLogin = (Button) findViewById(R.id.google);
//        googleLogin.setOnClickListener(this);

    }

//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.facebook:
//                provider = MobileServiceAuthenticationProvider.Facebook;
//                break;
//            case R.id.google:
//                provider = MobileServiceAuthenticationProvider.Google;
//                break;
//            default:
//                break;
//        }
//
//        authenticate(false);
//    }

//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

//        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
//        recyclerView.setHasFixedSize(true);
//        linearLayoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(linearLayoutManager);

//    /**
//     * Authenticates with the desired login provider. Also caches the token.
//     *
//     * If a local token cache is detected, the token cache is used instead of an actual
//     * login unless bRefresh is set to true forcing a refresh.
//     *
//     * @param bRefreshCache
//     *            Indicates whether to force a token refresh.
//     */
//    private void authenticate(boolean bRefreshCache) {
//
//        bAuthenticating = true;
//
//        if (bRefreshCache || !loadUserTokenCache(mClient))
//        {
//            // New login using the provider and update the token cache.
//            mClient.login(provider,
//                    new UserAuthenticationCallback() {
//                        @Override
//                        public void onCompleted(MobileServiceUser user,
//                                                Exception exception, ServiceFilterResponse response) {
//
//                            synchronized (mAuthenticationLock) {
//                                if (exception == null) {
//                                    cacheUserToken(mClient.getCurrentUser());
//                                    createFragment();
//                                } else {
//                                    createAndShowDialog(exception.getMessage(), "Login Error");
//                                }
//                                bAuthenticating = false;
//                                mAuthenticationLock.notifyAll();
//                            }
//                        }
//                    });
//        }
//        else
//        {
//            // Other threads may be blocked waiting to be notified when
//            // authentication is complete.
//            synchronized(mAuthenticationLock)
//            {
//                bAuthenticating = false;
//                mAuthenticationLock.notifyAll();
//            }
//            createFragment();
//        }
//    }
//
//    private void createFragment() {
//        LocalFragment fragment = new LocalFragment();
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.beginTransaction().add(R.id.content_frame, fragment).commit();
//        setTitle(R.string.local);
//    }
//
//    private void cacheUserToken(MobileServiceUser user)
//    {
//        SharedPreferences prefs = getSharedPreferences(Constants.SHAREDPREFFILE, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(Constants.USERIDPREF, user.getUserId());
//        editor.putString(Constants.TOKENPREF, user.getAuthenticationToken());
//        editor.commit();
//    }
//
//    private boolean loadUserTokenCache(MobileServiceClient client)
//    {
//        SharedPreferences prefs = getSharedPreferences(Constants.SHAREDPREFFILE, Context.MODE_PRIVATE);
//        String userId = prefs.getString(Constants.USERIDPREF, "undefined");
//        if (userId == "undefined")
//            return false;
//        String token = prefs.getString(Constants.TOKENPREF, "undefined");
//        if (token == "undefined")
//            return false;
//
//        MobileServiceUser user = new MobileServiceUser(userId);
//        user.setAuthenticationToken(token);
//        client.setCurrentUser(user);
//
//        return true;
//    }
//
//    /**
//     * Detects if authentication is in progress and waits for it to complete.
//     * Returns true if authentication was detected as in progress. False otherwise.
//     */
//    public boolean detectAndWaitForAuthentication()
//    {
//        boolean detected = false;
//        synchronized(mAuthenticationLock)
//        {
//            do
//            {
//                if (bAuthenticating == true)
//                    detected = true;
//                try
//                {
//                    mAuthenticationLock.wait(1000);
//                }
//                catch(InterruptedException e)
//                {}
//            }
//            while(bAuthenticating == true);
//        }
//        if (bAuthenticating == true)
//            return true;
//
//        return detected;
//    }
//
//    /**
//     * Waits for authentication to complete then adds or updates the token
//     * in the X-ZUMO-AUTH request header.
//     *
//     * @param request
//     *            The request that receives the updated token.
//     */
//    private void waitAndUpdateRequestToken(ServiceFilterRequest request)
//    {
//        MobileServiceUser user = null;
//        if (detectAndWaitForAuthentication())
//        {
//            user = mClient.getCurrentUser();
//            if (user != null)
//            {
//                request.removeHeader("X-ZUMO-AUTH");
//                request.addHeader("X-ZUMO-AUTH", user.getAuthenticationToken());
//            }
//        }
//    }
//
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

    public void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        Class fragmentClass = null;
        switch(menuItem.getItemId()) {
            case R.id.local_fragment:
                fragmentClass = LocalFragment.class;
                break;
            case R.id.whats_hot_fragment:
                fragmentClass = WhatsHotFragment.class;
                break;
            case R.id.trending_fragment:
//                fragmentClass = TrendingFragment.class;
                break;
            case R.id.logout:
                mClient.logout();
                fragmentClass = LoginFragment.class;
            default:
                fragmentClass = LoginFragment.class;
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

//    /**
//     * The RefreshTokenCacheFilter class filters responses for HTTP status code 401.
//     * When 401 is encountered, the filter calls the authenticate method on the
//     * UI thread. Out going requests and retries are blocked during authentication.
//     * Once authentication is complete, the token cache is updated and
//     * any blocked request will receive the X-ZUMO-AUTH header added or updated to
//     * that request.
//     */
//    private class RefreshTokenCacheFilter implements ServiceFilter {
//
//        AtomicBoolean mAtomicAuthenticatingFlag = new AtomicBoolean();
//
//        @Override
//        public ListenableFuture<ServiceFilterResponse> handleRequest(
//                final ServiceFilterRequest request,
//                final NextServiceFilterCallback nextServiceFilterCallback
//        )
//        {
//            // In this example, if authentication is already in progress we block the request
//            // until authentication is complete to avoid unnecessary authentications as
//            // a result of HTTP status code 401.
//            // If authentication was detected, add the token to the request.
//            waitAndUpdateRequestToken(request);
//
//            // Send the request down the filter chain
//            // retrying up to 5 times on 401 response codes.
//            ListenableFuture<ServiceFilterResponse> future = null;
//            ServiceFilterResponse response = null;
//            int responseCode = 401;
//            for (int i = 0; (i < 5 ) && (responseCode == 401); i++)
//            {
//                future = nextServiceFilterCallback.onNext(request);
//                try {
//                    response = future.get();
//                    responseCode = response.getStatus().getStatusCode();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    if (e.getCause().getClass() == MobileServiceException.class)
//                    {
//                        MobileServiceException mEx = (MobileServiceException) e.getCause();
//                        responseCode = mEx.getResponse().getStatus().getStatusCode();
//                        if (responseCode == 401)
//                        {
//                            // Two simultaneous requests from independent threads could get HTTP status 401.
//                            // Protecting against that right here so multiple authentication requests are
//                            // not setup to run on the UI thread.
//                            // We only want to authenticate once. Requests should just wait and retry
//                            // with the new token.
//                            if (mAtomicAuthenticatingFlag.compareAndSet(false, true))
//                            {
//                                // Authenticate on UI thread
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        // Force a token refresh during authentication.
//                                        authenticate(true);
//                                    }
//                                });
//                            }
//
//                            // Wait for authentication to complete then update the token in the request.
//                            waitAndUpdateRequestToken(request);
//                            mAtomicAuthenticatingFlag.set(false);
//                        }
//                    }
//                }
//            }
//            return future;
//        }
//    }
}
