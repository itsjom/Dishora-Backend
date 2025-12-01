package com.example.dishora.defaultUI.inboxTab.chatTab.model;

import java.util.Date; // Or use String/long for timestamp

public class ChatMessage {
    private String messageId; // Unique ID for the message
    private String senderId;
    private String recipientId; // Or conversationId if group chat
    private String messageBody;
    private Date timestamp; // Or long/String
    private boolean isSentByUser; // Flag to easily determine view type

    // Constructor
    public ChatMessage(String messageId, String senderId, String recipientId, String messageBody, Date timestamp, boolean isSentByUser) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.messageBody = messageBody;
        this.timestamp = timestamp;
        this.isSentByUser = isSentByUser;
    }

    // Getters
    public String getMessageId() { return messageId; }
    public String getSenderId() { return senderId; }
    public String getRecipientId() { return recipientId; }
    public String getMessageBody() { return messageBody; }
    public Date getTimestamp() { return timestamp; }
    public boolean isSentByUser() { return isSentByUser; }

    // You might add setters if needed, e.g., for status (Sent, Delivered, Read)
}