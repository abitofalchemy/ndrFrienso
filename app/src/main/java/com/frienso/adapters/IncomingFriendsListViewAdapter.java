package com.frienso.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.frienso.android.application.R;
import com.frienso.helper.ContactsHelper;
import com.frienso.helper.FriendIncoming;
import com.frienso.helper.FriendsHelper;

/**
 * Created by Udayan Kumar on 12/20/14.
 **/
public class IncomingFriendsListViewAdapter extends ArrayAdapter<FriendIncoming>{

    Context mContext;
    private final FriendIncoming[] values;

    public IncomingFriendsListViewAdapter(Context context, FriendIncoming[] values) {
        super(context,R.layout.in_friend_list_layout, values);
        this.mContext = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.in_friend_list_layout, parent, false);
        TextView firstLine = (TextView) rowView.findViewById(R.id.inFriendFirstLine);
        TextView secondLine = (TextView) rowView.findViewById(R.id.inFriendSecondLine);
        firstLine.setText(values[position].getFullName(mContext));
        secondLine.setText(values[position].getNumber());
        //TODO: Contacts should not be read here. move this to somewhere else
        ContactsHelper.getContactName(mContext,values[position].getNumber());
        ImageView blockIcon = (ImageView) rowView.findViewById(R.id.inFriendBlockIcon);
        blockIcon.setOnClickListener(mBlockUserListener);
        return rowView;
    }


    View.OnClickListener mBlockUserListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {

            TextView secondLine = (TextView) v.findViewById(R.id.inFriendSecondLine);
            String phoneNumber = secondLine.getText().toString();
            FriendsHelper.blockFriend(phoneNumber);
        }
    };
}
