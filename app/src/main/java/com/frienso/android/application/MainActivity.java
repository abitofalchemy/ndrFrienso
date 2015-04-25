package com.frienso.android.application;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.frienso.android.helper.EventHelper;
import com.frienso.android.helper.Friend;
import com.frienso.android.helper.FriendIncoming;
import com.frienso.android.helper.FriendsHelper;
import com.frienso.android.helper.LocationHelper;
import com.frienso.android.helper.MapHelper;
import com.frienso.android.receivers.MyAlarmManager;
import com.frienso.android.services.LocationService;
import com.frienso.android.utils.Network;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity implements  OnMapReadyCallback {


    public final static  String INTENT_MSG_TYPE_SHOW_USER ="ShowUser";
    public final static  String INTENT_MSG_TYPE_SHOW_USER_PHONENUMBER ="PhoneNumber";
    private final static int HANDLER_FRIENDS_REFRESHED = 1;
    private final static String SELECT_TEXT = "Select";
    private final static String EMPTY_TEXT = "";
    private final static String CHANGE_FRIENDS = "+/- Friends";
    private final static String LOADING_FRIENDS = "Loading";
    private final static int REFRESH_DATA_IN_MILLIS = 1 * 60 * 1000;
    private final static int REFRESH_MENU_CHECK_IN_MILLIS =  5 * 1000;
    private final static int TRACKING_ALERT_NOTIFICATION_ID = 1;
    public static final String BROADCAST_MESSAGE_TYPE = "BROADCAST_MESSAGE_TYPE";
    public static final String BROADCAST_MESSAGE_LOCATION_RECEIVED = "LOCATION_RECEIVED";
    private boolean isLocationServiceOn = false;

    public static final String INTENT_ACTION_WHEN_LOCATION_UPDATED = "com.frienso.android.app.MainActivity.LocationUpdate";


    private boolean mIsMapReady = false;
    private GoogleMap mGoogleMap;
    private String mUserOnMap;
    private Friend mFriendOnMap;
    private Context mContext;
    private android.os.Handler mHandler;
    private MyAlarmManager mMyAlarmManager;
    Menu mMenu;
    private NotificationManager mNotificationManager;
    private BroadcastReceiver mReceiver;


    private final String LOGTAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new MainActivityBroadCastReceiver();
        setContentView(R.layout.activity_main);
        mContext = this;
        mUserOnMap = null;
        Log.i(LOGTAG, "onCreate" + android.os.Process.getThreadPriority(android.os.Process.myTid()));


        //Check the intent values. If the type is ShowUser then read the phoneNumber and set the mUserOnMap
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            processIntent(extras);
        }

        //Handler part of the messaging.
        mHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case(HANDLER_FRIENDS_REFRESHED):
                        Log.i(LOGTAG,"This is the time to update the UI Values");
                        if(mMenu == null){
                            //this is to avoid setting up menu even before it is loaded by the system
                            sendEmptyMessageDelayed(HANDLER_FRIENDS_REFRESHED,REFRESH_MENU_CHECK_IN_MILLIS);
                        } else {
                            loadFriendsAgain();
                            loadActiveEventsAgain();
                        }
                        break;
                }
            }
        };

        //this should be set before call any event method
        EventHelper.setContext(mContext);
        //check network connectivity
        if(!Network.hasInternet(mContext)) {
            Toast.makeText(mContext,R.string.no_internet,Toast.LENGTH_LONG).show();
            return;
        }
        //following this https://developers.google.com/maps/documentation/android/map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mMyAlarmManager = new MyAlarmManager(mContext);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    private void processIntent(Bundle extras) {
        //check the msgType
        //check the msgValue
        if((boolean)extras.get(INTENT_MSG_TYPE_SHOW_USER)) {
            // The msg type is Show user on map.
            String phoneNumber = null;
            if((phoneNumber= (String) extras.get(INTENT_MSG_TYPE_SHOW_USER_PHONENUMBER))!=null) {
                //we have the phone number that we want to show
                mUserOnMap = phoneNumber;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        ActionBar actionBar = getActionBar();
        //use the line below to disable display of title and logo
        //actionBar.setDisplayOptions(0);
        getMenuInflater().inflate(R.menu.main, menu);

        //set switch text
        MenuItem item = mMenu.findItem(R.id.mainslidealert);
        Switch aswitch = (Switch) MenuItemCompat.getActionView(item);
        aswitch.setTextOff(getString(R.string.tracking_switch_off));
        aswitch.setTextOn(getString(R.string.tracking_switch_on));
        //if the alarm is set, move the switch to on position
        if(mMyAlarmManager.isAlarmSet()){
            aswitch.setChecked(true);
        }
        aswitch.setOnCheckedChangeListener(mAlertButtonChangedListerner);


        //To setup spinner use this stackoverflow answer by francois
        //http://stackoverflow.com/questions/11377760/adding-spinner-to-actionbar-not-navigation
        //test array list
        ArrayList<String> a = new ArrayList<String>();
        a.add(LOADING_FRIENDS);
        setSpinnerMenu(a);
        return true;
    }

    CompoundButton.OnCheckedChangeListener mAlertButtonChangedListerner = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                //user has started the tracking activity
                startAlert();
            }else {
                //user has turned off tracking
                stopAlert();
            }
        }
    };

    private void stopAlert() {

        if (mMyAlarmManager != null) {
            mMyAlarmManager.cancelAlarm();
        }

        //remove notification
        clearOnGoingAlertNotification();
        //turn off event in parse
        EventHelper.tellParseAlertIsOff();

    }


    private void clearOnGoingAlertNotification() {
        mNotificationManager.cancel(TRACKING_ALERT_NOTIFICATION_ID);

    }

    private void setOnGoingAlertNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.frienso_icon)
                        .setContentTitle(getString(R.string.notification_alert_on))
                        .setContentText(getString(R.string.notification_text))
                        .setOngoing(true);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);


        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(mContext, 0, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        // TRACKING_ALERT_NOTIFICATION_ID allows you to update the notification later on.
        mNotificationManager.notify(TRACKING_ALERT_NOTIFICATION_ID, mBuilder.build());
    }

    private void startAlert() {

        // show a dialog box to get more details about the event
        // check that GPS is enabled.
        if(!isGPSEnabled()){
            showAlertGPSIsDisbled();
            //reset the slider to off position
            MenuItem item = mMenu.findItem(R.id.mainslidealert);
            Switch aswitch = (Switch) MenuItemCompat.getActionView(item);
            aswitch.setChecked(false);
            return;
        }

        //set event information on Parse
        EventHelper.tellParseAlertIsOn();

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("title", mContext.getString(R.string.trackingStartNotificationMessage));
        params.put("message", mContext.getString(R.string.tracking_alert_msg));
        ParseCloud.callFunctionInBackground("sendTrackEventStartNotification", params, new FunctionCallback<String>() {

            @Override
            public void done(String s, ParseException e) {
                if (e == null) {
                    Log.i(LOGTAG,"push message successful");
                } else {
                    Log.e(LOGTAG,"Push message failed" + e.getMessage());
                }
            }
        });

        // setup a local notification, saying that location is being shared.
        setOnGoingAlertNotification();
        // start the alarm manager to retrieve location.
        mMyAlarmManager.setRepeatingAlarm(60);
        
    }

    private boolean isGPSEnabled() {
        // show a dialog box to get more details about the event
        // check that GPS is enabled.
        LocationHelper lh = new LocationHelper(mContext,LocationManager.GPS_PROVIDER);
        if (!lh.isProviderEnabled()) {

            return false;
        }
        return true;
    }

    private void showAlertGPSIsDisbled() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.gpsIsNotEnabledMessage);

        builder.setPositiveButton(R.string.optionEnableGPS,new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(callGPSSettingIntent);
            }
        });

        builder.setNegativeButton(R.string.cancelEnablingGPS, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(LOGTAG, "Did not enable GPS. operation cancelled");

            }
        });
        builder.show();
    }

    public void setSpinnerMenu(ArrayList<String> options) {
        MenuItem item = mMenu.findItem(R.id.mainspinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, options);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setAdapter(spinnerArrayAdapter);
        SpinnerItemListener listener = new SpinnerItemListener();
        spinner.setOnItemSelectedListener(listener);
        spinner.setOnTouchListener(listener);

    }

    public void setSpinnerFocusOnFirstPosition(){
        MenuItem item = mMenu.findItem(R.id.mainspinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setSelection(0);
    }

    /* use this method to set Focus on the user pointer to by mUserOnMap */
    private void setSpinnerFocusOnItem(String mUserOnMap) {
        MenuItem item = mMenu.findItem(R.id.mainspinner);
        //get name for the number
        String name = FriendsHelper.getNameFromNumberIncomingFriends(mUserOnMap,mContext);
        if (name == null) {
            name = mUserOnMap;
        }

        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        for (int i =0; i< spinner.getCount();i++){
            if(name.compareTo((String)spinner.getItemAtPosition(i))==0){
                spinner.setSelection(i);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mIsMapReady = true;
        mGoogleMap = googleMap;
        //mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }



    class SpinnerItemListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
        boolean userSelect = false;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            userSelect = true;
            Log.i(LOGTAG,"User selected a menu item");
            return false;
        }


        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (!userSelect) {
                return;
            }
            userSelect = false;
            Log.i(LOGTAG, "Menu Item clicked"+ parent.getItemAtPosition(position).toString());
            String selectedText = parent.getItemAtPosition(position).toString();

            //to make sure that if a user has name same as SELECTED_TEXT we donot make an error
            //added a position
            if(selectedText.compareTo(SELECT_TEXT) == 0 && position == 0) {
                //TODO: Remove the currently mapped user ??. For now doing nothing
                mUserOnMap = null;
                startDeviceLocationService();

                return;
            } else if(selectedText.compareTo(EMPTY_TEXT) == 0){
                //Empty line cannot be selected. Move the selection to SELECT_TEXT
                //TODO:Same as above
                setSpinnerFocusOnFirstPosition();
                return;
            } else if(selectedText.compareTo(CHANGE_FRIENDS) == 0) {
                //TODO: Start an Activity to allow changing of Friends
                setSpinnerFocusOnFirstPosition();
                startActivity(new Intent(mContext,FriendManipulator.class));
                return;
            } else if (selectedText.compareTo(LOADING_FRIENDS) == 0) {
                //do nothing..
                return;
            } else {
                // This is the section where a user is selected and her path is to be shown

                String phoneNumber = FriendsHelper.getNumberFromNameIncomingFriends(selectedText,mContext);
                if (phoneNumber == null) {
                    phoneNumber = selectedText;
                    Log.i(LOGTAG,"Name not found for the selectedText " +selectedText);
                }
                //TODO: if the user is not having an active event then show alert that no event active for
                // this user
                if (!EventHelper.hasActiveEvent(phoneNumber)) {

                }
                stopDeviceLocationService();
                mGoogleMap.clear();
                loadMapForUser(phoneNumber);
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.i(LOGTAG, "Menu Item nothing selected");

        }
    }

   synchronized private void stopDeviceLocationService() {
       if(isLocationServiceOn) {
           mContext.stopService(new Intent(mContext, LocationService.class));
           isLocationServiceOn = false;
       }
    }

   synchronized private void startDeviceLocationService() {

       if(isLocationServiceOn)
           return;

        if (isGPSEnabled()) {
            mContext.startService(new Intent(mContext, LocationService.class));
            isLocationServiceOn = true;
        }else {
            showAlertGPSIsDisbled();
        }
    }


    /**
     *
     * @param mapThisUser - this is a phoneNumber
     */
    private void loadMapForUser(String mapThisUser) {

        mUserOnMap = mapThisUser;

        MapHelper.showOnMap(mContext, mUserOnMap, mGoogleMap);
    }

    private void loadMapforThisDevice() {

        /* Steps:
        1. Read the values from preference file.
        2. If timestamp less than 2 mins, display them on map.
         */
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                LocationService.LOCATION_PREFERENCE_FILE, Context.MODE_PRIVATE);
        long timestamp = sharedPref.getLong(LocationService.LOCATION_PREFERENCE_FILE_TIMESTAMP, 0L);
        float latitude = sharedPref.getFloat(LocationService.LOCATION_PREFERENCE_FILE_LATITUDE, 0.0f);
        float longitude = sharedPref.getFloat(LocationService.LOCATION_PREFERENCE_FILE_LONGITUDE,0.0f);
        float accuracy = sharedPref.getFloat(LocationService.LOCATION_PREFERENCE_FILE_ACCURACY,500000.0f);

        MapHelper.showOnMap(mContext, mGoogleMap, latitude,longitude,timestamp,accuracy);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadFriendsAgain() {
        //Reload all the layouts that have friends data
        Log.i(LOGTAG,"Friends reloaded");
        ArrayList<String> a = new ArrayList<String>();
        a.add(SELECT_TEXT);
        for (FriendIncoming fi: FriendsHelper.sFriendIncoming) {
            String name = fi.getFullName(mContext);
            if (name == null) {
                name = fi.getNumber();
            }
            a.add(name);
        }
        a.add(EMPTY_TEXT);
        a.add(CHANGE_FRIENDS);
        setSpinnerMenu(a);
    }

    public void loadActiveEventsAgain() {
        //TODO:mark the UI so that user can identify the active events.
        Log.i(LOGTAG,"EVents updated");
        //reload the map for the existing user
        if(mUserOnMap != null) {
            loadMapForUser(mUserOnMap);
            setSpinnerFocusOnItem(mUserOnMap);
        } else {
            // we want to show the current location of the device on the map
            loadMapforThisDevice();
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        //check for google play services used by maps and locations
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext) != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext),this, 0).show();
        }

        invalidateOptionsMenu();
        startRepeatingTask();
        if(mUserOnMap == null) {
            startDeviceLocationService();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ACTION_WHEN_LOCATION_UPDATED);
        registerReceiver(mReceiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopRepeatingTask();
        stopDeviceLocationService();
        unregisterReceiver(mReceiver);
    }


    void startRepeatingTask() {
        new Thread(mRefresh).start();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mRefresh);
        EventHelper.stopEventService();
    }

    Runnable mRefresh = new Runnable() {

        @Override
        public void run() {
            Log.i(LOGTAG, "Run" + android.os.Process.getThreadPriority(android.os.Process.myTid()));

            FriendsHelper.refreshFriends(mContext);
            //now update the events since we have the friend information
            EventHelper.refreshActiveEvents();
            mHandler.postDelayed(mRefresh,REFRESH_DATA_IN_MILLIS);
            mHandler.sendEmptyMessage(HANDLER_FRIENDS_REFRESHED);

        }
    };


    class MainActivityBroadCastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(BROADCAST_MESSAGE_LOCATION_RECEIVED.compareTo(bundle.getString(BROADCAST_MESSAGE_TYPE)) == 0) {
                if(mUserOnMap == null) {
                    loadMapforThisDevice();
                }
            }
        }

    }

}
