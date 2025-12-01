package com.example.dishora.vendorUI.orderTab.model;

import com.google.gson.annotations.SerializedName;

// FINAL VERSION: This model is "flat" to perfectly match the new VendorOrderItemDto from the backend.
public class OrderItem {

    // Note: The @SerializedName values MUST match the camelCase JSON keys from the C# backend.

    @SerializedName("orderItemId")
    private long orderItemId;

    @SerializedName("orderId")
    private long orderId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("priceAtOrderTime")
    private double priceAtOrderTime;

    @SerializedName("orderItemStatus")
    private String orderItemStatus;

    @SerializedName("createdAt")
    private String createdAt;

    // --- Fields for Payment and Customer Info ---
    @SerializedName("paymentMethodName")
    private String paymentMethodName;

    @SerializedName("customerFullName")
    private String customerFullName;

    // --- Fields for Delivery Info ---
    @SerializedName("contactNumber")
    private String contactNumber;

    @SerializedName("deliveryAddress")
    private String deliveryAddress;

    @SerializedName("deliveryDate")
    private String deliveryDate; // This will be a date-time string from the server

    // --- GETTERS for all fields ---

    public long getOrderItemId() { return orderItemId; }
    public long getOrderId() { return orderId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPriceAtOrderTime() { return priceAtOrderTime; }
    public String getOrderItemStatus() { return orderItemStatus; }
    public String getCreatedAt() { return createdAt; }
    public String getPaymentMethodName() { return paymentMethodName; }
    public String getCustomerFullName() { return customerFullName; }
    public String getContactNumber() { return contactNumber; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getDeliveryDate() { return deliveryDate; }

    // --- SETTER for updating status locally ---

    public void setOrderItemStatus(String orderItemStatus) {
        this.orderItemStatus = orderItemStatus;
    }

    // The nested ProductInfo and OrderInfo classes have been removed because
    // all their necessary information is now included at the top level.
}