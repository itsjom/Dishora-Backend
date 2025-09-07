package com.example.dishora.network;

import com.example.dishora.defaultUI.homeTab.search.model.SearchResultItem;
import com.example.dishora.models.LoginRequest;
import com.example.dishora.models.LoginResponse;
import com.example.dishora.models.RegisterRequest;
import com.example.dishora.models.RegisterResponse;
import com.example.dishora.models.Users;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/users/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("api/users/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);
}
