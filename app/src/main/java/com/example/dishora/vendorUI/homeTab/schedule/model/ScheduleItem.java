package com.example.dishora.vendorUI.homeTab.schedule.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

// Represents one schedule entry received from the ASP.NET Core API
public class ScheduleItem implements Serializable {

    @SerializedName("scheduleId")
    public long scheduleId;

    @SerializedName("availableDate")
    public String availableDate; // Matches C# string format "yyyy-MM-dd"

    @SerializedName("maxOrders")
    public int maxOrders;

    @SerializedName("currentOrderCount")
    public int currentOrders;

    @SerializedName("isActive")
    public boolean isActive;

    @SerializedName("businessId")
    public long businessId;

    // Optional: Keep the constructor simple for easy instantiation if needed,
    // but Gson uses reflection, so it doesn't strictly require this constructor
    public ScheduleItem(String availableDate, int maxOrders, int currentOrders) {
        this.availableDate = availableDate;
        this.maxOrders = maxOrders;
        this.currentOrders = currentOrders;
    }

    // Calculated method used by the RecyclerView Adapter
    public int getRemainingCapacity() {
        return maxOrders - currentOrders;
    }

    // You can also add getters for a cleaner coding style, but it's optional
    public String getAvailableDate() { return availableDate; }

    public boolean isFull() {
        // A date is considered "full" or unavailable if it's inactive
        // OR if the current orders meet or exceed the max capacity.
        if (!isActive) {
            return true;
        }
        return currentOrders >= maxOrders;
    }
}