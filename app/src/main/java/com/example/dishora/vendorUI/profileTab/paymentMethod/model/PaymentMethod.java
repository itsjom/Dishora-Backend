package com.example.dishora.vendorUI.profileTab.paymentMethod.model;

import com.google.gson.annotations.SerializedName;

public class PaymentMethod {
    @SerializedName("payment_method_id")
    private long id;

    @SerializedName("master_method_id")
    private long masterMethodId;

    @SerializedName("method_name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("enabled")
    private boolean enabled;

    @SerializedName("account_number")
    private String accountNumber;

    @SerializedName("account_name")
    private String accountName;

    public long getId() { return id; }
    public long getMasterMethodId() { return masterMethodId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isEnabled() { return enabled; }

    public String getAccountNumber() { return accountNumber; }
    public String getAccountName() { return accountName; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
}