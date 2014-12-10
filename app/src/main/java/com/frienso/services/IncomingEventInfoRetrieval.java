package com.frienso.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.frienso.helper.EventHelper;

public class IncomingEventInfoRetrieval extends Service {
    private final static int sSleepInMillis = 1 * 60 * 1000;
    public IncomingEventInfoRetrieval() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean repeatAgain = true;
        while (repeatAgain) {



            if (EventHelper.sActiveIncomingEvents.size() == 0) {
                repeatAgain = false;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
