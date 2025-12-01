package com.example.dishora.vendorUI.preOrderTab;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dishora.R;
import com.example.dishora.utils.TabTitles;
import com.example.dishora.vendorUI.preOrderTab.model.PreOrderViewModel;
import com.example.dishora.vendorUI.preOrderTab.adapter.PreOrderTabsAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class VendorPreOrderFragment extends Fragment {

    private final String[] tabTitles = TabTitles.ORDER_TABS;
    private PreOrderViewModel viewModel;

    public VendorPreOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vendor_pre_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1. INITIALIZE THE VIEWMODEL AND LOAD DATA ---
        viewModel = new ViewModelProvider(this).get(PreOrderViewModel.class);
        viewModel.init(requireContext().getApplicationContext());
        viewModel.loadAllPreOrders();
        // ---------------------------------------------------

        // Get toolbar items
        ImageView btnBack = view.findViewById(R.id.btnBack);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        TextView toolbarTitle = view.findViewById(R.id.toolbarTitle);

        toolbarTitle.setText("Pre-Orders");
        btnSearch.setVisibility(View.GONE);

        btnBack.setOnClickListener(v -> {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.bottomNavigationView);
            bottomNav.setSelectedItemId(R.id.home);
        });

        // Tab + ViewPager2 setup
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        PreOrderTabsAdapter adapter = new PreOrderTabsAdapter(this, tabTitles);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }
}