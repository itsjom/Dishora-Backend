package com.example.dishora.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private boolean success;
    private String message;
    private UserData data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UserData getData() { return data; }

    public static class UserData {
        @SerializedName("user_Id")
        private long userId;

        @SerializedName("business_Id")
        private Long businessId;

        @SerializedName("fullName")
        private String fullName;

        @SerializedName("username")
        private String username;

        @SerializedName("email")
        private String email;

        @SerializedName("token")
        private String token;

        @SerializedName("isVendor")
        private boolean isVendor;

        @SerializedName("vendorStatus")
        private String vendorStatus;

        @SerializedName("vendorId")
        private Long vendorId;

        // --- Getters ---
        public long getUserId() { return userId; }
        public String getFullName() { return fullName; }      // ✅ add getter
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getToken() { return token; }
        public boolean isVendor() { return isVendor; }
        public String getVendorStatus() { return vendorStatus; }
        public Long getVendorId() { return vendorId; }
        public Long getBusinessId() { return businessId; }
    }
}