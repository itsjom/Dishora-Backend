package com.example.dishora.vendorUI.menuTab.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Product implements Serializable {
    @SerializedName("product_id")
    private long product_id;
    @SerializedName("item_name")
    private String item_name;
    @SerializedName("price")
    private double price;
    @SerializedName("image_url")
    private String image_url;
    @SerializedName("is_available")
    private boolean is_available;
    @SerializedName("is_pre_order")
    private boolean is_pre_order;
    @SerializedName("description")
    private String description;
    @SerializedName("product_category_id")
    private long product_category_id;
    @SerializedName("business_id")
    private long business_id;
    @SerializedName("advance_amount") // Or your server's name
    private double advance_amount;

    // Getters
    public long getProduct_id() { return product_id; }
    public String getItem_name() { return item_name; }
    public double getPrice() { return price; }
    public String getImage_url() { return image_url; }
    public boolean isAvailable() { return is_available; }
    public boolean isPreorder() { return is_pre_order; }
    public String getDescription() { return description; }
    public long getProduct_category_id() { return product_category_id; }
    public long getBusiness_id() { return business_id; }

    public void setItem_name(String item_name) { this.item_name = item_name; }
    public void setPrice(double price) { this.price = price; }
    public void setAvailable(boolean available) { this.is_available = available; }
    public void setPreorder(boolean preorder) { this.is_pre_order = preorder; }
    public void setDescription(String description) { this.description = description; }
    public void setImage_url(String image_url) { this.image_url = image_url; }

}
