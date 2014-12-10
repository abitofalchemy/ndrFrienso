package com.frienso.helper;

import com.parse.ParseUser;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by udayan kumar on 11/12/14.
 */
public class FriendIncoming {
    private String phoneNumber;
    private String lname;
    private String fname;
    private ParseUser pu;

    public FriendIncoming(ParseUser pu, String phoneNumber, String lname, String fname) {
        this.lname = lname;
        this.fname = fname;
        this.phoneNumber = phoneNumber;
        this.pu = pu;
    }

    public boolean block() {
        return true;
    }

    public String getNumber() {
        return phoneNumber;
    }

    public ParseUser getParseUser() { return pu;};

}

