package com.frienso.helper;

import android.content.Context;

import com.frienso.android.application.R;
import com.parse.ParseUser;

/**
 * Created by Udayan Kumar on 12/21/14.
 */
public abstract class Friend {
    private String phoneNumber;
    private String lname;
    private String fname;
    private ParseUser pu;
    private String contactPicURI;

    public Friend(ParseUser pu, String phoneNumber, String lname, String fname) {
        this.lname = lname;
        this.fname = fname;
        this.phoneNumber = phoneNumber;
        this.pu = pu;
        contactPicURI = null;
    }
    public Friend () {
        this.lname = null;
        this.fname = null;
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

    /* Since contacts api do not provide first name and last name,
    we are only storing name info in fname. May be we can remove lname.
     */

    public void setFName(String fname) {
        this.fname = fname;
    }



    public void loadContactInfo(Context context) {
        if(fname != null && lname != null)
             return;
        ContactsHelper.getContactName(context,this);
    }

    public String getNumber() {
        return phoneNumber;
    }

    public ParseUser getParseUser() { return pu;};

    public String getFullName(Context context) {
        if(fname != null && lname!= null) {
            return fname + " " + lname;
        } else if(fname == null && lname == null) {
            return context.getString(R.string.contactNameNotFound);
        } else {
            //atleast one of them is null
            return fname == null?lname:fname;
        }
    }

}
