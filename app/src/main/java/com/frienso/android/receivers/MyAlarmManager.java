package com.frienso.android.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

import com.frienso.android.helper.LocationHelper;

/**
 * Created by udayan kumar on 1/19/15.
 */
public class MyAlarmManager extends BroadcastReceiver{

    Context mContext;
    // this constructor is called by the alarm manager.
    public MyAlarmManager(){ }

    public MyAlarmManager(Context context) {

        this.mContext = context;
    }

    public void setRepeatingAlarm(int timeoutInSeconds) {

        if(isAlarmSet())
            cancelAlarm();

        AlarmManager alarmMgr =
                (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(mContext, 0, getIntent(),
                        PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.setRepeating(android.app.AlarmManager.RTC_WAKEUP,0, timeoutInSeconds*1000,
                pendingIntent);
    }

    private Intent getIntent() {
        return new Intent(mContext, MyAlarmManager.class);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
       Toast.makeText(context, "Alarm went off", Toast.LENGTH_SHORT).show();
       LocationHelper lh = new LocationHelper(context, LocationManager.GPS_PROVIDER);
       lh.registerForLocationUpdates();
    }


    public void cancelAlarm() {
        AlarmManager alarmMgr =
                (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(mContext, 0, getIntent(),
                        PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public boolean isAlarmSet() {
        return  (PendingIntent.getBroadcast(mContext, 0, getIntent(),
                PendingIntent.FLAG_NO_CREATE) != null);

    }
}
