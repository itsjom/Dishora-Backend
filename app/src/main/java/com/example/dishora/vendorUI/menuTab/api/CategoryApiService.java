package com.example.dishora.vendorUI.menuTab.api;

import com.example.dishora.vendorUI.menuTab.api.responseModel.CategoryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CategoryApiService {
    @GET("categories") // ✅ adjust this to match your ASP.NET route
    Call<List<CategoryResponse>> getCategories();
}
