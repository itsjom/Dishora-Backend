package com.example.dishora.defaultUI.orderTab.orderDetails.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class OrderItem implements Parcelable {
    @SerializedName("productName")
    private String productName;
    @SerializedName("quantity")
    private int quantity;
    @SerializedName("price")
    private double price;
    @SerializedName("subtotal")
    private double subtotal;

    // Getters
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getSubtotal() { return subtotal; }


    // --- Parcelable Implementation ---
    protected OrderItem(Parcel in) {
        productName = in.readString();
        quantity = in.readInt();
        price = in.readDouble();
        subtotal = in.readDouble();
    }

    public static final Creator<OrderItem> CREATOR = new Creator<OrderItem>() {
        @Override
        public OrderItem createFromParcel(Parcel in) {
            return new OrderItem(in);
        }
        @Override
        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productName);
        dest.writeInt(quantity);
        dest.writeDouble(price);
        dest.writeDouble(subtotal);
    }
}