package com.example.dishora.defaultUI.homeTab.cart.model;

import com.example.dishora.defaultUI.homeTab.cart.item.CartItem;

import java.util.List;

public class CartModel {
    private final String shopName;
    private final String shopAddress;
    private final String shopLogoUrl;    // ✅ use URL instead of resource id
    private final long businessId;
    private final List<CartItem> items;

    public CartModel(String shopName, String shopAddress, String shopLogoUrl, long businessId, List<CartItem> items) {
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.shopLogoUrl = shopLogoUrl;
        this.items = items;
        this.businessId = businessId;
    }

    public String getShopName() { return shopName; }
    public String getShopAddress() { return shopAddress; }
    public String getShopLogoUrl() { return shopLogoUrl; }
    public long getBusinessId() { return businessId; }
    public List<CartItem> getItems() { return items; }

    public double calculateTotal() {
        double total = 0;
        for (CartItem item : items) {
            if (item.getProduct() != null) {
                total += item.getProduct().getPrice() * item.getQuantity();
            }
        }
        return total;
    }
}