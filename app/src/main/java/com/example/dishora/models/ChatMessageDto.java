package com.example.dishora.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class ChatMessageDto {
    @SerializedName("messageId")
    private long messageId;
    @SerializedName("senderId")
    private long senderId;
    @SerializedName("messageText")
    private String messageText;
    @SerializedName("sentAt")
    private Date sentAt;

    // Add getters for all fields
    public long getMessageId() { return messageId; }
    public long getSenderId() { return senderId; }
    public String getMessageText() { return messageText; }
    public Date getSentAt() { return sentAt; }
}