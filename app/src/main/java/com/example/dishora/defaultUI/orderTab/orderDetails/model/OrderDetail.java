package com.example.dishora.defaultUI.orderTab.orderDetails.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * This new model matches the DETAILED OrderDetailDto from your server.
 */
public class OrderDetail {

    // These fields match your simple 'Order' model
    @SerializedName("id")
    private long id;
    @SerializedName("placedDate")
    private String placedDate;
    @SerializedName("vendorName")
    private String vendorName;
    @SerializedName("total")
    private double total;
    @SerializedName("status")
    private String status;

    // --- These are the NEW fields from the server ---
    @SerializedName("deliveryDate")
    private String deliveryDate;
    @SerializedName("isPaid")
    private boolean isPaid;
    @SerializedName("items")
    private List<OrderItem> items; // ✅ IT USES YOUR OrderItem MODEL HERE

    // --- Getters for all fields ---

    public long getId() { return id; }
    public String getPlacedDate() { return placedDate; }
    public String getVendorName() { return vendorName; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public String getDeliveryDate() { return deliveryDate; }
    public boolean isPaid() { return isPaid; }
    public List<OrderItem> getItems() { return items; }
}