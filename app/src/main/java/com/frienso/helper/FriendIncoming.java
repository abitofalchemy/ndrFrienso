package com.frienso.helper;

import android.content.Context;

import com.frienso.android.application.R;
import com.parse.ParseUser;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by udayan kumar on 11/12/14.
 */
public class FriendIncoming extends  Friend{


    public FriendIncoming(ParseUser pu, String phoneNumber, String lname, String fname) {
        super(pu,phoneNumber,lname,fname);
    }

    public boolean block() {
        //TODO: implement this method
        return true;
    }


}

