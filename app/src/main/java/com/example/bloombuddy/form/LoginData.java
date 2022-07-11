package com.example.bloombuddy.form;

import com.google.gson.annotations.SerializedName;

public class LoginData {
    @SerializedName("userid")
    String userid;

    @SerializedName("userPwd")
    String userPwd;

    @SerializedName("nickname")
    String nickname;

    @SerializedName("apiType")
    String apiType;

    @SerializedName("apiToken")
    private String apiToken;

    public LoginData(String userid, String userPwd, String nickname, String apiType, String apiToken) {
        this.userid = userid;
        this.userPwd = userPwd;
        this.nickname = nickname;
        this.apiType = apiType;
        this.apiToken = apiToken;
    }
}
