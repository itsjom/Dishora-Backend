package com.example.dishora.vendorUI.profileTab.paymentMethod.model;

import com.google.gson.annotations.SerializedName;

public class PaymentDetailsBody {

    @SerializedName("business_payment_method_id")
    private long businessPaymentMethodId;

    @SerializedName("account_number")
    private String accountNumber;

    @SerializedName("account_name")
    private String accountName;

    public PaymentDetailsBody(long businessPaymentMethodId, String accountName, String accountNumber) {
        this.businessPaymentMethodId = businessPaymentMethodId;
        this.accountName = accountName;
        this.accountNumber = accountNumber;
    }
}
