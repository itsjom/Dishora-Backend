package com.example.dishora.defaultUI.orderTab.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.example.dishora.defaultUI.orderTab.orderDetails.model.OrderItem; // Import the new model
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Order implements Parcelable {

    // --- Existing fields ---
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

    // --- New fields for details screen ---
    @SerializedName("deliveryDate")
    private String deliveryDate;
    @SerializedName("isPaid")
    private boolean isPaid;
    @SerializedName("items")
    private List<OrderItem> items;

    // --- Getters for all fields ---
    public long getId() { return id; }
    public String getPlacedDate() { return placedDate; }
    public String getVendorName() { return vendorName; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public String getDeliveryDate() { return deliveryDate; }
    public boolean isPaid() { return isPaid; }
    public List<OrderItem> getItems() { return items; }


    // --- Parcelable Implementation (Updated) ---
    protected Order(Parcel in) {
        id = in.readLong();
        placedDate = in.readString();
        vendorName = in.readString();
        total = in.readDouble();
        status = in.readString();
        deliveryDate = in.readString();
        isPaid = in.readByte() != 0;
        items = in.createTypedArrayList(OrderItem.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(placedDate);
        dest.writeString(vendorName);
        dest.writeDouble(total);
        dest.writeString(status);
        dest.writeString(deliveryDate);
        dest.writeByte((byte) (isPaid ? 1 : 0));
        dest.writeTypedList(items);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }
        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };
}