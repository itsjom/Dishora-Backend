package com.example.dishora.defaultUI.homeTab.vendorSection;

public class Vendor {
    long id;       // <-- ADD THIS
    String name;
    String logoUrl;

    // CONSTRUCTOR (no changes needed, but good to be aware)
    public Vendor(String name, String logoUrl) {
        this.name = name;
        this.logoUrl = logoUrl;
    }

    // GETTERS
    public long getId() {  // <-- ADD THIS
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }
}