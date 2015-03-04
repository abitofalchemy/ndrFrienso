package com.frienso.android.application;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.frienso.android.adapters.IncomingFriendsListViewAdapter;
import com.frienso.android.adapters.OutgoingFriendsListViewAdapter;
import com.frienso.android.helper.Friend;
import com.frienso.android.helper.FriendIncoming;
import com.frienso.android.helper.FriendOutgoing;
import com.frienso.android.helper.FriendsHelper;
import com.frienso.android.utils.PhoneNumbers;

import java.util.ArrayList;


public class FriendManipulator extends Activity implements Friend.OperationComplete{
    private static final String LOG_TAG = "FriendManipulator";
    private static final int PICK_CONTACT_REQUEST = 1;

    private int positionOfInterest = -1;
    Context mContext;
    ArrayList<FriendOutgoing> arrayOfFriends;
    OutgoingFriendsListViewAdapter outgoingAdapter;
    IncomingFriendsListViewAdapter incomingAdapter;
    ProgressDialog waitDialog;
    int mNumFriendsOutGoing;

    public enum FriendOptions {
        ADD, REMOVE, BLOCK, UNBLOCK, REPLACE, INVITE
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_manipulator);
        this.mContext = this;
        mNumFriendsOutGoing = this.getResources().getInteger(R.integer.numberOutgoingFriends);
        arrayOfFriends = new ArrayList<FriendOutgoing>(mNumFriendsOutGoing);
        //outGoingFriends
        // friends we want to support
        loadOutgoingFriends();


        outgoingAdapter = new OutgoingFriendsListViewAdapter(this,arrayOfFriends);
        ListView outgoingList = (ListView) findViewById(R.id.listViewOutFriends);
        outgoingList.setAdapter(outgoingAdapter);
        outgoingList.setLongClickable(true);
        outgoingList.setOnItemLongClickListener(mOutgoingLongClickViewListener);




        //incoming Friends
        incomingAdapter = new IncomingFriendsListViewAdapter(this,
                (FriendsHelper.sFriendIncoming).toArray(new FriendIncoming[FriendsHelper.sFriendIncoming.size()]));
        ListView incomingList = (ListView) findViewById(R.id.listViewInFriends);
        incomingList.setAdapter(incomingAdapter);
        incomingList.setOnItemLongClickListener(mIncomingLongClickViewListener);


        Toast.makeText(this,R.string.longPressHint,Toast.LENGTH_LONG).show();

    }


    private void  loadOutgoingFriends() {
        int size = arrayOfFriends.size();
        for (int i = 0 ; i < size; i ++) {
            arrayOfFriends.remove(0);
        }
        for (int i = 0 ; i < FriendsHelper.sFriendOutgoing.size() ;i++){
            arrayOfFriends.add(FriendsHelper.sFriendOutgoing.get(i));
        }
        if(FriendsHelper.sFriendOutgoing.size() < mNumFriendsOutGoing) {
            //create dummy friends
            for (int i = FriendsHelper.sFriendOutgoing.size(); i< mNumFriendsOutGoing; i ++) {
                arrayOfFriends.add(new FriendOutgoing());
            }
        }
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
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.modifyFriendsAlertDialogTitle);


            final int optionArray;
            //this array should be increased if adding more than two options in
            // dialog box.
            final FriendOptions friendOptions [] = new FriendOptions[3];
            // if this is a dummy place holder
            if (arrayOfFriends.get(position).isDummy()){
                optionArray = R.array.onlyAddFriendOperation;
                friendOptions[0]=FriendOptions.ADD;

            } else if (!arrayOfFriends.get(position).isOnFrienso()) {

                //for users added but not on Frienso
                optionArray = R.array.onlyInviteFriendOperation;
                friendOptions[0]=FriendOptions.INVITE;
                friendOptions[1]=FriendOptions.REMOVE;
            }
            else {
                    //order should be Remove and then Replace
                    optionArray = R.array.outFriendOperations;
                    friendOptions[0]=FriendOptions.REMOVE;
            }

            builder.setItems(optionArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //check which array we are using
                    String optionStrings [] = mContext.getResources().getStringArray(optionArray);
                    String selectedOption = optionStrings[which];
                    Log.i(LOG_TAG, " Option " + which + "Clicked " + selectedOption);
                    positionOfInterest = -1;
                    switch(friendOptions[which]){
                        case ADD:
                            Log.i(LOG_TAG, " Add clicked for position" + position);

                            addFriend (position);
                            break;
                        case REMOVE:
                            Log.i(LOG_TAG, " Remove clicked for position" + position );
                            // remove from parse
                            // if successful remove from friends list
                            removeFriend(position);
                            break;
                        case INVITE:
                            //Allow users to pick a communication medium and send a pre drafted message
                            break;

                    }
                    //get the name of the option
                    //call the relevant function
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

    boolean addFriend(int position) {
        //pick a contact
        //check in parse
        //Add to user list if on parse
        //refresh friends list
        positionOfInterest = position;
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
        return true;
    }

    boolean removeFriend(int position) {
        showProgressBar();
        //delete
        FriendsHelper.deleteFriend(arrayOfFriends.get(position).getNumber(),this);
        return true;
    }

    private void showProgressBar() {

        waitDialog = ProgressDialog.show(this, "", getString(R.string.waitingForAction), true);
    }

    private void hideProgressBar(){

        waitDialog.dismiss();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact
                Uri contactUri = data.getData();
                // We only need the NUMBER column, because there will be only one row in the result
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

                // Perform the query on the contact to get the NUMBER column
                // We don't need a selection or sort order (there's only one result for the given URI)
                // TODO: CAUTION: The query() method should be called from a separate thread to avoid blocking
                // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
                // Consider using CursorLoader to perform the query.
                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();

                // Retrieve the phone number from the NUMBER column
                int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(column);
                column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String name = cursor.getString(column);
                showProgressBar();
                // now remove dummy at position of interest and add another friend ..
                // What should we do if the user is not on parse?
                // Should we only show user that are on parse?
                FriendsHelper.addFriend(PhoneNumbers.giveStandardizedPhoneNumber(number),name,this);
            }
        }
    }



    @Override
    public void friendOperationComplete(Friend.FriendOperationResult val) {
        //now check the val if error - display it to the user, else refresh the display notify Datachange
        Log.i(LOG_TAG,"Results of adding a friend are " + val.toString());
        //Reload the friends list.

        //Start an async task and in the done box.. cancel the progress bar and notify the data dirty
        new refreshFriends().execute();


    }

    class refreshFriends extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            FriendsHelper.refreshFriends(mContext);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadOutgoingFriends();
            outgoingAdapter.notifyDataSetChanged();
            //Then hide the progress bar
            hideProgressBar();

        }
    }
}
