package com.frienso.android.helper;

import android.content.Context;
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
    private final static String CORE_FRIEND_NOT_ON_FRIENSO_TABLE = "CoreFriendNotOnFriensoYet";
    private final static String ACCEPT_TEXT = "accept";

    static ArrayList<FriendsUpdated> callBack = new ArrayList<FriendsUpdated>();
    static long lastUpdateTimeInMS  =  0;
    final static long minTimeBetweenFriendReloadInMS = 1  * 60 * 1000;

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
     public static void refreshFriends(Context context){
      //   if((System.currentTimeMillis() - lastUpdateTimeInMS) < minTimeBetweenFriendReloadInMS)
      //       return;
         Log.i(LOG_TAG, "Refreshing friends starting now");
         sFriendIncoming = loadIncomingFriends();
         sFriendOutgoing = loadOutgoingFriends();

         //TODO: load from contacts only when the received contacts are different. Currently we load contacts everytime we refresh.
         loadInfoFromContacts(context);
         lastUpdateTimeInMS = System.currentTimeMillis();
     }



    private FriendsHelper() {

    }


    public static void addFriend(String phoneNumber, String name, Friend.OperationComplete callback) {
        //TODO: check that total number of friends do not exceed R.integer.numberOutgoingFriends
        FriendOutgoing fo = new FriendOutgoing(null, phoneNumber,name);
        fo.addFriendOnParse(callback);
    }

    public static void deleteFriend(FriendOutgoing fo, Friend.OperationComplete callback) {
          fo.delete(callback);
    }

    public static void deleteFriend(String phoneNumber,Friend.OperationComplete callback) {
        for (FriendOutgoing fo: sFriendOutgoing){
            if(fo.getNumber().compareTo(phoneNumber) ==0)
                fo.delete(callback);
        }
    }

    /*disallow a user from sending any more updates in the future*/

    public static void blockFriend(FriendIncoming fi, Friend.OperationComplete callback) {
       fi.block(callback);
    }

    public static void blockFriend(String phoneNumber,  Friend.OperationComplete callback) {
        for (FriendIncoming fi: sFriendIncoming){
            if(fi.getNumber().compareTo(phoneNumber)==0){
               blockFriend(fi,callback);
            }
        }
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

                //currently name will be NULL,, We may have to pull data out from phone book.
                String name = (String) pu.get("name");
                String phoneNumber = (String) pu.get("phoneNumber");
                Log.i(LOG_TAG, "Outgoing friend - email:" + (String) pu.getEmail());

                // we used this constructor since the user is on parse and added
                FriendOutgoing  fo = new FriendOutgoing(pu,phoneNumber,name,true);
                fol.add(fo);
            }

            // now load friends that are not on frienso yet.
            pq = ParseQuery.getQuery(CORE_FRIEND_NOT_ON_FRIENSO_TABLE);
            pq.whereEqualTo("sender", ParseUser.getCurrentUser());
            try {
                pofs =  pq.find();
            } catch (ParseException e) {
                e.printStackTrace();
                return fol;

            }
            for (ParseObject pof : pofs ){
                //currently name will be NULL,, We may have to pull data out from phone book.
                String name = (String) pof.get("recipientName");
                String phoneNumber = (String) pof.get("recipientPhoneNumber");
                Log.i(LOG_TAG, "Outgoing friend - phone:" + phoneNumber);

                // we used this constructor since the user is on parse and added
                FriendOutgoing  fo = new FriendOutgoing(null,phoneNumber,name,false);
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
                Log.i(LOG_TAG,"Number of incoming friends : "+ pofs.size());
            } catch (ParseException e) {
                e.printStackTrace();
                return fil;

            }
            for (ParseObject pof : pofs ){
                ParseUser pu = (ParseUser) pof.getParseObject("sender");
                //currently name will be NULL,, We may have to pull data out from phone book.
                String name = (String) pu.get("name");

                String phoneNumber = (String) pu.get("phoneNumber");
                Log.i(LOG_TAG,"Incoming friend - email:"+(String)pu.getEmail());

                // we used this constructor since the user is on parse and added
                FriendIncoming  fi = new FriendIncoming(pu,phoneNumber,name);
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

                    //currently name will be NULL,, We may have to pull data out from phone book.
                    String name = (String) pu.get("name");
                    String phoneNumber = (String) pu.get("phoneNumber");
                    Log.i(LOG_TAG, "Outgoing friend - email:" + (String) pu.getEmail());
                    // we used this constructor since the user is on parse and added
                    FriendOutgoing  fo = new FriendOutgoing(pu, phoneNumber,name,true);
                    fol.add(fo);

                } else {
                    //incoming
                    ParseUser pu = (ParseUser) po.getParseObject("sender");
                    //currently name will be NULL,, We may have to pull data out from phone book.
                    String name = (String) pu.get("name");

                    String phoneNumber = (String) pu.get("phoneNumber");
                    Log.i(LOG_TAG,"Incoming friend - email:"+(String)pu.getEmail());
                    // we used this constructor since the user is on parse and added
                    FriendIncoming  fi = new FriendIncoming(pu,phoneNumber,name);
                    fil.add(fi);
                }
            }
            sFriendOutgoing = fol;
            sFriendIncoming = fil;
        }

    }

    public static void loadInfoFromContacts(Context mContext) {
        //Load info from contacts here
        for (Friend fi : sFriendIncoming){
            fi.loadContactInfo(mContext);
        }

        for (Friend fo : sFriendOutgoing){
            fo.loadContactInfo(mContext);
        }
    }
}
