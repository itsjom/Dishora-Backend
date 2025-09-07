package com.example.dishora.defaultUI.profileTab;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dishora.R;

public class ProfileFragment extends Fragment {

    private TextView userTextView, emailTextView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userTextView = view.findViewById(R.id.usernameTextView); // Make sure your XML has this ID
        emailTextView = view.findViewById(R.id.emailTextView);       // And this one too

        // Load from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        String email = prefs.getString("email", "your@email.com");

        // Apply to UI
        userTextView.setText(capitalizeFirstLetter(username));
        emailTextView.setText(email);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load SettingsFragment inside HomeFragment
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.child_fragment_container, new SettingsFragment())
                .commit();
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}