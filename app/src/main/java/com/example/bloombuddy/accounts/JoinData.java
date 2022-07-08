package com.example.bloombuddy.accounts;

import com.google.gson.annotations.SerializedName;

public class JoinData {
    @SerializedName("nickname")
    private String nickname;

    @SerializedName("userPwd")
    private String userPwd;

    @SerializedName("apiToken")
    private String apiToken;

    public JoinData(String nickname, String userPwd, String apiToken) {
        this.nickname = nickname;
        this.userPwd = userPwd;
        this.apiToken = apiToken;
    }
}