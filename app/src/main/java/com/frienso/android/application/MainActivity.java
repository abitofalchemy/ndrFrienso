package com.frienso.android.application;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.frienso.android.application.R;
import com.frienso.helper.EventHelper;
import com.frienso.helper.FriendIncoming;
import com.frienso.helper.FriendsHelper;
import com.frienso.utils.Network;

import java.util.ArrayList;

public class MainActivity extends Activity implements FriendsHelper.FriendsUpdated, EventHelper.EventsUpdated {

    Menu mMenu;
    private final String LOGTAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check network connectivity
        if(!Network.hasInternet(this)) {
            Toast.makeText(this,R.string.no_internet,Toast.LENGTH_LONG).show();
            return;
        }

        FriendsHelper.refreshFriends();


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
        a.add("loading data");
        setSpinnerMenu(a);
        return true;
    }


    public void setSpinnerMenu(ArrayList<String> options) {
        MenuItem item = mMenu.findItem(R.id.mainspinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, options);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setAdapter(spinnerArrayAdapter);
        //spinner.setOnItemClickListener(onSpinnerItemSelectedListener);
    }

    public void onSpinnerItemSelectedListener() {

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

    @Override
    public void loadFriendsAgain() {
        //Reload all the layouts that have friends data
        Log.i(LOGTAG,"Friends reloaded");
        ArrayList<String> a = new ArrayList<String>();
        a.add("Select");
        for (FriendIncoming fi: FriendsHelper.sFriendIncoming) {
            //TODO: change numbers to name from contacts.
            a.add(fi.getNumber());
        }
        a.add("");
        a.add("+/- Friends");
        setSpinnerMenu(a);

        //now update the events
        EventHelper.refreshActiveEvents();
    }

    @Override
    public void loadActiveEventsAgain() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        FriendsHelper.registerUpdateCallBack(this);
        EventHelper.registerUpdateCallBack(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FriendsHelper.unregisterUpdateCallBack(this);
        EventHelper.unregisterUpdateCallBack(this);

    }
}
