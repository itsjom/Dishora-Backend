package com.example.dishora.vendorUI.menuTab.request;

import com.google.gson.annotations.SerializedName;

public class ProductUpdateRequest {
    @SerializedName("item_name")
    private String item_name;

    @SerializedName("price")
    private Double price; // use wrapper type so null means "not sent"

    @SerializedName("is_available")
    private Boolean is_available;

    @SerializedName("is_pre_order")
    private Boolean is_pre_order;

    @SerializedName("description")
    private String description;

    @SerializedName("product_category_id")
    private Long product_category_id; // optional FK

    // Getters and setters
    public String getItem_name() { return item_name; }
    public void setItem_name(String item_name) { this.item_name = item_name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Boolean getIs_available() { return is_available; }
    public void setIs_available(Boolean is_available) { this.is_available = is_available; }

    public Boolean getIs_pre_order() { return is_pre_order; }
    public void setIs_pre_order(Boolean is_pre_order) { this.is_pre_order = is_pre_order; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getProduct_category_id() { return product_category_id; }
    public void setProduct_category_id(Long product_category_id) { this.product_category_id = product_category_id; }
}
