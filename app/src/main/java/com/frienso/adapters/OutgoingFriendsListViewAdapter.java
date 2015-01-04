package com.frienso.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.frienso.android.application.R;
import com.frienso.helper.FriendIncoming;
import com.frienso.helper.FriendOutgoing;
import com.frienso.helper.FriendsHelper;

import java.net.URI;

/**
 * Created by Udayan Kumar on 12/20/14.
 **/
public class OutgoingFriendsListViewAdapter extends ArrayAdapter<FriendOutgoing>{

    private static final String LOG_TAG = "OutgoingFriendsListViewAdapter";
    Context mContext;
    private final FriendOutgoing[] values;

    public OutgoingFriendsListViewAdapter(Context context, FriendOutgoing[] values) {
        super(context,R.layout.in_friend_list_layout, values);
        this.mContext = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.out_friend_list_layout, parent, false);
        TextView firstLine = (TextView) rowView.findViewById(R.id.outFriendFirstLine);
        TextView secondLine = (TextView) rowView.findViewById(R.id.outFriendSecondLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.outFriendPhotoIcon);

        firstLine.setText(values[position].getFullName(mContext));
        secondLine.setText(values[position].getNumber());
        String imageUri = values[position].getPicURI();
        if(imageUri != null) {
            imageView.setImageURI(Uri.parse(imageUri));
        }
        ImageView removeIcon = (ImageView) rowView.findViewById(R.id.outFriendRemoveIcon);
        removeIcon.setOnClickListener(mBlockUserListener);
        removeIcon.setTag(secondLine);
        return rowView;
    }


    View.OnClickListener mBlockUserListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            TextView secondLine = (TextView) v.getTag();
            String phoneNumber = secondLine.getText().toString();
            FriendsHelper.deleteFriend(phoneNumber);
        }
    };
}
