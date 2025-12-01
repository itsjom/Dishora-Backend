package com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.DTO;

public class CheckoutRequestDto {
    private int amount;                // centavos
    private String itemName;
    private int quantity;
    private int vendorId;
    private String paymentMethodType;  // "card", "gcash", "paymaya"
    private final String orderDetailsMetadata;

    public CheckoutRequestDto(int amount, String itemName, int quantity, int vendorId, String paymentMethodType,  String orderDetailsMetadata) {
        this.amount = amount;
        this.itemName = itemName;
        this.quantity = quantity;
        this.vendorId = vendorId;
        this.paymentMethodType = paymentMethodType;
        this.orderDetailsMetadata = orderDetailsMetadata;
    }

    // Getters/setters (optional if Gson is used)
    public int getAmount() { return amount; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public int getVendorId() { return vendorId; }
    public String getPaymentMethodType() { return paymentMethodType; }
}
