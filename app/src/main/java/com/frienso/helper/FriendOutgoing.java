package com.frienso.helper;

import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by udayan kumar on 11/12/14.
 */
public class FriendOutgoing {
    private String phoneNumber;
    private String lname;
    private String fname;
    private ParseUser pu;
    private boolean isOnParse = false;

    public FriendOutgoing(ParseUser pu, String phoneNumber, String lname, String fname) {
        this.lname = lname;
        this.fname = fname;
        this.phoneNumber = phoneNumber;

    }

    public FriendOutgoing(ParseUser pu, String phoneNumber, String lname, String fname, boolean isOnParse) {
        this(pu,phoneNumber, lname, fname);
        this.isOnParse =isOnParse;
    }

    public String getNumber() {
        return phoneNumber;
    }

    public static boolean addFriendOnParse() {
        return true;
    }

    public static boolean delete() {
        return true;
    }
}
