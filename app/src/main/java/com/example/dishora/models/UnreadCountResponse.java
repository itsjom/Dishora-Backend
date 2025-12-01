package com.example.dishora.models;

import com.google.gson.annotations.SerializedName;

public class UnreadCountResponse {
    @SerializedName("unreadCount")
    private int unreadCount;

    public int getUnreadCount() {
        return unreadCount;
    }
}
