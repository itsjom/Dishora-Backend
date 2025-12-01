package com.example.dishora.defaultUI.vendorsTab.openingVendors.model; // Or wherever this file actually is

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Product implements Serializable {

    @SerializedName("product_id")
    private long productId;

    @SerializedName("item_name")
    private String itemName;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private double price;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("is_available")
    private boolean isAvailable;

    @SerializedName("is_pre_order")
    private boolean isPreOrder;

    @SerializedName("vendor_id") // Assuming server sends this, matches C# business_id?
    private long vendorId; // Changed to long to match vendorUI model & backend likely

    // --- ADD THIS FIELD ---
    @SerializedName("advance_amount") // Make sure this matches the JSON key from server
    private double advanceAmount;
    // --- END OF ADDITION ---

    // ─────────────── Getters ───────────────
    public long getProductId() { return productId; }
    public String getItemName() { return itemName; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public boolean isAvailable() { return isAvailable; }
    public boolean isPreOrder() { return isPreOrder; }
    public long getVendorId() { return vendorId; } // Changed return type to long

    // --- ADD THIS GETTER ---
    public double getAdvanceAmount() { return advanceAmount; }
    // --- END OF ADDITION ---


    // ─────────────── Setters (optional) ───────────────
    public void setProductId(long productId) { this.productId = productId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public void setPreOrder(boolean preOrder) { this.isPreOrder = preOrder; }
    public void setVendorId(long vendorId) { this.vendorId = vendorId; } // Changed param type to long
    public void setAdvanceAmount(double advanceAmount) { this.advanceAmount = advanceAmount; } // Optional setter
}