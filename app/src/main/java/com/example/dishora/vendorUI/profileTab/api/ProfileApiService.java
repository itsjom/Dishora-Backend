package com.example.dishora.vendorUI.profileTab.api;

import com.example.dishora.vendorUI.profileTab.model.BusinessProfile;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ProfileApiService {
    @GET("business/{id}")
    Call<BusinessProfile> getBusinessDetails(@Path("id") long businessId);
}
