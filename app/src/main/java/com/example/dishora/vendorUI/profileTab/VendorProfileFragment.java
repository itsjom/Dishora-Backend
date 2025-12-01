package com.example.dishora.vendorUI.profileTab;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

// ADD THESE IMPORTS
import com.bumptech.glide.Glide;
import com.example.dishora.R;
import com.example.dishora.adapters.SettingsAdapter;
import com.example.dishora.defaultUI.CustomerMainActivity;
import com.example.dishora.models.SettingItem;
import com.example.dishora.network.ApiClient;
import com.example.dishora.utils.SessionManager;
import com.example.dishora.vendorUI.profileTab.api.ProfileApiService;
import com.example.dishora.vendorUI.profileTab.model.BusinessProfile;
import com.example.dishora.vendorUI.profileTab.paymentMethod.PaymentMethodActivity;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorProfileFragment extends Fragment {

    // View variables
    private RecyclerView recyclerView;
    private TextView usernameTextView;
    private TextView emailTextView;
    private ShapeableImageView profileImage; // <-- ADDED
    private ProgressBar profileProgressBar; // <-- ADDED
    private LinearLayout profileContentLayout; // <-- ADDED

    // Adapter and model variables
    private SettingsAdapter vendorSettingAdapter;
    private List<SettingItem> vendorSettingItems;

    // Utils
    private SessionManager sessionManager;
    private ProfileApiService apiService; // <-- ADDED

    public VendorProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SessionManager and ApiService
        sessionManager = new SessionManager(requireContext());
        // Use the AUTHENTICATED client to get profile details
        apiService = ApiClient.getBackendClient(requireContext()).create(ProfileApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the new, combined layout
        View view = inflater.inflate(R.layout.fragment_vendor_profile, container, false);

        // Toolbar logic
        ImageView btnBack = view.findViewById(R.id.btnBack);
        TextView toolbarTitle = view.findViewById(R.id.toolbarTitle);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        toolbarTitle.setText("Profile");
        btnSearch.setVisibility(View.GONE);
        btnBack.setOnClickListener(v -> requireActivity().finish());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Find all views ---
        usernameTextView = view.findViewById(R.id.usernameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        profileImage = view.findViewById(R.id.profileImage);
        profileProgressBar = view.findViewById(R.id.profileProgressBar);
        profileContentLayout = view.findViewById(R.id.profileContentLayout);
        recyclerView = view.findViewById(R.id.vendorProfileRecycler);

        // --- Load Business Profile Data ---
        loadBusinessProfile();

        // --- Setup RecyclerView ---
        setupSettingsList();
    }

    private void loadBusinessProfile() {
        // Get the businessId stored during login
        long businessId = sessionManager.getBusinessId(); // <-- Assumes you have this getter

        // Set email (we already have this from SessionManager)
        emailTextView.setText(sessionManager.getEmail());

        if (businessId == -1L) {
            // Handle error - no business ID found
            Toast.makeText(getContext(), "Error: Could not find business ID.", Toast.LENGTH_SHORT).show();
            profileProgressBar.setVisibility(View.GONE);
            profileContentLayout.setVisibility(View.VISIBLE); // Show default text
            usernameTextView.setText("Business Not Found");
            return;
        }

        // Show loader, hide content
        profileProgressBar.setVisibility(View.VISIBLE);
        profileContentLayout.setVisibility(View.GONE);

        // Make the API call
        apiService.getBusinessDetails(businessId).enqueue(new Callback<BusinessProfile>() {
            @Override
            public void onResponse(Call<BusinessProfile> call, Response<BusinessProfile> response) {
                profileProgressBar.setVisibility(View.GONE);
                profileContentLayout.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    BusinessProfile profile = response.body();

                    // Set Business Name
                    usernameTextView.setText(profile.getBusinessName());

                    // Load Business Logo using Glide
                    Glide.with(requireContext())
                            .load(profile.getBusinessImage())
                            .placeholder(R.drawable.profileimg) // Default image
                            .error(R.drawable.profileimg) // Image on error
                            .into(profileImage);

                } else {
                    Log.e("VendorProfile", "API error: " + response.code());
                    Toast.makeText(getContext(), "Could not load profile.", Toast.LENGTH_SHORT).show();
                    usernameTextView.setText(sessionManager.getFullName()); // Fallback
                }
            }

            @Override
            public void onFailure(Call<BusinessProfile> call, Throwable t) {
                profileProgressBar.setVisibility(View.GONE);
                profileContentLayout.setVisibility(View.VISIBLE);
                Log.e("VendorProfile", "Network failure: " + t.getMessage());
                Toast.makeText(getContext(), "Network error.", Toast.LENGTH_SHORT).show();
                usernameTextView.setText(sessionManager.getFullName()); // Fallback
            }
        });
    }

    private void setupSettingsList() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        vendorSettingItems = new ArrayList<>();
        vendorSettingItems.add(new SettingItem("Profile", R.drawable.profile));
        vendorSettingItems.add(new SettingItem("Payment Methods", R.drawable.wallet));
        vendorSettingItems.add(new SettingItem("Switch to Customer", R.drawable.ic_swap));
        vendorSettingItems.add(new SettingItem("Logout", R.drawable.logout));

        vendorSettingAdapter = new SettingsAdapter(requireContext(), vendorSettingItems, (item, position) -> {
            Log.d("VendorProfileFragment", "Item clicked: " + item.getLabel());
            switch (item.getLabel()) {
                case "Payment Methods":
                    showPaymentMethod();
                    break;
                case "Logout":
                    logoutUser();
                    break;
                case "Profile":
                    Toast.makeText(requireContext(), "Navigating to Profile", Toast.LENGTH_SHORT).show();
                    break;
                case "Switch to Customer":
                    switchToCustomerUI();
                    break;
                default:
                    Toast.makeText(requireContext(), "Clicked: " + item.getLabel(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        recyclerView.setAdapter(vendorSettingAdapter);
    }

    // --- Helper methods ---

    private void showPaymentMethod() {
        Intent intent = new Intent(requireContext(), PaymentMethodActivity.class);
        startActivity(intent);
    }

    private void switchToCustomerUI() {
        Intent intent = new Intent(requireContext(), CustomerMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void logoutUser() {
        sessionManager.logoutUser();
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    // You can remove showLogoutDialog if you call logoutUser() directly
}