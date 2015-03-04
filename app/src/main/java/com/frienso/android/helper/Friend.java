package com.frienso.android.helper;

import android.content.Context;

import com.frienso.android.application.R;
import com.parse.ParseUser;

/**
 * Created by Udayan Kumar on 12/21/14.
 */
public abstract class Friend {
    protected String phoneNumber;
    protected String name;
    private ParseUser pu;
    private String contactPicURI;

    public enum FriendOperationResult {
        FAILURE, SUCCESS, NONETWORK, DUPLICATE, FriendNotFound, DUMMY
    }

    public interface OperationComplete {
        void friendOperationComplete(FriendOperationResult val);
    }

    public Friend(ParseUser pu, String phoneNumber, String name) {

        this.name = name;
        this.phoneNumber = phoneNumber;
        this.pu = pu;
        contactPicURI = null;
    }
    public Friend () {
        this.name = null;
        this.phoneNumber = null;
        this.pu = null;
        contactPicURI = null;
    }
    public void setContactPicURI(String picURI){
        this.contactPicURI = picURI;
    }

    public String getPicURI() {
        return contactPicURI;
    }



    public void setName(String name) {
        this.name = name;
    }



    public void loadContactInfo(Context context) {
        if(name != null)
             return;
        ContactsHelper.getContactName(context,this);
    }

    public String getNumber() {
        return phoneNumber;
    }

    public ParseUser getParseUser() { return pu;};

    public String getFullName(Context context) {
        if(name != null) {
            return name;
        } else {
            return context.getString(R.string.contactNameNotFound);
        }
    }

}
