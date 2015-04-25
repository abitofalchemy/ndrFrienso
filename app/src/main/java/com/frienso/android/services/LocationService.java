package com.frienso.android.services;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.frienso.android.application.MainActivity;
import com.frienso.android.helper.FriendOutgoing;
import com.frienso.android.helper.FriendsHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    public static final String LOCATION_DATA = "CURRENT_LOCATION_DATA";
    public static final String LOCATION_PREFERENCE_FILE = "com.frienso.android.LOCATION_PREFERENCE_FILE";
    public static final String LOCATION_PREFERENCE_FILE_TIMESTAMP = "TIMESTAMP";
    public static final String LOCATION_PREFERENCE_FILE_LATITUDE = "LATITUDE";
    public static final String LOCATION_PREFERENCE_FILE_LONGITUDE = "LONGITUDE";
    public static final String LOCATION_PREFERENCE_FILE_ACCURACY = "ACCURACY";
    private static final String LOCATION_PREFERENCE_FILE_PROVIDER = "PROVIDER";
    private static GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static String LOG_TAG = "LocationService";
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private boolean sendRemote = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        buildGoogleApiClient();
        Log.d(LOG_TAG, "Starting Service");
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    protected synchronized void buildGoogleApiClient() {
        //avoid connecting multiple times to the google api
        if (mGoogleApiClient != null)
            return ;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.i(LOG_TAG,"START location updates called");
    }



    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
       LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void saveLocationToSharePrefs(Location location) {
        Log.d(LOG_TAG,"saving location" + location.getLatitude());

        Context context = this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LOCATION_PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(LOCATION_PREFERENCE_FILE_TIMESTAMP, location.getTime());
        editor.putFloat(LOCATION_PREFERENCE_FILE_LATITUDE, (float) location.getLatitude());
        editor.putFloat(LOCATION_PREFERENCE_FILE_LONGITUDE, (float) location.getLongitude());
        editor.putFloat(LOCATION_PREFERENCE_FILE_ACCURACY, location.getAccuracy());
        editor.putString(LOCATION_PREFERENCE_FILE_PROVIDER, location.getProvider());
        editor.commit();

        Intent intent = new Intent();
        intent.setAction(MainActivity.INTENT_ACTION_WHEN_LOCATION_UPDATED);
        intent.putExtra(MainActivity.BROADCAST_MESSAGE_TYPE, MainActivity.BROADCAST_MESSAGE_LOCATION_RECEIVED);
        intent.putExtra(LocationService.LOCATION_DATA,location);

        sendBroadcast(intent);

    }

    private void saveLocationToParse(Location location) {

        //if there is no user logged in or remote it false then terminate
        if(ParseUser.getCurrentUser() != null && sendRemote == true) {
            ParseObject po = new ParseObject("LocationsVisited");
            //create a geo object
            ParseGeoPoint pgp = new ParseGeoPoint(location.getLatitude(),location.getLongitude());
            po.put("location", pgp);
            po.put("user", ParseUser.getCurrentUser());
            po.put("provider", location.getProvider());
            po.put("accuracy",Math.round(location.getAccuracy()));

            //set ACL only to the friends

            ParseACL pacl = new ParseACL(ParseUser.getCurrentUser());
            pacl.setPublicReadAccess(false);
            pacl.setPublicWriteAccess(false);
            pacl.setWriteAccess(ParseUser.getCurrentUser(),true);
            for (FriendOutgoing fo : FriendsHelper.sFriendOutgoing) {
                pacl.setReadAccess(fo.getParseUser(),true);
            }
            po.setACL(pacl);
            po.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        Log.i(LOG_TAG, "Saved successfully");
                    } else {
                        Log.i(LOG_TAG,"Saved unsuccessfully" + e.toString());
                    }
                }
            });

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG_TAG,"Connection to location Service Successful");

        startLocationUpdates();
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (lastLocation != null) {
            saveLocationToSharePrefs(lastLocation);
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOG_TAG, "Google Api connection suspended");


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, connectionResult.toString());

    }

    @Override
    public void onLocationChanged(Location location) {
        //save it to the local file
        //if remote tracking is set, send to parse.
        saveLocationToSharePrefs(location);
        saveLocationToParse(location);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "On destroy called");
        //stopLocationUpdates();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            Log.d(LOG_TAG, "mGoogleApiClientDisconnected");
            mGoogleApiClient = null;
        }
        stopSelf();

    }
}
