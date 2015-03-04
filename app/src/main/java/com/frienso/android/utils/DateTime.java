package com.frienso.android.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by udayan kumar on 11/23/14.
 */
public class DateTime {

    private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static String getISO8601StringForCurrentDate() {
        Date now = new Date();
        return getISO8601StringForDate(now);
    }

    private  static String getISO8601StringForDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    public static String getISO8601StringForTimeStampInMillis(long tsInMillis) {
        Date dt = new Date(tsInMillis);
        return getISO8601StringForDate(dt);
    }

    public static long getTimeStampinMillisForISO8601String(String date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date d = dateFormat.parse(date);
        return d.getTime();
    }



}
