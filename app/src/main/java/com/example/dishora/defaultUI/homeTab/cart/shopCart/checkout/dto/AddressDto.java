package com.example.dishora.defaultUI.homeTab.cart.shopCart.checkout.dto;

import com.google.gson.annotations.SerializedName;

public class AddressDto {

    // SerializedNames must match the C# DTO properties (PascalCase)
    @SerializedName("PhoneNumber")
    public String phoneNumber;

    @SerializedName("Region")
    public String region;

    @SerializedName("Province")
    public String province;

    @SerializedName("City")
    public String city;

    @SerializedName("Barangay")
    public String barangay;

    @SerializedName("PostalCode")
    public String postalCode;

    @SerializedName("StreetName")
    public String streetName;

    @SerializedName("FullAddress")
    public String fullAddress;
}