package com.example.dishora.vendorUI.orderTab.model;

public class GroupStatusUpdateRequest {

    final long OrderId; // Must match C# 'OrderId'
    final String NewStatus; // Must match C# 'NewStatus'

    public GroupStatusUpdateRequest(long orderId, String newStatus) {
        this.OrderId = orderId;
        this.NewStatus = newStatus;
    }
}