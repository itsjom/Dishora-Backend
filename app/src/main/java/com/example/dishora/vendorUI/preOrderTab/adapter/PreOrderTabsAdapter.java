package com.example.dishora.vendorUI.preOrderTab.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.dishora.vendorUI.preOrderTab.VendorPreOrderListFragment;

public class PreOrderTabsAdapter extends FragmentStateAdapter {

    private final String[] tabTitles;

    public PreOrderTabsAdapter(@NonNull Fragment fragment, String[] tabTitles) {
        super(fragment);
        this.tabTitles = tabTitles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return VendorPreOrderListFragment.newInstance(tabTitles[position]);
    }

    @Override
    public int getItemCount() {
        return tabTitles.length;
    }
}