// SessionManager.java
package com.example.dishora.utils;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.dishora.Login;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.cart.CartManager;
import com.example.dishora.models.LoginResponse;

/**
 * Manages user session data using SharedPreferences.
 * This class centralizes all session-related logic, such as login, logout,
 * and retrieving user data. It acts as a single source of truth for the user's
 * session state throughout the application.
 */
public class SessionManager {

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private final Context context;

    // Name of the SharedPreferences file
    private static final String PREF_NAME = "MyPreferences";

    // Key for checking if the user is logged in
    private static final String IS_LOGGED_IN = "isLoggedIn";

    // All user data keys (public so they can be accessed if needed, but using getters is preferred)
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_JWT_TOKEN = "jwt_token";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_FULL_NAME = "full_name";   // ✅ add this
    public static final String KEY_EMAIL = "email";
    public static final String KEY_IS_VENDOR = "isVendor";
    public static final String KEY_VENDOR_STATUS = "vendorStatus";
    public static final String KEY_VENDOR_ID = "vendorId";
    public static final String KEY_BUSINESS_ID = "business_id";

    public SessionManager(Context context) {
        this.context = context.getApplicationContext(); // Use application context to avoid memory leaks
        sharedPreferences = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Creates a login session for the user.
     * This method saves all user data to SharedPreferences and updates the CartManager.
     * @param data The UserData object received from the successful login API response.
     */
    public void createLoginSession(LoginResponse.UserData data) {
        // Save all the user data
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, data.getUserId());
        editor.putString(KEY_JWT_TOKEN, data.getToken());
        editor.putString(KEY_USERNAME, data.getUsername());
        editor.putString(KEY_FULL_NAME, data.getFullName());      // ✅ store full name
        editor.putString(KEY_EMAIL, data.getEmail());
        editor.putBoolean(KEY_IS_VENDOR, data.isVendor());
        editor.putString(KEY_VENDOR_STATUS, data.getVendorStatus());

        // Handle potentially null Long values with a default
        editor.putLong(KEY_VENDOR_ID, data.getVendorId() != null ? data.getVendorId() : -1L);
        editor.putLong(KEY_BUSINESS_ID, data.getBusinessId() != null ? data.getBusinessId() : -1L);

        // Use commit() for login because it's a critical, synchronous action.
        // The app should not proceed until the session is successfully saved.
        editor.commit();

        // IMPORTANT: Tell the CartManager who the current user is.
        CartManager.getInstance().setCurrentUser(String.valueOf(data.getUserId()));

        Log.d(TAG, "====== LOGIN SUCCESS ======");
        Log.d(TAG, "SessionManager: Created session for userId: " + data.getUserId());
    }

    /**
     * Clears all session data and redirects the user to the Login screen.
     * This method also resets the CartManager to guest mode.
     */
    public void logoutUser() {
        // Reset the CartManager to guest mode. The user's cart data remains saved on disk.
        CartManager.getInstance().setCurrentUser(null);

        Log.d(TAG, "====== LOGOUT ======");
        Log.d(TAG, "SessionManager: Resetting CartManager to guest.");

        // Clear all data from SharedPreferences
        editor.clear();
        editor.commit();

        // After logout, redirect the user to the Login Activity
        Intent i = new Intent(context, Login.class);
        // Add flags to clear the activity stack, so the user can't press "back"
        // to return to the logged-in part of the app.
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }

    /**
     * Checks if a user is currently logged in.
     * @return true if logged in, false otherwise.
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false);
    }

    // --- GETTERS for user data ---

    public long getUserId() { return sharedPreferences.getLong(KEY_USER_ID, -1L); }

    public String getToken() { return sharedPreferences.getString(KEY_JWT_TOKEN, null); }

    public String getUsername() { return sharedPreferences.getString(KEY_USERNAME, "Guest"); }

    public String getFullName() { return sharedPreferences.getString(KEY_FULL_NAME, getUsername()); }

    public String getEmail() { return sharedPreferences.getString(KEY_EMAIL, ""); }

    public boolean isVendor() { return sharedPreferences.getBoolean(KEY_IS_VENDOR, false); }

    public String getVendorStatus() { return sharedPreferences.getString(KEY_VENDOR_STATUS, "Not Registered"); }

    public long getBusinessId() { return sharedPreferences.getLong(KEY_BUSINESS_ID, -1L); }
}