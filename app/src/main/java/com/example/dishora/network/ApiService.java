package com.example.dishora.network;

import com.example.dishora.models.LoginRequest;
import com.example.dishora.models.LoginResponse;
import com.example.dishora.models.Users;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api/users")
    Call<List<Users>> getUsers();

    @POST("api/users")
    Call<Void> createUser(@Body Users user);

    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

}
