package com.example.dishora.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("fullName")
    private String FullName;

    @SerializedName("userName")
    private String UserName;

    @SerializedName("email")
    private String Email;

    @SerializedName("password")
    private String Password;

    public RegisterRequest(String fullName, String userName, String email, String password) {
        this.FullName = fullName;
        this.UserName = userName;
        this.Email = email;
        this.Password = password;
    }

    public String getFullName() { return FullName; }
    public String getUserName() { return UserName; }
    public String getEmail() { return Email; }
    public String getPassword() { return Password; }
}

