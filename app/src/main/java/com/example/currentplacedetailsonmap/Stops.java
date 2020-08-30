package com.example.currentplacedetailsonmap;

import com.google.android.gms.maps.model.LatLng;

public class Stops {


    String name;
    LatLng stopLoc;
    double mdistance;

    public Stops(String name) {

        this.name = name;

    }

    public void setStopLoc( LatLng latLng ) {
        stopLoc=latLng;
    }
    public double getStopLocLat( ) {

        return stopLoc.latitude;
    }

    public double getStopLocLon( ) {

        return stopLoc.longitude;
    }

    public void setDistance( double length ) {
        mdistance=length;
    }

    public double getDistance( ) {

        return mdistance;
    }

}
