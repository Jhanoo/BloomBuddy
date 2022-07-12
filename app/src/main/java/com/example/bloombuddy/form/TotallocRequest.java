package com.example.bloombuddy.form;

import com.google.gson.annotations.SerializedName;

public class TotallocRequest {
    @SerializedName("userid")
    String userid;

    public TotallocRequest(String userid) {
        this.userid = userid;
    }
}

