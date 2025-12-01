package com.example.dishora.vendorUI;

import android.os.Bundle;
import android.util.Log; // <-- ADD THIS

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.dishora.R;
import com.example.dishora.databinding.ActivityVendorMainBinding;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.cart.CartManager;
import com.example.dishora.network.MessageApi;
import com.example.dishora.utils.SessionManager;

// --- ADD ALL THESE IMPORTS ---
import com.example.dishora.models.UnreadCountResponse;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.ApiService;
import com.google.android.material.badge.BadgeDrawable;
// -----------------------------

import com.example.dishora.vendorUI.homeTab.VendorHomeFragment;
import com.example.dishora.vendorUI.inboxTab.VendorInboxFragment;
import com.example.dishora.vendorUI.menuTab.VendorMenuFragment;
import com.example.dishora.vendorUI.orderTab.VendorOrderFragment;
import com.example.dishora.vendorUI.preOrderTab.VendorPreOrderFragment;
import com.example.dishora.vendorUI.profileTab.VendorProfileFragment;

// --- ADD THESE IMPORTS ---
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
// -------------------------

public class VendorMainActivity extends AppCompatActivity {

    ActivityVendorMainBinding binding;
    private SessionManager sessionManager; // <-- Make this a class variable
    private MessageApi apiService; // <-- Add this

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityVendorMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- Initialize SessionManager and ApiService ---
        sessionManager = new SessionManager(this);

        // Use the AUTHENTICATED client
        apiService = ApiClient.getBackendClient(this).create(MessageApi.class);

        if (sessionManager.isLoggedIn()) {
            CartManager.getInstance().setCurrentUser(String.valueOf(sessionManager.getUserId()));
        } else {
            CartManager.getInstance().setCurrentUser(null);
        }

        replaceFragment(new VendorHomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home) {
                replaceFragment(new VendorHomeFragment());
            } else if (id == R.id.order) {
                replaceFragment(new VendorOrderFragment());
            } else if (id == R.id.inbox) {
                replaceFragment(new VendorInboxFragment());
                // We are NOT clearing the badge on click, per your last instruction
            } else if (id == R.id.menu) {
                replaceFragment(new VendorMenuFragment());
            } else if (id == R.id.preorders) {
                replaceFragment(new VendorPreOrderFragment());
            }

            return true;
        });

        // --- ADD THIS ---
        // Fetch the unread count when the activity starts
        fetchUnreadMessageCount();
        // ----------------
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment_container, fragment);
        fragmentTransaction.commit();
    }

    // --- ADD THESE TWO METHODS ---

    /**
     * Shows or hides the notification badge on the Inbox tab.
     * @param count The number of unread messages. Pass 0 to hide.
     */
    public void showInboxBadge(int count) {
        // Use the vendor's binding
        BadgeDrawable badge = binding.bottomNavigationView.getOrCreateBadge(R.id.inbox);

        if (count > 0) {
            badge.setNumber(count);
            badge.setVisible(true);
            Log.d("VendorMain", "Showing badge with count: " + count);
        } else {
            badge.setVisible(false);
            Log.d("VendorMain", "Hiding badge.");
        }
    }

    /**
     * This is the REAL implementation that calls your API.
     */
    private void fetchUnreadMessageCount() {
        if (!sessionManager.isLoggedIn()) {
            return; // Don't fetch if user is a guest
        }

        apiService.getVendorUnreadCount().enqueue(new Callback<UnreadCountResponse>() {
            @Override
            public void onResponse(Call<UnreadCountResponse> call, Response<UnreadCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int unreadCount = response.body().getUnreadCount();
                    showInboxBadge(unreadCount);
                } else {
                    Log.e("VendorMain", "API Error fetching unread count: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UnreadCountResponse> call, Throwable t) {
                Log.e("VendorMain", "Network Failure fetching unread count: " + t.getMessage());
            }
        });
    }
}