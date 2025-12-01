package com.example.dishora.vendorUI.profileTab.model;

import com.google.gson.annotations.SerializedName;

public class BusinessProfile {

    // These names must match the JSON from your C# DTO
    @SerializedName("businessName")
    private String businessName;

    @SerializedName("businessImage")
    private String businessImage;

    // Getters
    public String getBusinessName() {
        return businessName;
    }

    public String getBusinessImage() {
        return businessImage;
    }
}