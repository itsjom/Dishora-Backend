package com.example.dishora.defaultUI.profileTab.startSellingOption.steps.dto;

public class BusinessDto {
    private String businessName;
    private String description;
    private String type;
    private String location;
    private String businessDuration;
    private String birRegNo;
    private String businessPermitNo;
    private String validIdType;
    private String validIdNo;
    private double latitude;
    private double longitude;

    // ===== Getters/Setters =====

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getBusinessDuration() { return businessDuration; }
    public void setBusinessDuration(String businessDuration) { this.businessDuration = businessDuration; }

    public String getBirRegNo() { return birRegNo; }
    public void setBirRegNo(String birRegNo) { this.birRegNo = birRegNo; }

    public String getBusinessPermitNo() { return businessPermitNo; }
    public void setBusinessPermitNo(String businessPermitNo) { this.businessPermitNo = businessPermitNo; }

    public String getValidIdType() { return validIdType; }
    public void setValidIdType(String validIdType) { this.validIdType = validIdType; }

    public String getValidIdNo() { return validIdNo; }
    public void setValidIdNo(String validIdNo) { this.validIdNo = validIdNo; }
}