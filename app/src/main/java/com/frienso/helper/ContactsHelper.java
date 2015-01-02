package com.frienso.helper;

import android.content.ContentResolver;
import android.content.ContentUris;
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

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, projection, null, null, null);
        if (cursor == null) {
            Log.e(LOG_TAG,"Cursor is null");
            return null;
        }
        String contactName = null;
        String photoUri;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI));

            Log.v(LOG_TAG, "Started uploadcontactphoto: Contact Found @ " + contactName);
            Log.v(LOG_TAG, "Started uploadcontactphoto: Contact Found @ " + phoneNumber);
            Log.v(LOG_TAG, "Started uploadcontactphoto: Contact name  = " + photoUri);

        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }
}
