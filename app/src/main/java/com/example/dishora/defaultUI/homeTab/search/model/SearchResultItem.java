package com.example.dishora.defaultUI.homeTab.search.model;

public class SearchResultItem {
    private String foodName;
    private String storeName;
    private double rating;
    private String reviews;
    private String time;
    private String price;
    private int imageResId; // for drawable images, use URL + Glide if from API

    public SearchResultItem(String foodName, String storeName, double rating, String reviews, String time, String price, int imageResId) {
        this.foodName = foodName;
        this.storeName = storeName;
        this.rating = rating;
        this.reviews = reviews;
        this.time = time;
        this.price = price;
        this.imageResId = imageResId;
    }

    public String getFoodName() { return foodName; }
    public String getStoreName() { return storeName; }
    public double getRating() { return rating; }
    public String getReviews() { return reviews; }
    public String getTime() { return time; }
    public String getPrice() { return price; }
    public int getImageResId() { return imageResId; }
}

