package com.example.dishora.vendorUI.inboxTab;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dishora.R;
import com.example.dishora.defaultUI.inboxTab.trashTab.CustomerTrashTabFragment;
import com.example.dishora.vendorUI.inboxTab.chatTab.VendorChatTabFragment;
import com.google.android.material.tabs.TabLayout;

public class VendorInboxFragment extends Fragment {

    public VendorInboxFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        // Toolbar
        ImageView btnBack = view.findViewById(R.id.btnBack);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        TextView toolbarTitle = view.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Inbox");

        btnBack.setOnClickListener(v -> {
            // Get the bottom nav from the Activity
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.bottomNavigationView);

            // Trigger selecting the Home tab
            bottomNav.setSelectedItemId(R.id.home);
        });

        TabLayout tabLayout = view.findViewById(R.id.inboxTabLayout);

        // ✅ Add vendor-specific tabs
        tabLayout.addTab(tabLayout.newTab().setText("Chat"));
        tabLayout.addTab(tabLayout.newTab().setText("Feedback"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selected = (tab.getPosition() == 0)
                        ? new VendorChatTabFragment()
                        : new CustomerTrashTabFragment();

                getChildFragmentManager().beginTransaction()
                        .replace(R.id.inboxTabContentContainer, selected)
                        .commit();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Default → load Chat first
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.inboxTabContentContainer, new VendorChatTabFragment())
                    .commit();
        }

        return view;
    }
}