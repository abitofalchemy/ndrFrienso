package com.frienso.android.application;


import android.app.Application;

import com.parse.Parse;
import com.parse.ParseCrashReporting;

/**
 * Created by rjain6 on 2/2/15.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ParseCrashReporting.enable(this);
        Parse.initialize(this, Splash.PARSE_APPLICATION_ID, Splash.PARSE_CLIENT_KEY);
    }
}
