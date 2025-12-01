package com.example.dishora.vendorUI.menuTab;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.vendorUI.menuTab.adapter.MenuAdapter;
import com.example.dishora.vendorUI.menuTab.productAddUpdate.ProductFormFragment;
import com.example.dishora.vendorUI.menuTab.model.Product;
import com.example.dishora.network.ApiClient;
import com.example.dishora.vendorUI.menuTab.api.ProductApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorMenuFragment extends Fragment {

    private RecyclerView recyclerMenu;
    private MenuAdapter menuAdapter;
    private List<Product> productList = new ArrayList<>();

    public VendorMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_menu, container, false);

        // Toolbar
        ImageView btnBack = view.findViewById(R.id.btnBack);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        TextView toolbarTitle = view.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Menu");

        // Back button
        btnBack.setOnClickListener(v -> {
            // Get the bottom nav from the Activity
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.bottomNavigationView);
            // Trigger selecting the Home tab
            bottomNav.setSelectedItemId(R.id.home);
        });

        // RecyclerView setup
        recyclerMenu = view.findViewById(R.id.recyclerMenu);
        recyclerMenu.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns
        menuAdapter = new MenuAdapter(requireContext(), productList);
        recyclerMenu.setAdapter(menuAdapter);

        // Load products from API
        loadProducts();

        // Add Item button
        Button btnAddItem = view.findViewById(R.id.btnAddItem);
        btnAddItem.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment_container, new ProductFormFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadProducts() {
        ProductApiService api = ApiClient.getBackendClient().create(ProductApiService.class);

        // ✅ Load the logged-in vendor’s business_id from SharedPreferences
        long businessId = requireContext()
                .getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                .getLong("business_id", -1);

        if (businessId == -1) {
            Toast.makeText(requireContext(), "Invalid business session", Toast.LENGTH_LONG).show();
            return; // stop here if no business_id in prefs
        }

        api.getVendorProducts(businessId).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    menuAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
