package com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.DTO;

import com.google.gson.annotations.SerializedName;

public class CheckoutResponseDto {
    @SerializedName("checkoutUrl")
    private String checkoutUrl;
    @SerializedName("checkoutId")
    private String checkoutId;
    @SerializedName("draftId")
    private long draftId;


    public String getCheckoutUrl() {
        return checkoutUrl;
    }
    public String getCheckoutId() { return checkoutId; }
    public long getDraftId() { return draftId; }
}