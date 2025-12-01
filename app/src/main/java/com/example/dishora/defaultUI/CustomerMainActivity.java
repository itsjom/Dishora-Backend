package com.example.dishora.defaultUI;

import android.os.Bundle;
import android.util.Log; // <-- ADD THIS IMPORT

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.dishora.R;
import com.example.dishora.databinding.ActivityCustomerMainBinding;
import com.example.dishora.defaultUI.homeTab.HomeFragment;
import com.example.dishora.defaultUI.inboxTab.CustomerInboxFragment;
import com.example.dishora.defaultUI.orderTab.CustomerOrderFragment;
import com.example.dishora.defaultUI.profileTab.ProfileFragment;
import com.example.dishora.defaultUI.vendorsTab.VendorsFragment;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.cart.CartManager;
import com.example.dishora.models.UnreadCountResponse;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.ApiService;
import com.example.dishora.network.MessageApi;
import com.example.dishora.utils.SessionManager;
import com.google.android.material.badge.BadgeDrawable; // <-- ADD THIS IMPORT

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerMainActivity extends AppCompatActivity {

    ActivityCustomerMainBinding binding;
    private SessionManager sessionManager; // <-- Make this a class variable
    private MessageApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCustomerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- Initialize SessionManager ---
        sessionManager = new SessionManager(this);

        apiService = ApiClient.getBackendClient(this).create(MessageApi.class);

        if (sessionManager.isLoggedIn()) {
            CartManager.getInstance().setCurrentUser(String.valueOf(sessionManager.getUserId()));
        } else {
            CartManager.getInstance().setCurrentUser(null);
        }

        // Default fragment
        replaceFragment(new HomeFragment());

        // --- Bottom Navigation Listener ---
        binding.customerBottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.order) {
                replaceFragment(new CustomerOrderFragment());
            } else if (id == R.id.inbox) {
                replaceFragment(new CustomerInboxFragment());
                // showInboxBadge(0);
            } else if (id == R.id.vendors) {
                replaceFragment(new VendorsFragment());
            } else if (id == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }

            return true;
        });

        // <-- ADD THIS: Fetch the unread count when the activity starts
        fetchUnreadMessageCount();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    // --- ADD THIS HELPER METHOD ---
    /**
     * Shows or hides the notification badge on the Inbox tab.
     * @param count The number of unread messages. Pass 0 to hide.
     */
    public void showInboxBadge(int count) {
        // Get the badge for the 'inbox' menu item
        BadgeDrawable badge = binding.customerBottomNavigationView.getOrCreateBadge(R.id.inbox);

        if (count > 0) {
            badge.setNumber(count);
            badge.setVisible(true);
            Log.d("CustomerMain", "Showing badge with count: " + count);
        } else {
            // Hide the badge if the count is 0
            badge.setVisible(false);
            Log.d("CustomerMain", "Hiding badge.");
        }
    }

    // --- ADD THIS API CALL METHOD ---
    /**
     * This is where you will call your API to get the unread count.
     */
    private void fetchUnreadMessageCount() {
        if (!sessionManager.isLoggedIn()) {
            return; // Don't fetch if user is a guest
        }

        apiService.getCustomerUnreadCount().enqueue(new Callback<UnreadCountResponse>() {
            @Override
            public void onResponse(Call<UnreadCountResponse> call, Response<UnreadCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int unreadCount = response.body().getUnreadCount();
                    showInboxBadge(unreadCount);
                } else {
                    Log.e("CustomerMain", "API Error fetching unread count: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UnreadCountResponse> call, Throwable t) {
                Log.e("CustomerMain", "Network Failure fetching unread count: " + t.getMessage());
            }
        });
    }
}