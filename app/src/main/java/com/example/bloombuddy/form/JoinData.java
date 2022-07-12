package com.example.bloombuddy.form;

import com.google.gson.annotations.SerializedName;

public class JoinData {
    @SerializedName("userid")
    private String userid;

    @SerializedName("userPwd")
    private String userPwd;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("username")
    private String username;

    @SerializedName("apiType")
    private String apiType;

    public JoinData(String userid, String userPwd, String nickname, String username, String apiType) {
        this.userid = userid;
        this.nickname = nickname;
        this.userPwd = userPwd;
        this.username = username;
        this.apiType = apiType;
    }
}