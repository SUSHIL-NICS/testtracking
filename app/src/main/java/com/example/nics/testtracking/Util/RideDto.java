package com.example.nics.testtracking.Util;

import java.io.Serializable;

/**
 * Created by Savithri on 20-07-2017.
 */

public class RideDto implements Serializable {

    private String latitude;
    private String longitude;
    private String rideStartDate;
    private String rideEndDate;
    private String distance;
    private String time;


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }



    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getRideStartDate() {
        return rideStartDate;
    }

    public void setRideStartDate(String rideStartDate) {
        this.rideStartDate = rideStartDate;
    }

    public String getRideEndDate() {
        return rideEndDate;
    }

    public void setRideEndDate(String rideEndDate) {
        this.rideEndDate = rideEndDate;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }



}
