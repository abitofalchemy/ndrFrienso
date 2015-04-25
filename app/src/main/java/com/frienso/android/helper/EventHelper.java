package com.frienso.android.helper;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.frienso.android.services.IncomingEventInfoRetrieval;
import com.frienso.android.utils.DateTime;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Udayan Kumar on 11/22/14.
 */
public class EventHelper {

    public static ArrayList<ActiveIncomingEvent> sActiveIncomingEvents;
    private final static String EVENT_TABLE = "UserEvent";
    private final static String PARSE_EVENT_TABLE_ACTIVE_EVENT = "eventActive";
    private final static String PARSE_EVENT_TABLE_FRIENSO_USER = "friensoUser";
    private final static String PARSE_EVENT_TABLE_EVENT_TYPE = "eventType";
    private static final String PARSE_EVENT_TABLE_EVENT_TYPE_WATCHME = "watchMe" ;
    private static Object SyncObject = new Object();
    private final static String LOG_TAG = "EVENT_HELPER";
    private static Context sContext;
    private static long sLastUpdateTimeInMillis = 0;
    private final static long MIN_TIME_BETWEEN_FRIEND_RELOAD_MILLIS = 1 * 60 * 1000;

    private EventHelper() {

    }

    public static void setContext(Context context) {
        sContext = context;
    }






    public static void refreshActiveEvents () {
        //get a list of incoming friends
        if (FriendsHelper.sFriendIncoming.size() == 0) {
            // no incoming friend
            return;
        }
        if((System.currentTimeMillis() - sLastUpdateTimeInMillis) < MIN_TIME_BETWEEN_FRIEND_RELOAD_MILLIS)
            return;
        sLastUpdateTimeInMillis = System.currentTimeMillis();
        loadActiveEvent();
    }

    private static void loadActiveEvent() {
        synchronized (SyncObject) {
            List<ParseObject> pos = null;
            //check how many of them have an active event now
            String currentTimeinISO = DateTime.getISO8601StringForCurrentDate();

            List<ParseQuery<ParseObject>> lpq = new ArrayList<ParseQuery<ParseObject>>();

            //For each incoming friend create a query
            for (FriendIncoming singleFriend : FriendsHelper.sFriendIncoming) {
                ParseQuery<ParseObject> pq = ParseQuery.getQuery(EVENT_TABLE);
                pq.whereEqualTo("friensoUser", singleFriend.getParseUser());
                Log.i(LOG_TAG, "friends searching for " + singleFriend.getParseUser());
                pq.whereEqualTo("eventActive",true);
              //  pq.whereDoesNotExist("overAt");
                lpq.add(pq);
            }
            //combine all the queries here and send only one request to Parse.
            ParseQuery<ParseObject> superQuery = ParseQuery.or(lpq);
            superQuery.orderByDescending("createdAt");
            try {
                pos = superQuery.find();
            } catch (ParseException e) {
                e.printStackTrace();
                sActiveIncomingEvents = null;
                return;
            }

            //now parse the returned result to create different Active Events that
            // can be later reused.

            //This arrayList will copy all the active events data from existing list and include new events
            // then these lists are swapped so that inactive events are removed.
            ArrayList<ActiveIncomingEvent>  currentlyActiveEvents = new ArrayList<ActiveIncomingEvent>();
            for (ParseObject po : pos) {
                Log.i(LOG_TAG, "Event User found " + po.get("friensoUser"));
                ParseUser pu = (ParseUser) po.get("friensoUser");
                try {
                    //get the whole user object
                    pu.fetchIfNeeded();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (sActiveIncomingEvents == null) {
                    sActiveIncomingEvents = new ArrayList<ActiveIncomingEvent>();
                }
                //check if av user already exists then ignore else add


                boolean matchFound = false;
                for (ActiveIncomingEvent ae : sActiveIncomingEvents) {
                    if (ae.sameUser(pu)) {
                        matchFound = true;
                        //it does not exists in the new arraylist
                        if(!exists(currentlyActiveEvents,pu)) {
                            currentlyActiveEvents.add(ae);
                        }
                    }
                }
                if (!matchFound) {
                    if(!exists(currentlyActiveEvents,pu)) {
                        ActiveIncomingEvent av = new ActiveIncomingEvent(pu, (po.getCreatedAt()).getTime());
                        currentlyActiveEvents.add(av);
                    }
                }

            }
            //swapping out the old elements. GC GC GC GC!!!
            sActiveIncomingEvents = currentlyActiveEvents;
            Log.i(LOG_TAG," currently Active Event Size " + currentlyActiveEvents.size()  );

        }
        Log.i(LOG_TAG," sActiveIncoming Event Size " + sActiveIncomingEvents.size()  );
        if(sActiveIncomingEvents !=null && sActiveIncomingEvents.size() != 0){
            //TODO: incomingEventInfoRetrival is a dead code. remove it.
            //start Service
           // startEventService();
           fetchData();
        }//else {
           // stopEventService();
      //  }
    }


    private static boolean exists(ArrayList<ActiveIncomingEvent> list, ParseUser pu) {

        for (ActiveIncomingEvent ae: list) {
            if(ae.sameUser(pu)) {
                return true;
            }
        }

        return false;
    }


    /* This is the method that does all the heavy lifting
 */
    private static void fetchData () {

        //update each user's active event separately .. TODO: finally we should do it all in one query
        for (ActiveIncomingEvent ae : EventHelper.sActiveIncomingEvents) {
            List<ParseObject> pofs;
            ParseQuery<ParseObject> pq = ParseQuery.getQuery(ParseDataStructures.LOCATION_DATA_TABLE);
            long timeStamp;
            if(ae.mLocationArray.size()==0){
                timeStamp = ae.createdAtInMillis - 1 ; //to get greater than equal to
            } else {
                timeStamp = (ae.mLocationArray.get(ae.mLocationArray.size() - 1)).timeStampInMillis;
            }

            //convert timeStamp into ISO format
            String UTCtimeStamp = DateTime.getISO8601StringForTimeStampInMillis(timeStamp);

            pq.whereEqualTo("user",ae.mUser);
            pq.whereGreaterThan("createdAt",UTCtimeStamp);
            pq.orderByAscending("createdAt");

            //TODO:remove the line below. This is to limit the output
           // pq.setLimit(50);

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
                        pgp.getLatitude() + " " + pgp.getLongitude() + " " +(int) pof.get("accuracy")
                        + " " + (pof.getCreatedAt()).getTime());
            }
        }
    }

    public static void startEventService () {
        Log.i(LOG_TAG,"Event Service Started");
        IncomingEventInfoRetrieval.stopService = false;
        //TODO: check if there is an event already Active.. in that case ignore this
        sContext.startService(new Intent(sContext, IncomingEventInfoRetrieval.class));

    }

    public static void stopEventService () {
        IncomingEventInfoRetrieval.stopService = true;
    }
    public static void tellParseAlertIsOn () {
        //TODO: check if there is an existing event that is ON for this user. cancel it
        //Start a new event
        ParseObject po = new ParseObject(EVENT_TABLE);
        po.put(PARSE_EVENT_TABLE_FRIENSO_USER,ParseUser.getCurrentUser());
        po.put(PARSE_EVENT_TABLE_EVENT_TYPE,PARSE_EVENT_TABLE_EVENT_TYPE_WATCHME);
        po.put(PARSE_EVENT_TABLE_ACTIVE_EVENT,true);
        po.saveInBackground();
    }


    public static void tellParseAlertIsOff () {

        ParseQuery<ParseObject> pq = ParseQuery.getQuery(EVENT_TABLE);
        pq.whereEqualTo(PARSE_EVENT_TABLE_FRIENSO_USER, ParseUser.getCurrentUser());
        pq.whereEqualTo(PARSE_EVENT_TABLE_ACTIVE_EVENT,true);
        pq.whereEqualTo(PARSE_EVENT_TABLE_EVENT_TYPE,PARSE_EVENT_TABLE_EVENT_TYPE_WATCHME);
        pq.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                //TODO:HANDLE Parse exceptions. may be create a class to handle all the errors
                for (ParseObject po : parseObjects) {
                    po.put(PARSE_EVENT_TABLE_ACTIVE_EVENT, false);
                    po.saveInBackground();
                }
            }
        });

    }

    /**
     * Use this method to find if a particualr user identified by phoneNumber is having an active event
     * @param usersPhoneNumber - user's phonenumber to search with
     * @return
     */
    public static boolean hasActiveEvent(String usersPhoneNumber) {
        for (ActiveIncomingEvent aie: EventHelper.sActiveIncomingEvents){
            //TODO: Change this if the phone number is no longer the identity of the user
            if (aie.mUser.get("phoneNumber").toString().compareTo(usersPhoneNumber) == 0)
                return true;
        }
        return false;
    }
}
