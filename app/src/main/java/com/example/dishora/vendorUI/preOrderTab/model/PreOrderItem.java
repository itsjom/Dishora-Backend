package com.example.dishora.vendorUI.preOrderTab.model;

import com.google.gson.annotations.SerializedName;

public class PreOrderItem {

    @SerializedName("menuItemName")
    private String menuItemName;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("price")
    private double price;

    // Getters
    public String getMenuItemName() {
        return menuItemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}