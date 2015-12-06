package com.jonathancromie.brisbanecityparks;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.SphericalUtil;

import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by Jonathan on 11/28/2015.
 */
public class Park {
    private int id;

    @SerializedName("park_code")
    String park_code;

    @SerializedName("name")
    String name;

    @SerializedName("street")
    String street;

    @SerializedName("suburb")
    String suburb;

    @SerializedName("easting")
    String easting;

    @SerializedName("northing")
    String northing;

    double latitude;

    double longitude;

    double distance;

    @SerializedName("reviews")
    private Review[] reviews;

    public Park() {
        setReviews(new Review[0]);
    }

    public Park(int id, String park_code, String name, String street, String suburb, String easting, String northing, double latitude, double longitude, double distance) {
        this.id = id;
        this.park_code = park_code;
        this.name = name;
        this.street = street;
        this.suburb = suburb;
        this.easting = easting;
        this.northing = northing;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPark_code() {
        return park_code;
    }

    public void setPark_code(String park_code) {
        this.park_code = park_code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public String getEasting() {
        return easting;
    }

    public void setEasting(String easting) {
        this.easting = easting;
    }

    public String getNorthing() {
        return northing;
    }

    public void setNorthing(String northing) {
        this.northing = northing;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Review[] getReviews() {
        return reviews;
    }

    public void setReviews(Review[] reviews) {
        this.reviews = reviews;
    }
}
