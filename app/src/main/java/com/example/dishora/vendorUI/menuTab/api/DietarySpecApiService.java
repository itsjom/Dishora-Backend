package com.example.dishora.vendorUI.menuTab.api;

import com.example.dishora.vendorUI.menuTab.api.responseModel.DietarySpecResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DietarySpecApiService {
    @GET("dietaryspecifications")
    Call<List<DietarySpecResponse>> getDietarySpecs();
}
