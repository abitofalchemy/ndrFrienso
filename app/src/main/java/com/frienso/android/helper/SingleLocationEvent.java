package com.frienso.android.helper;

/**
 * Created by udayan Kumar on 11/23/14.
 */
public class SingleLocationEvent {
    public double latitude;
    public double longitude;
    public int accuracyInM;
    public long timeStampInMillis;

    SingleLocationEvent(double latitude, double longitude, int accuracy, long time){
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracyInM = accuracy;
        this.timeStampInMillis = time;
    }
}
