package com.frienso.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.frienso.android.application.Splash;
import com.frienso.helper.ActiveIncomingEvent;
import com.frienso.helper.EventHelper;
import com.frienso.utils.DateTime;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class IncomingEventInfoRetrieval extends Service {
    private final static int sSleepInMillis = 1 * 60 * 1000;
    private static final String LOCATION_DATA_TABLE = "LocationsVisited";
    private static final String LOG_TAG = "IncomingEventInfoRetrieval" ;
    private static final int REFETCH_EVENT_DATA = 1 ;
    private static final int REFETCH_EVENT_DATA_STOPPED = 2;
    //This value can be set by the external classes to stop this service.
    public static boolean stopService = false;
    private Handler mHandler;
    private Context mContext;

    public IncomingEventInfoRetrieval() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        //Handler part of the messaging.
        mHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case(REFETCH_EVENT_DATA):
                        Log.i(LOG_TAG,"Ready for the next round");
                    break;
                    case(REFETCH_EVENT_DATA_STOPPED):
                        Log.i(LOG_TAG, "No more fetching of the data. Stopping Service");
                        stopSelf();
                }
            }
        };

    }

    private boolean continueChecking () {
        boolean continueCheck = true;

        //add more conditions below
        if (EventHelper.sActiveIncomingEvents.size() == 0 ) {
            continueCheck = false;
        }

        if(stopService == true){
            continueCheck = false;
        }

        return continueCheck;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //this is needed if the service is restarted while in the background.
        Parse.initialize(mContext, Splash.PARSE_APPLICATION_ID, Splash.PARSE_CLIENT_KEY);
        //if there is no user logged in, then terminate
        if(ParseUser.getCurrentUser() == null) {
            stopSelf();
        }
        new Thread(mEventRefresh).start();
       return super.onStartCommand(intent, flags, startId);
    }

    /* This is the method that does all the heavy lifting
     */
    private void fetchData () {

        //update each user's active event separately .. finally we should do it all in one query
        for (ActiveIncomingEvent ae : EventHelper.sActiveIncomingEvents) {
            List<ParseObject> pofs;
            ParseQuery<ParseObject> pq = ParseQuery.getQuery(LOCATION_DATA_TABLE);
            long timeStamp;
            if(ae.mLocationArray.size()==0){
                timeStamp = ae.createdAtInMillis - 1 ; //to get greater than equal to
            } else {
                timeStamp = (ae.mLocationArray.get(ae.mLocationArray.size() - 1)).timeStampInMillis;
            }

            //convert timeStamp into ISO format
            String UTCtimeStamp = DateTime.getISO8601StringForTimeStampInMillis(timeStamp);

            //TODO: uncomment the 3 lines below. Commented because data in parse is not correct
            //pq.whereEqualTo("user",ae.mUser);
            //pq.whereGreaterThan("createdAt",UTCtimeStamp);
            //pq.orderByAscending("createdAt");

            //TODO:remove the line below. This is to limit the output
            pq.setLimit(50);

            //query the data from Parse
            try {
                pofs =  pq.find();
            } catch (ParseException e) {
                e.printStackTrace();
                pofs = null;
            }

            //iterate through the result one by one.
            for (ParseObject pof : pofs ){
                ParseGeoPoint pgp = (ParseGeoPoint) pof.get("location");
                ae.addLocationEvent (pgp.getLatitude(),pgp.getLongitude(),(int)pof.get("accuracy"),
                        (pof.getCreatedAt()).getTime());

                Log.i(LOG_TAG, "Location Found - for : " + ae.mUser.getUsername() + " " +
                        pgp.getLatitude() + " " + pgp.getLongitude() + " " + (int) pof.get("accuracy")
                        + " " + (pof.getCreatedAt()).getTime());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    Runnable mEventRefresh = new Runnable() {

        @Override
        public void run() {
            Log.i(LOG_TAG, "Running the Event Service");
            fetchData();
            if(continueChecking()) {
                //now update the events since we have the friend information
                mHandler.postDelayed(mEventRefresh, sSleepInMillis);
                mHandler.sendEmptyMessage(REFETCH_EVENT_DATA);
            } else {
                mHandler.sendEmptyMessage(REFETCH_EVENT_DATA_STOPPED);

            }

        }
    };

    }
