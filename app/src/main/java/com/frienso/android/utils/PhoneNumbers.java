package com.frienso.android.utils;

/**
 * Created by udayan on 2/22/15.
 */
public class PhoneNumbers {

    public static String giveStandardizedPhoneNumber (String phoneNumber) {
        // for now just remove brackets and dashes
        phoneNumber = phoneNumber.replace("-","").replace("(","").replace(")","").replace(" ","");
        return phoneNumber;
    }
}
