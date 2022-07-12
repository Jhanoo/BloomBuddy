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


    public LoginData(String userid, String userPwd, String nickname, String apiType) {
        this.userid = userid;
        this.userPwd = userPwd;
        this.nickname = nickname;
        this.apiType = apiType;
    }
}
