package com.example.dishora.defaultUI.profileTab.startSellingOption.steps.dto;

import java.util.List;

public class RegistrationRequest {
    private long userId;
    private VendorDto vendor;
    private BusinessDto business;
    private List<OpeningHourDto> openingHours;

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public VendorDto getVendor() { return vendor; }
    public void setVendor(VendorDto vendor) { this.vendor = vendor; }

    public BusinessDto getBusiness() { return business; }
    public void setBusiness(BusinessDto business) { this.business = business; }

    public List<OpeningHourDto> getOpeningHours() { return openingHours; }
    public void setOpeningHours(List<OpeningHourDto> openingHours) { this.openingHours = openingHours; }
}
