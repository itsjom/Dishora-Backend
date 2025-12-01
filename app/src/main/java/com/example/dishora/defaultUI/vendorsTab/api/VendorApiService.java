package com.example.dishora.defaultUI.vendorsTab.api;

import com.example.dishora.defaultUI.vendorsTab.model.Vendor;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface VendorApiService {
    @GET("vendors")  // maps to GET /api/vendors
    Call<List<Vendor>> getVendors();
}
