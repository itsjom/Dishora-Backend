package com.example.dishora.defaultUI.homeTab.vendorSection.api;

import com.example.dishora.defaultUI.homeTab.vendorSection.Vendor;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface VendorNearbyApi {
    @GET("business/nearby")
    Call<List<Vendor>> getNearbyVendors(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude
    );
}
