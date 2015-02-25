package com.frienso.helper;

import android.util.Log;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Created by udayan kumar on 11/12/14.
 */
public class FriendOutgoing extends Friend{

    private static final String LOG_TAG = "FriendOutgoing" ;
    //isOnFrienso tells if the user has an active account on Frienso
    private boolean isOnFrienso = false;
    private boolean isDummy = true;

    public FriendOutgoing(ParseUser pu, String phoneNumber, String name) {
        super(pu,phoneNumber, name);
        isDummy = false;
        isOnFrienso = false;
    }

    public FriendOutgoing(ParseUser pu, String phoneNumber, String name, boolean isOnFrienso) {
        super(pu,phoneNumber,name);
        this.isOnFrienso =isOnFrienso;
        isDummy = false;
    }

    public FriendOutgoing() {
        super();
        isDummy = true;
    }

    public boolean isDummy () {
        return  isDummy;
    }

    public boolean isOnFrienso() {
        return isOnFrienso;
    }

    public void addFriendOnParse(final OperationComplete callback) {

        if (isDummy) {
            callback.friendOperationComplete(FriendOperationResult.DUMMY);
        }

        //check if the user is already a friend
        //if so call the callback with duplicate friend message


        //check if the user is on frienso
        //if so add and callback with Success
        ParseQuery<ParseUser> pq = ParseUser.getQuery();
        pq.whereEqualTo(ParseDataStructures.PhoneNumber, phoneNumber);
        pq.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        //user is on parse.
                        ParseUser pu = objects.get(0);
                        ParseObject newFriend = new ParseObject(ParseDataStructures.CORE_FRIEND_TABLE);
                        newFriend.put(ParseDataStructures.CORE_FRIEND_TABLE_SENDER,ParseUser.getCurrentUser());
                        newFriend.put(ParseDataStructures.CORE_FRIEND_TABLE_RECIPIENT,pu);
                        newFriend.put(ParseDataStructures.CORE_FRIEND_TABLE_STATUS,ParseDataStructures.CORE_FRIEND_TABLE_ACCEPT_TEXT);


                        //Set ACL
                        ParseACL pacl = new ParseACL(ParseUser.getCurrentUser());
                        pacl.setPublicReadAccess(false);
                        pacl.setPublicWriteAccess(false);
                        pacl.setWriteAccess(ParseUser.getCurrentUser(),true);
                        pacl.setWriteAccess(pu,true);
                        newFriend.setACL(pacl);

                        newFriend.saveInBackground(new SaveCallback() {
                            public void done(ParseException e) {
                                if (e == null) {
                                    callback.friendOperationComplete(FriendOperationResult.SUCCESS);
                                } else {
                                    callback.friendOperationComplete(FriendOperationResult.FAILURE);
                                    Log.e(LOG_TAG,"Error while finding friend on parse core friends ");
                                }
                            }
                        });

                    } else {
                        //user is not on frienso .. add to the other table
                        ParseObject newFriend = new ParseObject(ParseDataStructures.CORE_FRIEND_NOT_ON_FRIENSO_TABLE);
                        newFriend.put(ParseDataStructures.CORE_FRIEND_NOT_ON_FRIENSO_TABLE_SENDER,ParseUser.getCurrentUser());
                        newFriend.put(ParseDataStructures.CORE_FRIEND_NOT_ON_FRIENSO_TABLE_RECIPIENT_PHONE_NUMBER,phoneNumber);
                        newFriend.put(ParseDataStructures.CORE_FRIEND_NOT_ON_FRIENSO_TABLE_RECIPIENT_NAME,name);

                        //Set ACL
                        ParseACL pacl = new ParseACL(ParseUser.getCurrentUser());
                        pacl.setPublicWriteAccess(true);
                        pacl.setWriteAccess(ParseUser.getCurrentUser(),true);
                        newFriend.setACL(pacl);

                        newFriend.saveInBackground(new SaveCallback() {
                            public void done(ParseException e) {
                                if (e == null) {
                                    callback.friendOperationComplete(FriendOperationResult.SUCCESS);
                                } else {
                                    callback.friendOperationComplete(FriendOperationResult.FAILURE);
                                    Log.e(LOG_TAG,"Error while finding friend on parse not in core friends " + e.toString());

                                }
                            }
                        });
                    }
                } else {
                    // Something went wrong.
                    callback.friendOperationComplete(FriendOperationResult.FAILURE);
                    Log.e(LOG_TAG,"Error while searching for matching friends ");

                }
            }
        });

    }

    public void delete(final  OperationComplete callback) {
        if (isDummy) {
            callback.friendOperationComplete(FriendOperationResult.DUMMY);
        }
        ParseQuery<ParseUser> innerQ = ParseUser.getQuery();
        innerQ.whereEqualTo(ParseDataStructures.PhoneNumber, phoneNumber);

        ParseQuery<ParseObject> pq = ParseQuery.getQuery(ParseDataStructures.CORE_FRIEND_TABLE);
        pq.whereEqualTo("sender", ParseUser.getCurrentUser());
        pq.whereMatchesQuery("recipient",innerQ);
        pq.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        //user is on parse.
                        ParseObject po = objects.get(0);
                        po.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    callback.friendOperationComplete(FriendOperationResult.SUCCESS);
                                } else {
                                    callback.friendOperationComplete(FriendOperationResult.FAILURE);
                                }
                            }
                        });
                    } else {
                        //user is not on parse. Search in Friends not on frienso DB.
                        ParseQuery<ParseObject> pq = ParseQuery.getQuery(ParseDataStructures.CORE_FRIEND_NOT_ON_FRIENSO_TABLE);
                        pq.whereEqualTo("sender", ParseUser.getCurrentUser());
                        pq.whereEqualTo(ParseDataStructures.CORE_FRIEND_NOT_ON_FRIENSO_TABLE_RECIPIENT_PHONE_NUMBER,phoneNumber);
                        pq.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> objects, ParseException e) {
                                if (e == null) {
                                    if (objects.size() > 0) {
                                        //user is on parse.
                                        ParseObject po = objects.get(0);
                                        po.deleteInBackground(new DeleteCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    callback.friendOperationComplete(FriendOperationResult.SUCCESS);
                                                } else {
                                                    callback.friendOperationComplete(FriendOperationResult.FAILURE);
                                                }
                                            }
                                        });
                                    }
                                    else {
                                        callback.friendOperationComplete(FriendOperationResult.FriendNotFound);
                                    }
                                } else {
                                    callback.friendOperationComplete(FriendOperationResult.FAILURE);
                                }
                            }
                        });
                    }
                } else {
                    // Something went wrong.
                    callback.friendOperationComplete(FriendOperationResult.FAILURE);
                }
            }
        });

    }


}
