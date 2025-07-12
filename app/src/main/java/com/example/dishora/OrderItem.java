package com.example.dishora;

// MODEL CLASS //
public class OrderItem {
    String name, location, price;
    int image;

    public OrderItem(String name, String loc, String prc, int img) {
        this.name = name;
        this.location = loc;
        this.price = prc;
        this.image = img;

    }
}
