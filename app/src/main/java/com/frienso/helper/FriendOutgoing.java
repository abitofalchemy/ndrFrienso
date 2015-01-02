package com.frienso.helper;

import android.content.Context;

import com.frienso.android.application.R;
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by udayan kumar on 11/12/14.
 */
public class FriendOutgoing extends Friend{

    private boolean isOnParse = false;

    public FriendOutgoing(ParseUser pu, String phoneNumber, String lname, String fname) {
        super(pu,phoneNumber, lname, fname);
    }

    public FriendOutgoing(ParseUser pu, String phoneNumber, String lname, String fname, boolean isOnParse) {
        super(pu,phoneNumber, lname, fname);
        this.isOnParse =isOnParse;
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
