package com.example.bloombuddy.form;

import com.google.gson.annotations.SerializedName;

public class ImgData {
    @SerializedName("userid")
    private String userid;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("username")
    private String username;

    @SerializedName("apiType")
    private String apiType;

    @SerializedName("apiToken")
    private String apiToken;

    public ImgData(String userid, String nickname, String username, String apiType, String apiToken) {
        this.userid = userid;
        this.nickname = nickname;
        this.username = username;
        this.apiType = apiType;
        this.apiToken = apiToken;
    }
}
