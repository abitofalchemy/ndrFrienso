package com.frienso.android.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.frienso.android.application.R;
import com.frienso.android.helper.FriendIncoming;

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
        ImageView imageView = (ImageView) rowView.findViewById(R.id.inFriendPhotoIcon);

        if (values[position].getFullName(mContext) == null) {
            firstLine.setText(mContext.getString(R.string.contactNameNotFound));
        } else {
            firstLine.setText(values[position].getFullName(mContext));
        }
        secondLine.setText(values[position].getNumber());

        String imageUri = values[position].getPicURI();
        if(imageUri != null) {
            imageView.setImageURI(Uri.parse(imageUri));
        }
        return rowView;
    }



}
