package com.example.dishora.models;

import com.google.gson.annotations.SerializedName;

public class VendorStatusResponse {

    @SerializedName("isVendor")
    private boolean isVendor;

    @SerializedName("vendorStatus") // ✅ MUST match backend
    private String vendorStatus;

    @SerializedName("vendorId")
    private Long vendorId;

    public boolean isVendor() { return isVendor; }
    public String getStatus() { return vendorStatus; }
    public Long getVendorId() { return vendorId; }

    public void setVendor(boolean vendor) { this.isVendor = vendor; }
    public void setVendorStatus(String vendorStatus) { this.vendorStatus = vendorStatus; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
}