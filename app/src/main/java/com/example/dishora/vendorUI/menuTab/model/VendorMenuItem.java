package com.example.dishora.vendorUI.menuTab.model;

public class VendorMenuItem {
    private String name;
    private double price;
    private int imageRes;
    private boolean available;

    public VendorMenuItem(String name, double price, int imageRes, boolean available) {
        this.name = name;
        this.price = price;
        this.imageRes = imageRes;
        this.available = available;
    }

    // Getters
    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getImageRes() {
        return imageRes;
    }

    public boolean isAvailable() {
        return available;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}