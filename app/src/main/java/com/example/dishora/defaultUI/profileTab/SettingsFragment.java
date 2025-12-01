package com.example.dishora.defaultUI.profileTab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.adapters.SettingsAdapter;
import com.example.dishora.defaultUI.profileTab.startSellingOption.StartSellingActivity;
import com.example.dishora.models.SettingItem;
import com.example.dishora.models.VendorStatusResponse;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.ApiService;
import com.example.dishora.utils.SessionManager; // ✅ IMPORT SESSION MANAGER
import com.example.dishora.vendorUI.VendorMainActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment {

    private RecyclerView recyclerView;
    private SettingsAdapter adapter;
    private List<SettingItem> settingItems = new ArrayList<>();
    private SessionManager sessionManager; // ✅ Add SessionManager instance

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext()); // ✅ Initialize SessionManager

        recyclerView = view.findViewById(R.id.profileRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SettingsAdapter(requireContext(), settingItems, (item, position) -> {
            switch (item.getLabel()) {
                case "Start Selling":
                    startSelling();
                    break;
                case "My Vendor Status":
                    fetchAndShowVendorStatus(); // shows popup
                    break;
                case "Vendor Dashboard":
                    openVendorDashboard();
                    break;
                case "Logout":
                    logoutUser();
                    break;
                case "Profile":
                    Toast.makeText(requireContext(), "Navigating to Profile", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(requireContext(), "Clicked: " + item.getLabel(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
        recyclerView.setAdapter(adapter);

        // ✅ Step 1 → Build menu immediately using SessionManager
        buildMenu(sessionManager.isVendor());

        // Step 2 → Refresh from backend
        fetchVendorStatusFromServer();
    }

    private void buildMenu(boolean isVendor) {
        settingItems.clear();

        settingItems.add(new SettingItem("Profile", R.drawable.profile));
        settingItems.add(new SettingItem("Payment Option", R.drawable.wallet));
        settingItems.add(new SettingItem("Notifications", R.drawable.notification__profile));
        settingItems.add(new SettingItem("Security", R.drawable.ic_security));

        // ✅ Get vendor status from SessionManager
        String vendorStatus = sessionManager.getVendorStatus();

        if (!isVendor) {
            settingItems.add(new SettingItem("Start Selling", R.drawable.selling));
        } else if (vendorStatus.equalsIgnoreCase("Approved")) {
            settingItems.add(new SettingItem("Vendor Dashboard", R.drawable.ic_swap));
        } else {
            // Pending or rejected
            settingItems.add(new SettingItem("My Vendor Status", R.drawable.status));
        }

        settingItems.add(new SettingItem("Logout", R.drawable.logout));

        adapter.notifyDataSetChanged();
    }

    private void startSelling() {
        Intent i = new Intent(requireContext(), StartSellingActivity.class);
        startActivity(i);
    }

    private void logoutUser() {
        // ✅ NEW, CLEANER CODE
        // The SessionManager handles clearing prefs, resetting the CartManager, and navigating to Login.
        sessionManager.logoutUser();

        // Finish the hosting activity so the user can't press "back" to return.
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    /**
     * Backend refresh – updates prefs and menu.
     */
    private void fetchVendorStatusFromServer() {
        // (This method remains the same as it writes specific, fresh data to SharedPreferences,
        // which is a valid use case. The SessionManager will read this updated data next time.)
        ApiService api = ApiClient.getBackendClient(requireContext()).create(ApiService.class);
        api.getVendorStatus().enqueue(new Callback<VendorStatusResponse>() {
            @Override
            public void onResponse(Call<VendorStatusResponse> call, Response<VendorStatusResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    boolean isVendor = response.body().isVendor();
                    String status = response.body().getStatus();
                    if (status == null) status = "Pending";

                    Context ctx = getContext();
                    if (ctx == null) return;

                    SharedPreferences prefs = ctx.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("isVendor", isVendor)
                            .putString("vendorStatus", status)
                            .apply();

                    buildMenu(isVendor);
                }
            }

            @Override
            public void onFailure(Call<VendorStatusResponse> call, Throwable t) {
                if (!isAdded()) return;
                Context ctx = getContext();
                if (ctx != null) {
                    Toast.makeText(ctx, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Popup showing vendor status
     */
    private void fetchAndShowVendorStatus() {
        // (This method also remains unchanged for the same reasons as above)
        ApiService api = ApiClient.getBackendClient(requireContext()).create(ApiService.class);
        api.getVendorStatus().enqueue(new Callback<VendorStatusResponse>() {
            @Override
            public void onResponse(Call<VendorStatusResponse> call, Response<VendorStatusResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    String status = response.body().getStatus();
                    if (status == null) status = "Pending";
                    boolean isVendor = response.body().isVendor();

                    Context ctx = getContext();
                    if (ctx == null) return;

                    SharedPreferences prefs = ctx.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("isVendor", isVendor)
                            .putString("vendorStatus", status)
                            .apply();

                    int iconRes;
                    switch (status.toLowerCase()) {
                        case "approved":
                            iconRes = R.drawable.ic_approved;
                            break;
                        case "rejected":
                            iconRes = R.drawable.ic_rejected;
                            break;
                        default:
                            iconRes = R.drawable.ic_pending;
                            break;
                    }

                    new androidx.appcompat.app.AlertDialog.Builder(ctx)
                            .setTitle("Vendor Registration Status")
                            .setMessage("Your current status: " + status)
                            .setIcon(iconRes)
                            .setPositiveButton("OK", (dialog, which) -> buildMenu(isVendor))
                            .show();
                }
            }

            @Override
            public void onFailure(Call<VendorStatusResponse> call, Throwable t) {
                if (!isAdded()) return;
                Context ctx = getContext();
                if (ctx != null) {
                    Toast.makeText(ctx, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void openVendorDashboard() {
        Intent i = new Intent(requireContext(), VendorMainActivity.class);
        startActivity(i);
    }
}