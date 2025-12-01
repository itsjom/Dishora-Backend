package com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.model;

public class UserAddress {
    // Add other fields your backend might return, like 'id'
    // @SerializedName("id")
    // private int id;

    private String name;
    private String phone;
    private String region;
    private String province;
    private String city;
    private String barangay;
    private String postcode;
    private String streetAddress;
    private String fullAddress;
    private double latitude;
    private double longitude;

    // Constructor with all 11 fields
    public UserAddress(String name, String phone, String region, String province, String city, String barangay, String postcode, String streetAddress, String fullAddress, double latitude, double longitude) {
        this.name = name;
        this.phone = phone;
        this.region = region;
        this.province = province;
        this.city = city;
        this.barangay = barangay;
        this.postcode = postcode;
        this.streetAddress = streetAddress;
        this.fullAddress = fullAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // --- Getters ---
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getRegion() { return region; }
    public String getProvince() { return province; }
    public String getCity() { return city; }
    public String getBarangay() { return barangay; }
    public String getPostcode() { return postcode; }
    public String getStreetAddress() { return streetAddress; }
    public String getFullAddress() { return fullAddress; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}