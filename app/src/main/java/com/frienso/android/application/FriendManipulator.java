package com.frienso.android.application;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.frienso.adapters.IncomingFriendsListViewAdapter;
import com.frienso.adapters.OutgoingFriendsListViewAdapter;
import com.frienso.android.application.R;
import com.frienso.helper.FriendIncoming;
import com.frienso.helper.FriendOutgoing;
import com.frienso.helper.FriendsHelper;

import java.util.ArrayList;

public class FriendManipulator extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_manipulator);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friend_manipulator, menu);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //incoming Friends
        IncomingFriendsListViewAdapter incomingAdapter = new IncomingFriendsListViewAdapter(this,
                (FriendsHelper.sFriendIncoming).toArray(new FriendIncoming[FriendsHelper.sFriendIncoming.size()]));
        ListView incomingList = (ListView) findViewById(R.id.listViewInFriends);
        incomingList.setAdapter(incomingAdapter);

        //outGoingFriends
        //TODO: make sure the Adapter has atleast 3 elements. This is equal to the number of tracking
        // friends we want to support
        OutgoingFriendsListViewAdapter outgoingAdapter = new OutgoingFriendsListViewAdapter(this,
                (FriendsHelper.sFriendOutgoing).toArray(new FriendOutgoing[FriendsHelper.sFriendOutgoing.size()]));
        ListView outgoingList = (ListView) findViewById(R.id.listViewOutFriends);
        outgoingList.setAdapter(outgoingAdapter);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
