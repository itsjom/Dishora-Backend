package com.example.dishora.defaultUI.vendorsTab.openingVendors.api;

import com.example.dishora.defaultUI.vendorsTab.openingVendors.model.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ProductApiService {
    // ========= Get Vendor Products (ALL, vendor-side view) ==========
    @GET("products/vendor/{businessId}")
    Call<List<Product>> getVendorProducts(@Path("businessId") long businessId);

    // ========= Get Customer Products (ONLY available ones) ==========
    @GET("products/customer/{businessId}")
    Call<List<Product>> getCustomerProducts(@Path("businessId") long businessId);
}
