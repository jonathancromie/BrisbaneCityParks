package com.jonathancromie.brisbanecityparks;

import com.google.

/**
 * Created by Jonathan on 11/28/2015.
 */
public class Parks {
    /**
     * Item Id
     */
    @com.google.gson.annotations.SerializedName("id")
    String id;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("park_code")
    String code;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("name")
    String name;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("street")
    String street;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("suburb")
    String suburb;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("easting")
    String easting;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("northing")
    String northing;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("latitude")
    String latitude;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("longitude")
    String longitude;

    public Parks(String id, String code, String name, String street, String suburb, String easting, String northing, String latitude, String longitude) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.street = street;
        this.suburb = suburb;
        this.easting = easting;
        this.northing = northing;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private String getDistance(String latitude, String longitude) {

    }
}
