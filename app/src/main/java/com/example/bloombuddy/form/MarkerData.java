package com.example.bloombuddy.form;

public class MarkerData {
    private String userId;
    private String profileURL;
    private Double latitude;
    private Double longitude;


    public MarkerData(String userId, String profileURL, Double latitude, Double longitude) {
        this.userId = userId;
        this.profileURL = profileURL;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileURL() {
        return profileURL;
    }

    public void setProfileURL(String profileURL) {
        this.profileURL = profileURL;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

}
