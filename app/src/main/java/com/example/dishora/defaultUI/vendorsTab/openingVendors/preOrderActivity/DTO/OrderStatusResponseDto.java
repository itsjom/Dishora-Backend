package com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.DTO;

import com.google.gson.annotations.SerializedName;

public class OrderStatusResponseDto {
    @SerializedName("status")
    private String status;

    @SerializedName("orderId")
    private Long orderId;

    public String getStatus() {
        return status;
    }

    public Long getOrderId() {
        return orderId;
    }
}