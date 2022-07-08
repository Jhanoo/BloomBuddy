package com.example.bloombuddy.form;

import com.google.gson.annotations.SerializedName;

public class LoginData {
    @SerializedName("nickname")
    String nickname;

    @SerializedName("userPwd")
    String userPwd;

    public LoginData(String nickname, String userPwd) {
        this.nickname = nickname;
        this.userPwd = userPwd;
    }
}
