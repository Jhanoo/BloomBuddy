package com.example.bloombuddy.network;

import com.example.bloombuddy.form.JoinData;
import com.example.bloombuddy.form.JoinResponse;
import com.example.bloombuddy.form.LoginData;
import com.example.bloombuddy.form.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ServiceApi {
    @POST("/user/login")
    Call<LoginResponse> userLogin(@Body LoginData data);

    @POST("/user/join")
    Call<JoinResponse> userJoin(@Body JoinData data);
}