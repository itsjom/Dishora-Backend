package com.example.dishora.vendorUI.preOrderTab.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GroupedPreOrder {

    @SerializedName("groupId")
    private String groupId;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("status")
    private String status;

    @SerializedName("items")
    private List<PreOrderItem> items;

    // Getters
    public String getGroupId() {
        return groupId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public List<PreOrderItem> getItems() {
        return items;
    }
}