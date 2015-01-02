package com.frienso.helper;

import com.parse.Parse;
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by Udayan Kumar on 11/23/14.
 *
 * This class will only contain active events. Any event that is over should be removed from the list
 *
 */
public class ActiveIncomingEvent {
    public ParseUser mUser;
    public ArrayList<SingleLocationEvent> mLocationArray;
    public String mEventText;
    public long createdAtInMillis;


    ActiveIncomingEvent(ParseUser pu,long createdAt) {
        this.mUser = pu;
        this.createdAtInMillis = createdAt;
        mLocationArray = new ArrayList<SingleLocationEvent>();
    }

    ActiveIncomingEvent(ParseUser pu, long createdAt,String eventText) {
        this(pu,createdAt);
        this.mEventText = eventText;
    }

    public void addLocationEvent(double latitude, double longitude, int accuracy, long time) {
        //Calls to this methods must be done with ascending order in terms of createdAt values.
        SingleLocationEvent  sle = new SingleLocationEvent(latitude,longitude,accuracy,time);
        mLocationArray.add(sle);
    }

    public boolean sameUser(ParseUser pu) {
        if(mUser.hasSameId(pu))
            return true;
        else
            return false;
    }
}
