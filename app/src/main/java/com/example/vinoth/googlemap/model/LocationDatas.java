package com.example.vinoth.googlemap.model;

/**
 * Created by vinoth on 9/10/16.
 */

public class LocationDatas {

    private double latitude;
    private double longitude;
    private  String timeStamp;

    public LocationDatas(double latitude, double longitude, String timeStamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeStamp = timeStamp;
    }

    public LocationDatas() {
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

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
