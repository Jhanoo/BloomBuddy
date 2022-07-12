package com.example.bloombuddy.form;

import com.google.gson.annotations.SerializedName;

public class Locationdata {
    @SerializedName("userid")
    String userid;

    @SerializedName("longitude")
    String longitude;

    @SerializedName("latitude")
    String latitude;


    public Locationdata(String userid, String longitude, String latitude) {
        this.userid = userid;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
