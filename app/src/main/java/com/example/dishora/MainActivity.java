package com.example.dishora;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dishora.defaultUI.CustomerMainActivity;
import com.example.dishora.utils.SessionManager; // Import the SessionManager
import com.example.dishora.vendorUI.VendorMainActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // NOTE: We don't need to call EdgeToEdge or setContentView because this activity
        // will immediately redirect and then finish(). It has no visible UI.

        // Create an instance of the SessionManager to check the user's login state
        SessionManager sessionManager = new SessionManager(this);
        Intent intent;

        // Check if the user is logged in
        if (sessionManager.isLoggedIn()) {
            // User is logged in, now check if they are an approved vendor
            // This logic matches what you have in your Login and Settings screens.
            if (sessionManager.isVendor() && "Approved".equalsIgnoreCase(sessionManager.getVendorStatus())) {
                // Redirect to the vendor dashboard
                intent = new Intent(this, VendorMainActivity.class);
            } else {
                // User is a customer or a non-approved vendor, redirect to the customer screen
                intent = new Intent(this, CustomerMainActivity.class);
            }
        } else {
            // User is not logged in, redirect to the Login screen
            intent = new Intent(this, Login.class);
        }

        // Start the determined activity
        startActivity(intent);

        // Call finish() to remove this MainActivity from the back stack.
        // This prevents the user from pressing the back button and returning to a blank screen.
        finish();
    }
}