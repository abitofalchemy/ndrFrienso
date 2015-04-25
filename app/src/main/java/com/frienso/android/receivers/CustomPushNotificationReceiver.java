package com.frienso.android.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.frienso.android.application.MainActivity;
import com.frienso.android.application.R;
import com.frienso.android.helper.ContactsHelper;
import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Udayan Kumar on 3/8/15.
 */
public class CustomPushNotificationReceiver extends ParsePushBroadcastReceiver {
    private final static String PARSE_DATA_KEY = "com.parse.Data";
    private final static String CUSTOM_TRACKING_MSG_TYPE = "TrackAlert";
    private final static String MSG_TYPE_KEY = "msgType";
    private final static String LOG_TAG = "CustomPushNoti";

    private static final int TRACKING_ALERT_NOTIFICATION_ID = 10;

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        for (String key : bundle.keySet()) {
            Log.e("CustomPushReceiver","Key  " +key);
            Object value = bundle.get(key);
            Log.e("CustomPushReceiver", String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
        }
        if (bundle.get(PARSE_DATA_KEY)==null) {
            Log.e(LOG_TAG, "PARSE_DATA_KEY IS NULL");
            super.onPushReceive(context, intent);
            return;
        }



        try {
            JSONObject jo =  new JSONObject((String)bundle.get(PARSE_DATA_KEY));
            if (((String)jo.get(MSG_TYPE_KEY)).compareTo(CUSTOM_TRACKING_MSG_TYPE)==0) {
                //this is a tracking message. Yay!!
                //now we need to tweak it before displaying it.

                //Steps:
                //1. get phone Number.. convert it to a name from contact.
                //2. generate  intent
                //3. generate the notification.
                String phoneNumber = (String) jo.get("phoneNumber");
                String alert = (String) jo.get("alert");
                String title = (String) jo.get("title");
                String name = ContactsHelper.getContactName(context,phoneNumber);
                if (name == null) {
                    //if we cannot get the name then show the phone number
                    name = phoneNumber;
                }

                setOnGoingAlertNotification(context,title,alert,name,phoneNumber,bundle);
                return;
            }
        } catch (JSONException e) {
                e.printStackTrace();

        }
        //if the msgType is not found or msgtype not equal to tracking type
        Log.e(LOG_TAG, "msgType Not found");
        super.onPushReceive(context, intent);
        return;
    }

    private void setOnGoingAlertNotification(Context context, String title, String message,String Name,String phoneNumber,Bundle bundle) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.frienso_icon)
                        .setContentTitle(title + " " + Name)
                        .setContentText(Name +" " +message);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent();
        resultIntent.setAction(ParsePushBroadcastReceiver.ACTION_PUSH_OPEN);
        resultIntent.putExtras(bundle);

        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(context,0,
                resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE);


        //set a broadcast intent upon deletion of the intent.
        resultIntent = new  Intent();
        resultIntent.setAction(ParsePushBroadcastReceiver.ACTION_PUSH_DELETE);
        resultIntent.putExtras(bundle);
        PendingIntent deleteIntent = PendingIntent.getBroadcast(context,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        // TRACKING_ALERT_NOTIFICATION_ID allows you to update the notification later on.
        notificationManager.notify(TRACKING_ALERT_NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {

        /* we need to override this becasue we want to track the opening of push notifications
           upon push received, the pending intent is for push open. once receiving that pending intent
           here.. we want to redirect it to the activity we really want to open. which in the case of
           track messages is MainActivity.class


           at any error, we simply switch to the default pushOpen method
         */

        //if the message type is TrackAlert

        Bundle bundle = intent.getExtras();
        for (String key : bundle.keySet()) {
            Log.e("CustomPushReceiver","push Open Key  " +key);
            Object value = bundle.get(key);
            Log.e("CustomPushReceiver", String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
        }

        //Default
        if (bundle.get(PARSE_DATA_KEY)==null) {
            Log.e(LOG_TAG, "pushopen PARSE_DATA_KEY IS NULL");
            super.onPushOpen(context, intent);
            return;
        }



        try {
            JSONObject jo =  new JSONObject((String)bundle.get(PARSE_DATA_KEY));
            if (((String)jo.get(MSG_TYPE_KEY)).compareTo(CUSTOM_TRACKING_MSG_TYPE)==0) {
                String phoneNumber = (String) jo.get("phoneNumber");
                //this is the code that tracks the opening of the app
                ParseAnalytics.trackAppOpenedInBackground(intent);
                Intent resultIntent = new Intent(context, MainActivity.class);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                resultIntent.putExtra(MainActivity.INTENT_MSG_TYPE_SHOW_USER,true);
                resultIntent.putExtra(MainActivity.INTENT_MSG_TYPE_SHOW_USER_PHONENUMBER,phoneNumber);
                context.startActivity(resultIntent);
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }
        //if the msgType is not found or msgtype not equal to tracking type
        Log.e(LOG_TAG, "pushOpen msgType Not found");
        super.onPushOpen(context, intent);
        return;

    }

}
