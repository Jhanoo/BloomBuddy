package com.example.bloombuddy.form;

import com.google.gson.annotations.SerializedName;

public class ImgResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
