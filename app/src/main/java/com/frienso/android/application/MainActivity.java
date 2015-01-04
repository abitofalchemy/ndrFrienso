package com.frienso.android.application;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.frienso.android.application.R;
import com.frienso.helper.EventHelper;
import com.frienso.helper.FriendIncoming;
import com.frienso.helper.FriendsHelper;
import com.frienso.helper.MapHelper;
import com.frienso.utils.Network;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;



import java.text.ParseException;
import java.util.ArrayList;

public class MainActivity extends Activity implements  OnMapReadyCallback {

    private final static int HANDLER_FRIENDS_REFRESHED = 1;
    private final static String SELECT_TEXT = "Select";
    private final static String EMPTY_TEXT = "";
    private final static String CHANGE_FRIENDS = "+/- Friends";
    private final static String LOADING_FRIENDS = "Loading";
    private final static int REFRESH_DATA_IN_MILLIS = 1 * 60 * 1000;
    private final static int REFRESH_MENU_CHECK_IN_MILLIS =  5 * 1000;


    private boolean mIsMapReady = false;
    private GoogleMap mGoogleMap;
    private String mUserOnMap;
    private Context mContext;
    private android.os.Handler mHandler;
    Menu mMenu;


    private final String LOGTAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        Log.i(LOGTAG, "onCreate" + android.os.Process.getThreadPriority(android.os.Process.myTid()));


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


        //To setup spinner use this stackoverflow answer by francois
        //http://stackoverflow.com/questions/11377760/adding-spinner-to-actionbar-not-navigation
        //test array list
        ArrayList<String> a = new ArrayList<String>();
        a.add(LOADING_FRIENDS);
        setSpinnerMenu(a);
        return true;
    }


    public void setSpinnerMenu(ArrayList<String> options) {
        MenuItem item = mMenu.findItem(R.id.mainspinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, options);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(new SpinnerItemListener());
    }

    public void setSpinnerFocusOnFirstPosition(){
        MenuItem item = mMenu.findItem(R.id.mainspinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setSelection(0);
    }

    /* use this method to set Focus on the user pointer to by mUserOnMap */
    private void setSpinnerFocusOnItem(String mUserOnMap) {
        MenuItem item = mMenu.findItem(R.id.mainspinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        for (int i =0; i< spinner.getCount();i++){
            if(mUserOnMap.compareTo((String)spinner.getItemAtPosition(i))==0){
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

    class SpinnerItemListener implements AdapterView.OnItemSelectedListener {



        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.i(LOGTAG, "Menu Item clicked"+ parent.getItemAtPosition(position).toString());
            String selectedText = parent.getItemAtPosition(position).toString();
            if(selectedText.compareTo(SELECT_TEXT) == 0) {
                //TODO: Remove the currently mapped user ??. For now doing nothing
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
                loadMapForUser(selectedText);
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.i(LOGTAG, "Menu Item nothing selected");

        }
    }


    private void loadMapForUser(String mapThisUser) {
        mUserOnMap = mapThisUser;
        MapHelper.showOnMap(mContext, mUserOnMap, mGoogleMap);
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
            //TODO: change numbers to name from contacts.
            a.add(fi.getNumber());
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
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        startRepeatingTask();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopRepeatingTask();
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

}
