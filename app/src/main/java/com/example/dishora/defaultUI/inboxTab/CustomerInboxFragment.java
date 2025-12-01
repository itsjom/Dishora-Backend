package com.example.dishora.defaultUI.inboxTab;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dishora.R;
import com.example.dishora.defaultUI.inboxTab.chatTab.CustomerChatTabFragment;
import com.example.dishora.defaultUI.inboxTab.trashTab.CustomerTrashTabFragment;
import com.google.android.material.tabs.TabLayout;

public class CustomerInboxFragment extends Fragment {

    private TabLayout tabLayout;

    public CustomerInboxFragment() {
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
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        // Toolbar
        ImageView btnBack = view.findViewById(R.id.btnBack);
        TextView toolbarTitle = view.findViewById(R.id.toolbarTitle);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        toolbarTitle.setText("Inbox");

        // Back button
        btnBack.setOnClickListener(v -> {
            // Get the bottom nav from the Activity
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.customerBottomNavigationView);
            // Trigger selecting the Home tab
            bottomNav.setSelectedItemId(R.id.home);
        });

        tabLayout = view.findViewById(R.id.inboxTabLayout);

        // ✅ Add customer-specific tabs (different set!)
        tabLayout.addTab(tabLayout.newTab().setText("Chat"));
        tabLayout.addTab(tabLayout.newTab().setText("Trash"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment frag;
                if (tab.getPosition() == 0) {
                    frag = new CustomerChatTabFragment();
                } else {
                    frag = new CustomerTrashTabFragment();
                }
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.inboxTabContentContainer, frag)
                        .commit();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Default
        getChildFragmentManager().beginTransaction()
                .replace(R.id.inboxTabContentContainer, new CustomerChatTabFragment())
                .commit();
        return view;
    }
}