package com.frienso.helper;

import android.os.AsyncTask;
import android.util.Log;

import com.frienso.utils.DateTime;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Udayan Kumar on 11/22/14.
 */
public class EventHelper {

    public static ArrayList<ActiveIncomingEvent> sActiveIncomingEvents;
    private static ArrayList<EventsUpdated> callBack = new ArrayList<EventsUpdated>();
    private final static String EVENT_TABLE = "UserEvent";
    private static Object SyncObject = new Object();
    private final static String LOG_TAG = "EVENT_HELPER";

    private EventHelper() {

    }

    public interface EventsUpdated {
        void loadActiveEventsAgain();
    }

    public static void registerUpdateCallBack(EventsUpdated eventsUpdated) {
        if (callBack.contains(eventsUpdated))
            return;
        else
            callBack.add(eventsUpdated);
    }

    public static void unregisterUpdateCallBack(EventsUpdated eventsUpdated) {
        if (callBack.contains(eventsUpdated))
            callBack.remove(eventsUpdated);
        else
            return;
    }

     /*this method should be called every time events change or modified */

    private static void activeEventsChanged() {
        //start a service to get all the latitude/longitude info
        for (EventsUpdated cb : callBack){
            cb.loadActiveEventsAgain();
        }
    }

    public static void refreshActiveEvents () {
        //get a list of incoming friends
        if (FriendsHelper.sFriendIncoming.size() == 0) {
            // no incoming friend
            activeEventsChanged();
            return;
        }

        new LoadEvents().execute();
    }

    private static void loadActiveEvent() {
        synchronized (SyncObject) {
            List<ParseObject> pos = null;
            //check how many of them have an active event now
            String currentTimeinISO = DateTime.getISO8601StringForCurrentDate();

            List<ParseQuery<ParseObject>> lpq = new ArrayList<ParseQuery<ParseObject>>();

            //For each incoming friend create a query
            for (FriendIncoming singleFriend : FriendsHelper.sFriendIncoming)
            {
                ParseQuery<ParseObject> pq = ParseQuery.getQuery(EVENT_TABLE);
                pq.whereEqualTo("friensoUser", singleFriend.getParseUser());
                Log.i(LOG_TAG, "friends searching for " + singleFriend.getParseUser());
                pq.whereLessThan("createdAt", currentTimeinISO);
                //TODO: Enable this is in the prod version
                //pq.whereEqualTo("eventActive","true");
                pq.whereDoesNotExist("endDateTime");
                lpq.add(pq);
            }
            //combine all the queries here and send only one request to Parse.
            ParseQuery<ParseObject> superQuery = ParseQuery.or(lpq);
            try {
                pos =  superQuery.find();
            } catch (ParseException e) {
                e.printStackTrace();
                sActiveIncomingEvents = null;
                return;
            }

            //now parse the returned result to create different Active Events that
            // can be later reused.

            for (ParseObject po: pos) {
                Log.i(LOG_TAG, "Event User found " + po.get("friensoUser"));
                ParseUser pu = (ParseUser)po.get("friensoUser");
                try {
                    //get the whole user object
                    pu.fetchIfNeeded();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                ActiveIncomingEvent av = new ActiveIncomingEvent(pu);
            }



        }
    }


    public static void startMyEvent () {
        //check if there is an event already Active.. in that case ignore this
    }

    public static void stopMyEvent () {

    }

    private static class LoadEvents extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            loadActiveEvent();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            activeEventsChanged();
        }
    }


}
