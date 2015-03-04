package com.frienso.android.application;


import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;

/**
 * Created by udayan kumar on 2/2/15.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ParseCrashReporting.enable(this);
        Parse.initialize(this, Splash.PARSE_APPLICATION_ID, Splash.PARSE_CLIENT_KEY);

        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
        ParsePush.subscribeInBackground("udayan");
        ParsePush.subscribeInBackground("global", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });
    }
}
