package com.example.dishora;

import java.util.List;

public class StoreOrderGroup {
    String storeName;
    List<OrderItem> itemList;

    public StoreOrderGroup(String strName, List<OrderItem> itemList) {
        this.storeName = strName;
        this.itemList = itemList;
    }
}
