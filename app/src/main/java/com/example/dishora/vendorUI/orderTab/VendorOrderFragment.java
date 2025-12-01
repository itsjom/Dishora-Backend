package com.example.dishora.vendorUI.orderTab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.dishora.R;
import com.example.dishora.utils.TabTitles;
import com.example.dishora.vendorUI.orderTab.adapter.OrderTabsAdapter;
import com.example.dishora.vendorUI.orderTab.model.OrderViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class VendorOrderFragment extends Fragment {

    private final String[] tabTitles = TabTitles.ORDER_TABS;
    private OrderViewModel viewModel; // --- ADD THIS ---

    public VendorOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vendor_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1. INITIALIZE THE VIEWMODEL AND LOAD DATA ---
        // The ViewModel is scoped to THIS fragment's lifecycle.
        // Child fragments will get this same instance.
        viewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        // Initialize the ApiClient inside the ViewModel
        viewModel.init(requireContext().getApplicationContext());

        // Trigger the initial data load for all tabs
        viewModel.loadAllOrders();
        // ---------------------------------------------------

        // Get toolbar items
        ImageView btnBack = view.findViewById(R.id.btnBack);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        TextView toolbarTitle = view.findViewById(R.id.toolbarTitle);

        toolbarTitle.setText("Orders");
        btnSearch.setVisibility(View.GONE);

        btnBack.setOnClickListener(v -> {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.bottomNavigationView);
            bottomNav.setSelectedItemId(R.id.home);
        });

        // Tab + ViewPager2 setup
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        OrderTabsAdapter adapter = new OrderTabsAdapter(this, tabTitles);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }
}