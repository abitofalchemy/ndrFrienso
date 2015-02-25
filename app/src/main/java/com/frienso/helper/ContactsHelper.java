package com.frienso.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Created by Udayan Kumar on 12/21/14.
 */
public class ContactsHelper {

    private final static String[] projection = new String[] {
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI};

    private final static String LOG_TAG = "ContactsHelper";

    public static void getContactName(Context context, Friend friend) {
        String phoneNumber = friend.getNumber();
        if (phoneNumber == null) {
            return;
        }
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, projection, null, null, null);
        if (cursor == null) {
            Log.e(LOG_TAG,"Cursor is null");
            return;
        }
        String contactName = null;
        String photoUri = null ;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI));
            //set the links back to the friend object.
            friend.setContactPicURI(photoUri);
            friend.setName(contactName);

            Log.v(LOG_TAG, "Started uploadcontactphoto: Contact Found @ " + contactName);
            Log.v(LOG_TAG, "Started uploadcontactphoto: Contact Found @ " + phoneNumber);
            Log.v(LOG_TAG, "Started uploadcontactphoto: Contact name  = " + photoUri);

        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}
