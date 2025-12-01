package com.example.dishora.defaultUI.vendorsTab.model;

import com.google.gson.annotations.SerializedName;

public class Vendor {

    @SerializedName("vendorId")
    private long vendorId;

    @SerializedName("userId")
    private long userId;

    @SerializedName("businessId")
    private long businessId;

    @SerializedName("businessName")
    private String businessName;

    @SerializedName("businessAddress")
    private String businessAddress;

    @SerializedName("businessDescription")
    private String businessDescription;

    @SerializedName("businessImage")
    private String businessImage;

    @SerializedName("vendorStatus")
    private String vendorStatus;

    @SerializedName("rating")
    private double rating;

    // Getters
    public long getVendorId() {
        return vendorId;
    }

    public long getUserId() { return userId; }

    public long getBusinessId() {
        return businessId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getBusinessAddress() { return businessAddress; }

    public String getBusinessDescription() { return businessDescription; }

    public String getBusinessImage() { return businessImage; }

    public String getVendorStatus() { return vendorStatus; }

    public double getRating() { return rating; }
}