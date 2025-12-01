package com.example.dishora.defaultUI.homeTab.cart.item;

import com.example.dishora.defaultUI.vendorsTab.openingVendors.model.Product;

public class CartItem {
    private Product product;
    private int quantity;
    private String note; // optional (for special requests)

    public CartItem(Product product) {
        this.product = product;
        this.quantity = 1;
    }

    public Product getProduct() { return product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
