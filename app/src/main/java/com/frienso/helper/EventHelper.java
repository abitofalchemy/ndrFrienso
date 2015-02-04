package com.frienso.helper;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.frienso.services.IncomingEventInfoRetrieval;
import com.frienso.utils.DateTime;
import com.parse.FindCallback;
import com.parse.ParseException;
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
                //TODO: do we need the below check?
                pq.whereLessThan("createdAt", currentTimeinISO);
                //TODO: Enable this is in the prod version
                //pq.whereEqualTo("eventActive","true");
                pq.whereDoesNotExist("endDateTime");
                lpq.add(pq);
            }
            //combine all the queries here and send only one request to Parse.
            ParseQuery<ParseObject> superQuery = ParseQuery.or(lpq);
            try {
                pos = superQuery.find();
            } catch (ParseException e) {
                e.printStackTrace();
                sActiveIncomingEvents = null;
                return;
            }

            //now parse the returned result to create different Active Events that
            // can be later reused.

            for (ParseObject po : pos) {
                Log.i(LOG_TAG, "Event User found " + po.get("friensoUser"));
                ParseUser pu = (ParseUser) po.get("friensoUser");
                try {
                    //get the whole user object
                    pu.fetchIfNeeded();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                ActiveIncomingEvent av = new ActiveIncomingEvent(pu, (po.getCreatedAt()).getTime());
                if (sActiveIncomingEvents == null) {
                    sActiveIncomingEvents = new ArrayList<ActiveIncomingEvent>();
                }
                //check if av user already exists then ignore else add
                boolean matchFound = false;
                for (ActiveIncomingEvent ae : sActiveIncomingEvents) {
                    if (ae.sameUser(pu))
                        matchFound = true;
                }
                if (!matchFound) {
                    sActiveIncomingEvents.add(av);
                }

            }

        }
        Log.i(LOG_TAG," sActiveIncoming Event Size " + sActiveIncomingEvents.size()  );
        if(sActiveIncomingEvents !=null && sActiveIncomingEvents.size() != 0){
            //start Service
            startEventService();
        }else {
            stopEventService();
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
}
