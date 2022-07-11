package com.example.bloombuddy.form;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    /*code가 327이면 api로그인 첫 시도, 프로필 이미지 보내줘야함*/
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}