package com.frienso.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.frienso.android.application.R;
import com.frienso.helper.FriendOutgoing;

import java.util.ArrayList;

/**
 * Created by Udayan Kumar on 12/20/14.
 **/
public class OutgoingFriendsListViewAdapter extends ArrayAdapter<FriendOutgoing>{

    private static final String LOG_TAG = "OutgoingFriendsListViewAdapter";
    Context mContext;
    private final ArrayList<FriendOutgoing> values;

    public OutgoingFriendsListViewAdapter(Context context, ArrayList <FriendOutgoing> values) {
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
        TextView userNotOnFriensoLine = (TextView) rowView.findViewById(R.id.outFriendNotOnFrienso);

        //if the friend is dummy placeholder
        if (values.get(position).isDummy()) {
            firstLine.setText(mContext.getResources().getString(R.string.pressRowToAddFriend));
            secondLine.setText("");
            userNotOnFriensoLine.setVisibility(View.INVISIBLE);
        } else {
            firstLine.setText(values.get(position).getFullName(mContext));
            secondLine.setText(values.get(position).getNumber());
            String imageUri = values.get(position).getPicURI();
            if (imageUri != null) {
                imageView.setImageURI(Uri.parse(imageUri));
            }

            //remove the notice that the user is not on frienso
            if(values.get(position).isOnFrienso()) {
                userNotOnFriensoLine.setVisibility(View.INVISIBLE);
            }
        }
        return rowView;
    }


}
