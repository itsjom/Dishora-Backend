package com.example.dishora.vendorUI.homeTab.schedule.model;

public class ScheduleCreateRequest {
    // Note: The format of this date must match the C# DateOnly parsing (YYYY-MM-DD)
    public String availableDate;
    public int maxOrders;

    public ScheduleCreateRequest(String availableDate, int maxOrders) {
        this.availableDate = availableDate;
        this.maxOrders = maxOrders;
    }
}
