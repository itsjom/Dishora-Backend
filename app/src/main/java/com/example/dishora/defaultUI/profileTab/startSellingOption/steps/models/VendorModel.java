package com.example.dishora.defaultUI.profileTab.startSellingOption.steps.models;

public class VendorModel {
    private String fullname;
    private String phoneNumber;
    private long userId;

    public VendorModel (String fullName, String phoneNumber, long userId) {
        this.fullname = fullName;
        this.phoneNumber = phoneNumber;
        this.userId = userId;
    }

    public String getFullName() { return fullname; }
    public void setFullName(String fullName) { this.fullname = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
}
