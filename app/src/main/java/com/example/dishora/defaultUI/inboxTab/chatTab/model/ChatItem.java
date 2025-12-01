package com.example.dishora.defaultUI.inboxTab.chatTab.model;

public class ChatItem {
    public String recipientId;
    public int imageResId;
    public String name;
    public String message;
    public String time;
    public String badgeCount;

    public ChatItem(String recipientId, int imageResId, String name, String message, String time, String badgeCount) {
        this.recipientId = recipientId;
        this.imageResId = imageResId;
        this.name = name;
        this.message = message;
        this.time = time;
        this.badgeCount = badgeCount;
    }

    public String getRecipientId() { return recipientId; }
    public int getImageResId() { return imageResId; }
    public String getName() { return name; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public String getBadgeCount() { return badgeCount; }
}
