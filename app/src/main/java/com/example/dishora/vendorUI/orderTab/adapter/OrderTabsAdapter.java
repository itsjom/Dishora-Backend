package com.example.dishora.vendorUI.orderTab.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.dishora.vendorUI.orderTab.VendorsOrderListFragment;

public class OrderTabsAdapter extends FragmentStateAdapter {

    private final String[] tabTitles;

    public OrderTabsAdapter(@NonNull Fragment fragment, String[] tabTitles) {
        super(fragment);
        this.tabTitles = tabTitles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Create a new instance of VendorsOrderListFragment per tab
        return VendorsOrderListFragment.newInstance(tabTitles[position]);
    }

    @Override
    public int getItemCount() {
        return tabTitles.length;
    }
}