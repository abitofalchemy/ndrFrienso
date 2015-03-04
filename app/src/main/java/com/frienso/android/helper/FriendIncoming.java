package com.frienso.android.helper;

import com.parse.ParseUser;

/**
 * Created by udayan kumar on 11/12/14.
 */
public class FriendIncoming extends  Friend{


    public FriendIncoming(ParseUser pu, String phoneNumber, String name) {
        super(pu,phoneNumber,name);
    }

    public void block(OperationComplete callback) {
        //TODO: implement this method

    }


}

