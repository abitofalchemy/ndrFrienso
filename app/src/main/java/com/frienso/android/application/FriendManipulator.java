package com.frienso.android.application;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.frienso.adapters.IncomingFriendsListViewAdapter;
import com.frienso.adapters.OutgoingFriendsListViewAdapter;
import com.frienso.helper.FriendIncoming;
import com.frienso.helper.FriendOutgoing;
import com.frienso.helper.FriendsHelper;



public class FriendManipulator extends Activity {
    private static final String LOG_TAG = "FriendManipulator";
    Context mContext;
    FriendOutgoing[] arrayOfFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_manipulator);
        this.mContext = this;
        int numFriendsOutGoing = this.getResources().getInteger(R.integer.numberOutgoingFriends);

        //outGoingFriends
        // friends we want to support

        arrayOfFriends = (FriendsHelper.sFriendOutgoing).toArray(new FriendOutgoing[numFriendsOutGoing]);
        if(FriendsHelper.sFriendOutgoing.size() != numFriendsOutGoing) {
            //create dummy friends
            for (int i = FriendsHelper.sFriendOutgoing.size(); i< numFriendsOutGoing; i ++) {
                arrayOfFriends[i] = new FriendOutgoing();
            }
        }

        OutgoingFriendsListViewAdapter outgoingAdapter = new OutgoingFriendsListViewAdapter(this,arrayOfFriends);
        ListView outgoingList = (ListView) findViewById(R.id.listViewOutFriends);
        outgoingList.setAdapter(outgoingAdapter);
        outgoingList.setLongClickable(true);
        outgoingList.setOnItemLongClickListener(mOutgoingLongClickViewListener);


        //incoming Friends
        IncomingFriendsListViewAdapter incomingAdapter = new IncomingFriendsListViewAdapter(this,
                (FriendsHelper.sFriendIncoming).toArray(new FriendIncoming[FriendsHelper.sFriendIncoming.size()]));
        ListView incomingList = (ListView) findViewById(R.id.listViewInFriends);
        incomingList.setAdapter(incomingAdapter);
        incomingList.setOnItemLongClickListener(mIncomingLongClickViewListener);


        Toast.makeText(this,R.string.longPressHint,Toast.LENGTH_LONG).show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friend_manipulator, menu);
        getActionBar().setDisplayHomeAsUpEnabled(true);       
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



    AdapterView.OnItemLongClickListener mIncomingLongClickViewListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.modifyFriendsAlertDialogTitle);

            builder.setItems(R.array.inFriendOperations, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(LOG_TAG, " Option " + which + "Clicked " + mContext.getResources().getStringArray(R.array.inFriendOperations));
                }
            });

            builder.setNegativeButton(R.string.cancelFriendModification, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(LOG_TAG, "Friend Modification operation cancelled");

                }
            });
            builder.show();
            return false;
        }
    };


    AdapterView.OnItemLongClickListener mOutgoingLongClickViewListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.modifyFriendsAlertDialogTitle);


            int optionArray;
            // if this is a dummy place holder
            if (arrayOfFriends[position].isDummy()){
                optionArray = R.array.onlyAddFriendOperation;

            } else {
                optionArray = R.array.outFriendOperations;
            }
            builder.setItems(optionArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(LOG_TAG, " Option " + which + "Clicked " + R.array.outFriendOperations);
                }
            });

            builder.setNegativeButton(R.string.cancelFriendModification, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(LOG_TAG, "Friend Modification operation cancelled");

                }
            });
            builder.show();
            return false;
        }
    };
}
