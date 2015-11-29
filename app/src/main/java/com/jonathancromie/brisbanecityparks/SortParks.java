package com.jonathancromie.brisbanecityparks;

import com.google.android.gms.maps.model.LatLng;

import java.util.Comparator;

/**
 * Created by Jonathan on 11/29/2015.
 */
public class SortParks implements Comparator<Parks> {

    LatLng currentLocation;

    public SortParks(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }

    @Override
    public int compare(final Parks park1, final Parks park2) {
        double lat1 = Double.parseDouble(park1.latitude);
        double lon1 = Double.parseDouble(park1.longitude);
        double lat2 = Double.parseDouble(park2.latitude);
        double lon2 = Double.parseDouble(park2.longitude);

        double distanceToPark1 = distance(currentLocation.latitude, currentLocation.longitude, lat1, lon1);
        double distanceToPark2 = distance(currentLocation.latitude, currentLocation.longitude, lat2, lon2);
        return (int) (distanceToPark1 - distanceToPark2);
    }

    public double distance(double fromLat, double fromLon, double toLat, double toLon) {
        double radius = 6378137;   // approximate Earth radius, *in meters*
        double deltaLat = toLat - fromLat;
        double deltaLon = toLon - fromLon;
        double angle = 2 * Math.asin( Math.sqrt(
                Math.pow(Math.sin(deltaLat/2), 2) +
                        Math.cos(fromLat) * Math.cos(toLat) *
                                Math.pow(Math.sin(deltaLon/2), 2) ) );
        return radius * angle;
    }
}
