package com.frienso.helper;

import android.os.AsyncTask;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Udayan Kumar on 11/12/14.
 *
 * FriendsHelper is a static class. All interactions with friends should happen through this class.
 */
public class FriendsHelper {
    public static ArrayList<FriendIncoming> sFriendIncoming;
    public static ArrayList<FriendOutgoing> sFriendOutgoing;
    private static Object synchVarIncoming = new Object();
    private static Object synchVarOutgoing = new Object();
    private static Object synchVarFriends = new Object();

    private final static String CORE_FRIEND_TABLE = "CoreFriendRequest";
    private final static String ACCEPT_TEXT = "accept";

    static ArrayList<FriendsUpdated> callBack = new ArrayList<FriendsUpdated>();
    static long lastUpdateTimeInMS  =  0;
    final static long minTimeBetweenFriendReloadInMS = 60;

    private final static String LOG_TAG = "FriendsHelper";

    /* Those who need info when friends get updated should implement
       this interface and register for callback
     */
    public interface FriendsUpdated {
        void loadFriendsAgain();
    }
    /* refreshFriends should be called anytime we want to update the
       friends list
     */
     public static void refreshFriends(){
         if((System.currentTimeMillis() - lastUpdateTimeInMS) < minTimeBetweenFriendReloadInMS)
             return;
           new LoadFriends().execute();

    }

    public static void registerUpdateCallBack(FriendsUpdated friendsUpdated) {
        if (callBack.contains(friendsUpdated))
            return;
        else
            callBack.add(friendsUpdated);
    }

    public static void unregisterUpdateCallBack(FriendsUpdated friendsUpdated) {
        if (callBack.contains(friendsUpdated))
            callBack.remove(friendsUpdated);
        else
            return;
    }

    /*this method should be called everytime friends list is changed or modified */

    private static void friendsChanged() {
        for (FriendsUpdated cb : callBack){
            cb.loadFriendsAgain();
        }
    }

    private FriendsHelper() {

    }


    public static boolean addFriend(String phoneNumber, String lname, String fname) {
        FriendOutgoing fo = new FriendOutgoing(null, phoneNumber, lname, fname);
        boolean result = fo.addFriendOnParse();
        if(result){
            sFriendOutgoing.add(fo);
            friendsChanged();
        }

        return result;
    }

    public static boolean deleteFriend(FriendOutgoing fo) {
        boolean result = fo.delete();
        if(result)
            friendsChanged();
        return result;
    }

    /*disallow a user from sending any more updates in the future*/

    public static boolean blockFriend(FriendIncoming fi) {
        boolean result = fi.block();
        if(result)
            friendsChanged();
        return result;
    }




    private static ArrayList<FriendOutgoing> loadOutgoingFriends(){
        synchronized (synchVarOutgoing) {
            ArrayList<FriendOutgoing> fol = new ArrayList<FriendOutgoing>();
            ParseQuery<ParseObject> pq = ParseQuery.getQuery(CORE_FRIEND_TABLE);
            List<ParseObject> pofs;
            pq.whereEqualTo("sender", ParseUser.getCurrentUser());
            pq.whereEqualTo("status", ACCEPT_TEXT);
            pq.include("recipient");
            try {
                pofs =  pq.find();
            } catch (ParseException e) {
                e.printStackTrace();
                return fol;

            }
            for (ParseObject pof : pofs ){
                ParseUser pu = (ParseUser) pof.getParseObject("recipient");

                //currently fname,lname will be NULL,, We may have to pull data out from phone book.
                String lname = (String) pu.get("lname");
                String fname = (String) pu.get("fname");
                String phoneNumber = (String) pu.get("phoneNumber");
                Log.i(LOG_TAG, "Outgoing friend - email:" + (String) pu.getEmail());

                // we used this constructor since the user is on parse and added
                FriendOutgoing  fo = new FriendOutgoing(pu,phoneNumber,lname,fname,true);
                fol.add(fo);
            }

            return fol;
        }
    }
    private static ArrayList<FriendIncoming> loadIncomingFriends(){
        synchronized (synchVarIncoming) {
            ArrayList<FriendIncoming> fil = new ArrayList<FriendIncoming>();
            ParseQuery<ParseObject> pq = ParseQuery.getQuery(CORE_FRIEND_TABLE);
            List<ParseObject> pofs;
            pq.whereEqualTo("recipient", ParseUser.getCurrentUser());
            pq.whereEqualTo("status", ACCEPT_TEXT);
            pq.include("sender");
            try {
                pofs =  pq.find();
            } catch (ParseException e) {
                e.printStackTrace();
                return fil;

            }
            for (ParseObject pof : pofs ){
                ParseUser pu = (ParseUser) pof.getParseObject("sender");
                //currently lname, fname will be NULL,, We may have to pull data out from phone book.
                String lname = (String) pu.get("lname");
                String fname = (String) pu.get("fname");

                String phoneNumber = (String) pu.get("phoneNumber");
                Log.i(LOG_TAG,"Incoming friend - email:"+(String)pu.getEmail());

                // we used this constructor since the user is on parse and added
                FriendIncoming  fi = new FriendIncoming(pu,phoneNumber,lname,fname);
                fil.add(fi);
            }

            return fil;
        }
    }

    private static void loadFriends() {
        //TODO: use one method to minimize backend calls
        //Since Parse does not allow the use of include in the OR queries for now
        // we cannot use this method
        synchronized (synchVarFriends) {
            List<ParseObject> pos = null;

            //getting outgoing friends here
            ArrayList<FriendOutgoing> fol = new ArrayList<FriendOutgoing>();
            ParseQuery<ParseObject> pqo = ParseQuery.getQuery(CORE_FRIEND_TABLE);
            pqo.whereEqualTo("sender", ParseUser.getCurrentUser());
            pqo.whereEqualTo("status", ACCEPT_TEXT);
            pqo.include("recipient");

            //getting incoming friends here
            ArrayList<FriendIncoming> fil = new ArrayList<FriendIncoming>();
            ParseQuery<ParseObject> pqi = ParseQuery.getQuery(CORE_FRIEND_TABLE);
            pqi.whereEqualTo("recipient", ParseUser.getCurrentUser());
            pqi.whereEqualTo("status", ACCEPT_TEXT);
            pqi.include("sender");


            List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
            queries.add(pqo);
            queries.add(pqi);
            ParseQuery<ParseObject> superQuery = ParseQuery.or(queries);

            try {
                pos =  superQuery.find();
            } catch (ParseException e) {
                e.printStackTrace();
                sFriendIncoming = null;
                sFriendOutgoing = null;
            }
            for (ParseObject po : pos ) {

                if (po.get("sender") == ParseUser.getCurrentUser()){
                    //outgoing
                    ParseUser pu = (ParseUser) po.getParseObject("recipient");

                    //currently fname,lname will be NULL,, We may have to pull data out from phone book.
                    String lname = (String) pu.get("lname");
                    String fname = (String) pu.get("fname");
                    String phoneNumber = (String) pu.get("phoneNumber");
                    Log.i(LOG_TAG, "Outgoing friend - email:" + (String) pu.getEmail());
                    // we used this constructor since the user is on parse and added
                    FriendOutgoing  fo = new FriendOutgoing(pu, phoneNumber,lname,fname,true);
                    fol.add(fo);

                } else {
                    //incoming
                    ParseUser pu = (ParseUser) po.getParseObject("sender");
                    //currently lname, fname will be NULL,, We may have to pull data out from phone book.
                    String lname = (String) pu.get("lname");
                    String fname = (String) pu.get("fname");

                    String phoneNumber = (String) pu.get("phoneNumber");
                    Log.i(LOG_TAG,"Incoming friend - email:"+(String)pu.getEmail());
                    // we used this constructor since the user is on parse and added
                    FriendIncoming  fi = new FriendIncoming(pu,phoneNumber,lname,fname);
                    fil.add(fi);
                }
            }
            sFriendOutgoing = fol;
            sFriendIncoming = fil;
        }

    }

        private static class LoadFriends extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            sFriendIncoming = loadIncomingFriends();
            sFriendOutgoing = loadOutgoingFriends();
            //loadFriends();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            lastUpdateTimeInMS = System.currentTimeMillis();
            super.onPostExecute(aVoid);
            friendsChanged();
        }
    }



}
