package com.example.dishora.defaultUI.vendorsTab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar; // <-- Import this
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.defaultUI.vendorsTab.adapter.VendorAdapter;
import com.example.dishora.defaultUI.vendorsTab.api.VendorApiService;
import com.example.dishora.defaultUI.vendorsTab.model.Vendor;
import com.example.dishora.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorsFragment extends Fragment {

    private RecyclerView vendorRecyclerView;
    private VendorAdapter vendorAdapter;
    private List<Vendor> vendorList;
    private ProgressBar progressBar; // <-- Add this variable

    public VendorsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vendors, container, false);
        vendorRecyclerView = view.findViewById(R.id.vendorsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar); // <-- Find the ProgressBar

        // Toolbar
        ImageView btnBack = view.findViewById(R.id.btnBack);
        TextView toolbarTitle = view.findViewById(R.id.toolbarTitle);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        toolbarTitle.setText("Vendors");

        // Back button
        btnBack.setOnClickListener(v -> {
            // Get the bottom nav from the Activity
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.customerBottomNavigationView);
            // Trigger selecting the Home tab
            bottomNav.setSelectedItemId(R.id.home);
        });

        vendorList = new ArrayList<>();
        vendorAdapter = new VendorAdapter(vendorList, getActivity());
        vendorRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        vendorRecyclerView.setAdapter(vendorAdapter);

        // ✅ Load real vendors from API
        loadVendors();

        return view;
    }

    private void loadVendors() {
        // Show progress bar and hide the list
        progressBar.setVisibility(View.VISIBLE);
        vendorRecyclerView.setVisibility(View.GONE);

        VendorApiService api = ApiClient.getBackendClient().create(VendorApiService.class);

        api.getVendors().enqueue(new Callback<List<Vendor>>() {
            @Override
            public void onResponse(Call<List<Vendor>> call, Response<List<Vendor>> response) {
                // Hide progress bar regardless of outcome
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    // Show the list
                    vendorRecyclerView.setVisibility(View.VISIBLE);

                    vendorList.clear();
                    vendorList.addAll(response.body());
                    vendorAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Failed to load vendors", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Vendor>> call, Throwable t) {
                // Hide progress bar on failure
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}