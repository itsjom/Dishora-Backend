package com.example.dishora.vendorUI.orderTab.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents one "Customer Card"
 * It is created from the *first* OrderItem in a group.
 */
public class GroupedOrder {

    // --- 1. FIELD TO STORE THE REAL ID ---
    // This is the field that was missing
    private final long orderId;

    // A unique ID for this group (can be the same as orderId)
    private final String groupId;

    // Info for the card display
    private final String customerName;
    private final String orderDate;
    private final String contactNumber;
    private final String deliveryAddress;
    private final String paymentMethod;
    private final String deliveryDate; // Can be null
    private final String status; // Status of the whole group

    private double totalPrice;
    private final List<OrderItem> items;

    // Constructor to create a new group from the first item
    public GroupedOrder(OrderItem firstItem) {

        // --- 2. STORE THE REAL ORDER ID ---
        this.orderId = firstItem.getOrderId(); // Get the ID from the item

        // Use the OrderId as the unique group identifier
        this.groupId = String.valueOf(firstItem.getOrderId());

        // --- Store all other info from the first item ---
        this.customerName = firstItem.getCustomerFullName();
        this.orderDate = firstItem.getCreatedAt();
        this.contactNumber = firstItem.getContactNumber();
        this.deliveryAddress = firstItem.getDeliveryAddress();
        this.paymentMethod = firstItem.getPaymentMethodName();
        this.deliveryDate = firstItem.getDeliveryDate();
        this.status = firstItem.getOrderItemStatus(); // Group status is set by the first item

        this.items = new ArrayList<>();
        this.totalPrice = 0;

        // Add the first item
        this.addItem(firstItem);
    }

    // Add an item and update the total price
    public void addItem(OrderItem item) {
        this.items.add(item);
        this.totalPrice += (item.getPriceAtOrderTime() * item.getQuantity());
    }

    // --- 3. THE MISSING METHOD ---
    // This is the method your ViewModel needs
    public long getOrderId() {
        return orderId;
    }

    // --- Other Getters ---
    public String getGroupId() { return groupId; }
    public String getCustomerName() { return customerName; }
    public String getOrderDate() { return orderDate; }
    public String getContactNumber() { return contactNumber; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getDeliveryDate() { return deliveryDate; }
    public String getStatus() { return status; }
    public double getTotalPrice() { return totalPrice; }
    public List<OrderItem> getItems() { return items; }
}