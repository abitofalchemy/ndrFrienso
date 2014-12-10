package com.frienso.helper;

import com.parse.Parse;
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by Udayan Kumar on 11/23/14.
 */
public class ActiveIncomingEvent {
    private ParseUser mUser;
    private ArrayList<SingleLocationEvent> mLocationArray;
    private String mEventText;


    ActiveIncomingEvent(ParseUser pu) {
        this.mUser = pu;
    }

    ActiveIncomingEvent(ParseUser pu, String eventText) {
        this(pu);
        this.mEventText = eventText;
    }
}
