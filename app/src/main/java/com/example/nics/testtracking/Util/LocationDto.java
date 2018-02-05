package com.example.nics.testtracking.Util;

/**
 * Created by Savithri on 18-07-2017.
 */

public class LocationDto {
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

    public String getCumnTime() {
        return cumnTime;
    }

    public void setCumnTime(String cumnTime) {
        this.cumnTime = cumnTime;
    }

    private String latitude;
    private String longitude;
    private String cumnTime;

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    private String startDateTime;
    private String endDateTime;

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    private String flag;
}
