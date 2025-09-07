package com.example.dishora.models;

import android.service.autofill.UserData;

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
        private long user_id;
        private String username;

        public long getUserId() {
            return user_id;
        }

        public String getUsername() {
            return username;
        }
    }
}
