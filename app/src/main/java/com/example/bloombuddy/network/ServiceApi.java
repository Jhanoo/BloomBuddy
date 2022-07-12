package com.example.bloombuddy.network;

import com.example.bloombuddy.form.ImgResponse;
import com.example.bloombuddy.form.JoinData;
import com.example.bloombuddy.form.JoinResponse;
import com.example.bloombuddy.form.LoginData;
import com.example.bloombuddy.form.LoginResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ServiceApi {
    @POST("/user/login")
    Call<LoginResponse> userLogin(@Body LoginData data);

    @POST("/user/join")
    Call<JoinResponse> userJoin(@Body JoinData data);

    @Multipart
    @POST("/upload")
    Call<ImgResponse> sendImg(@Part MultipartBody.Part file);
}