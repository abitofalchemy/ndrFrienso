package com.frienso.android.helper;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by udayan kumar on 3/5/15.
 *
 * This class should handle all the subscription related stuff for the push notifications
 */
public class SubscribePushNotification {
    private static final String LOG_TAG="SubscribePushNoti";
    public static void subscribe () {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser != null) {
            String individualChannel = "Ph" + currentUser.get("phoneNumber");
            subscribeToChannel(individualChannel);
            subscribeToChannel("global");
        }else {
            Log.e(LOG_TAG,"Current user is null. Cannot subscribe to the channels");
        }

    }

    private static void subscribeToChannel(final String channel) {
        ParsePush.subscribeInBackground(channel, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(LOG_TAG, "successfully subscribed to the" + channel + " channel.");
                } else {
                    Log.e(LOG_TAG, "failed to subscribe for push", e);
                }
            }
        });

    }
}
