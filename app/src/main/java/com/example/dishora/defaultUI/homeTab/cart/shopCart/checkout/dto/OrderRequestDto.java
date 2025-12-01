package com.example.dishora.defaultUI.homeTab.cart.shopCart.checkout.dto;

import java.util.List;

public class OrderRequestDto {
    public long userId;
    public long businessId;
    public long paymentMethodId;
    public double total;
    public String deliveryDate;  // ISO8601 format (e.g. 2025‑10‑08T00:00:00Z)
    public String deliveryTime;
    public AddressDto address;
    public List<ItemDto> items;
}
