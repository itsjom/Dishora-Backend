package com.example.dishora.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ConversationDto {
    @SerializedName("recipientId")
    private long recipientId;
    @SerializedName("recipientName")
    private String recipientName;
    @SerializedName("lastMessage")
    private String lastMessage;
    @SerializedName("timestamp")
    private Date timestamp;
    @SerializedName("unreadCount")
    private int unreadCount;

    // Add Getters for all fields
    public long getRecipientId() { return recipientId; }
    public String getRecipientName() { return recipientName; }
    public String getLastMessage() { return lastMessage; }
    public Date getTimestamp() { return timestamp; }
    public int getUnreadCount() { return unreadCount; }
}
