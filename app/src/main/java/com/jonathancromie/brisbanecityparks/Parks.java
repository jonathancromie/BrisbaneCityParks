package com.jonathancromie.brisbanecityparks;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.text.NumberFormat;

/**
 * Created by Jonathan on 11/28/2015.
 */
public class Parks {
    int id;
    String code;
    String name;
    String street;
    String suburb;
    String easting;
    String northing;
    double latitude;
    double longitude;
    double distance;

    public Parks(int id, String code, String name, String street, String suburb, String easting, String northing, double latitude, double longitude, double distance) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.street = street;
        this.suburb = suburb;
        this.easting = easting;
        this.northing = northing;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }


}
