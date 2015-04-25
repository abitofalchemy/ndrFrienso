package com.frienso.android.helper;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.frienso.android.application.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Udayan Kumar on 12/15/14.
 */
public class MapHelper {

    private final static int POLYLINE_COLOR = Color.BLUE;
    private final static float POLYLINE_WIDTH = 5.0f;
    private static final String LOG_TAG = "MapHelper" ;
    //TODO: these should be set depending on the resolution of the phone screen
    public static int widthBoundingBox = 150;
    public static int heightBoundingBox = 150;
    public static int paddingBoundingBox = 0;


    public static void showOnMap(Context context, String mUsernameOnMap, GoogleMap googleMap) {
        // Steps
        //1. find the user from the set of Events
        //2. take his locations and create a polyLine
        //3. Set the correct Zoom Level
        //TODO: Throw exceptions in the case a user is not found
        ArrayList<SingleLocationEvent> locations = getUserLocationArray(mUsernameOnMap);
        if(locations == null)
            return;
        googleMap.clear();
        //TODO: for better memory efficiency we should avoid using clear and reset the data in the map objects.
        PolylineOptions plo = createPolyline(locations);
        // Get back the mutable Polyline
        Polyline polyline = googleMap.addPolyline(plo);
        LatLngBounds newbounds = getNewLatLngBounds(plo.getPoints());
        if (newbounds == null){
            //TODO: we should show a message to the user that we are still waiting
            // for the first location coordinate
            return;
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(newbounds,
                widthBoundingBox, heightBoundingBox, paddingBoundingBox));
        //remove existing markers

        //Start Marker
        googleMap.addMarker(new MarkerOptions()
                .position(plo.getPoints().get(0))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(mUsernameOnMap)
                .snippet(context.getString(R.string.tracking_start_location)));

        //End Marker
        googleMap.addMarker(new MarkerOptions()
                .position(plo.getPoints().get(plo.getPoints().size() - 1))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(mUsernameOnMap)
                .snippet(context.getString(R.string.tracking_last_known_position)));
    }


    /*
    gets the latitude and longitude bounds for the given set of points
     */
    private static LatLngBounds getNewLatLngBounds(List<LatLng> allPoints) {
        if (allPoints.size() >0) {
            LatLng firstPoint = allPoints.get(0);
            double westMostLongitude = firstPoint.longitude;
            double eastMostLongitude = firstPoint.longitude;
            double northMostLatitude = firstPoint.latitude;
            double southMostLatitude = firstPoint.latitude;
            for (LatLng onePoint : allPoints) {
                westMostLongitude = onePoint.longitude < westMostLongitude ? onePoint.longitude : westMostLongitude;
                eastMostLongitude = onePoint.longitude > eastMostLongitude ? onePoint.longitude : eastMostLongitude;
                northMostLatitude = onePoint.latitude > northMostLatitude ? onePoint.latitude : northMostLatitude;
                southMostLatitude = onePoint.latitude < southMostLatitude ? onePoint.latitude : southMostLatitude;
            }
            //  Log.i(LOG_TAG,"SouthWest Tip" + southMostLatitude + "," + westMostLongitude );
            //  Log.i(LOG_TAG,"NorthEast Tip" + northMostLatitude + "," + eastMostLongitude );

            return new LatLngBounds(new LatLng(southMostLatitude, westMostLongitude),
                    new LatLng(northMostLatitude, eastMostLongitude));
        } else {
            return null;
        }
    }


    private static PolylineOptions createPolyline(ArrayList<SingleLocationEvent> locations) {
        // Instantiates a new Polyline object and adds points to define a rectangle
        PolylineOptions rectOptions = new PolylineOptions();
        rectOptions.color(POLYLINE_COLOR);
        rectOptions.width(POLYLINE_WIDTH);

        for (SingleLocationEvent sle : locations){
            rectOptions.add(new LatLng(sle.latitude,sle.longitude));
        }

        return rectOptions;
    }

    private static ArrayList<SingleLocationEvent> getUserLocationArray(String mUsernameOnMap) {
        for (ActiveIncomingEvent aie: EventHelper.sActiveIncomingEvents){
            //TODO: Change this if the phone number is no longer the identity of the user
            if (aie.mUser.get("phoneNumber").toString().compareTo(mUsernameOnMap) == 0)
                return aie.mLocationArray;
        }
        return null;
    }

    public static void showOnMap(Context context, GoogleMap googleMap, float latitude, float longitude, long timestamp, float accuracy) {

        LatLng latlng = new LatLng(latitude,longitude);
        Log.d(LOG_TAG, "Lat " + latitude + " long " + longitude);
        googleMap.clear();

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,15.0f));
        googleMap.addMarker(new MarkerOptions()
                .position(latlng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(context.getString(R.string.current_location_text)));
    }
}
