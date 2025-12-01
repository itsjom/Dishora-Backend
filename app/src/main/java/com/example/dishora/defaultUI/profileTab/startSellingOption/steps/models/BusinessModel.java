package com.example.dishora.defaultUI.profileTab.startSellingOption.steps.models;

import java.util.List;

public class BusinessModel {
    private String businessName;
    private String businessImage;
    private String businessDuration;
    private String description;
    private String type;
    private String location;
    private double latitude;
    private double longitude;

    // Step 2 related
    private String birRegNo;
    private String birRegFile;
    private String businessPermitNo;
    private String businessPermitFile;
    private String validIdType;
    private String validIdNo;
    private String validIdFile;
    private String mayorPermitFile;

    private List<OpeningHourModel> openingHours;

    // Getter methods
    public String getBusinessImage() { return businessImage; }
    public String getBusinessDuration() { return businessDuration; }
    public String getBusinessName() { return businessName; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getLocation() { return location; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getBirRegNo() { return birRegNo; }
    public String getBirRegFile() { return birRegFile; }
    public String getBusinessPermitNo() { return businessPermitNo; }
    public String getBusinessPermitFile() { return businessPermitFile; }
    public String getValidIdType() { return validIdType; }
    public String getValidIdNo() { return validIdNo; }
    public String getValidIdFile() { return validIdFile; }
    public String getMayorPermitFile() { return mayorPermitFile; }
    public List<OpeningHourModel> getOpeningHours() { return openingHours; }

    // Setter methods
    public void setBusinessImage(String businessImage) { this.businessImage = businessImage; }
    public void setBusinessDuration(String businessDuration) { this.businessDuration = businessDuration; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public void setDescription(String description) { this.description = description; }
    public void setType(String type) { this.type = type; }
    public void setLocation(String location) { this.location = location; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setBirRegNo(String birRegNo) { this.birRegNo = birRegNo; }
    public void setBirRegFile(String birRegFile) { this.birRegFile = birRegFile; }
    public void setBusinessPermitNo(String businessPermitNo) { this.businessPermitNo = businessPermitNo; }
    public void setBusinessPermitFile(String businessPermitFile) { this.businessPermitFile = businessPermitFile; }
    public void setValidIdType(String validIdType) { this.validIdType = validIdType; }
    public void setValidIdNo(String validIdNo) { this.validIdNo = validIdNo; }
    public void setValidIdFile(String validIdFile) { this.validIdFile = validIdFile; }
    public void setMayorPermitFile(String mayorPermitFile) { this.mayorPermitFile = mayorPermitFile; }
    public void setOpeningHours(List<OpeningHourModel> openingHours) { this.openingHours = openingHours; }
}