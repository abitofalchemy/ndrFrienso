package com.frienso.helper;

import com.parse.ParseUser;

/**
 * Created by udayan kumar on 11/12/14.
 */
public class FriendOutgoing extends Friend{

    private boolean isOnParse = false;
    private boolean isDummy = true;

    public FriendOutgoing(ParseUser pu, String phoneNumber, String lname, String fname) {
        super(pu,phoneNumber, lname, fname);
        isDummy = false;
    }

    public FriendOutgoing(ParseUser pu, String phoneNumber, String lname, String fname, boolean isOnParse) {
        super(pu,phoneNumber, lname, fname);
        this.isOnParse =isOnParse;
        isDummy = false;
    }

    public FriendOutgoing() {
        super();
        isDummy = true;
    }

    public boolean isDummy () {
        return  isDummy;
    }

    public static boolean addFriendOnParse() {
        //TODO: Implement this
        return true;
    }

    public static boolean delete() {
        //TODO: implement this
        return true;
    }


}
