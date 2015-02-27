package com.frienso.helper;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.frienso.android.application.R;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by udayan kumar on 1/15/15.
 */
public class LocationHelper implements LocationListener {
    private static final String LOG_TAG = "LocationHelper";
    Context mContext;

    private long minTimeBetweenLocUpdates;
    private float minDistBetweenLocUpdates;
    LocationManager mLocationManager;
    String mLocationProvider;
    Location mCurrentBestLocation;

    private static final int TWO_MINUTES = 1000 * 60 * 2;


    public LocationHelper(Context context, String locationProvider){
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        minTimeBetweenLocUpdates = (long) mContext.getResources().getInteger(R.integer.minTimeBetweenLocUpdatesInMillis);
        minDistBetweenLocUpdates = mContext.getResources().getInteger(R.integer.minDistBetweenLocUpdatesInM);
        mLocationProvider = locationProvider;



    }

    public boolean isProviderEnabled() {
        //check if GPS is available
        if ( !mLocationManager.isProviderEnabled(mLocationProvider ) ) {
            Toast.makeText(mContext, mLocationProvider + " is disabled!", Toast.LENGTH_LONG).show();
            return false;
        }
        else {
            Toast.makeText(mContext, mLocationProvider + " is enabled!", Toast.LENGTH_LONG).show();
            return true;
        }

    }

    public Location registerForLocationUpdates() {
        mLocationManager.requestLocationUpdates(mLocationProvider, minTimeBetweenLocUpdates, minDistBetweenLocUpdates, this);
        mCurrentBestLocation = mLocationManager.getLastKnownLocation(mLocationProvider);

        //return the last known location for faster response to the user
        return mCurrentBestLocation;
    }

    public void unregisterForLocationUpdates() {
        mLocationManager.removeUpdates(this);

    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }


    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onLocationChanged(Location location) {

        if(isBetterLocation(location,mCurrentBestLocation)) {
            mCurrentBestLocation = location;
        }
        Log.i(LOG_TAG, " Location is " + location.getLatitude() + " " + location.getLongitude() + " " + location.getAccuracy());


        //if there is no user logged in, then terminate
        if(ParseUser.getCurrentUser() != null) {
            ParseObject po = new ParseObject("LocationsVisited");
            //create a geo object
            ParseGeoPoint pgp = new ParseGeoPoint(location.getLatitude(),location.getLongitude());
            po.put("location", pgp);
            po.put("user", ParseUser.getCurrentUser());
            po.put("provider", mLocationProvider);
            po.put("accuracy",location.getAccuracy());

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
                        Log.i(LOG_TAG,"Saved successfully");
                    } else {
                        Log.i(LOG_TAG,"Saved unsuccessfully" + e.toString());
                    }
                }
            });

        }

        this.unregisterForLocationUpdates();


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
