package com.example.dishora;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeatureCategoryFrag extends Fragment {

    private static final String ARG_CATEGORY = "category";
    private String categoryName;
    public static FeatureCategoryFrag newInstance(String category) {
        FeatureCategoryFrag fragment = new FeatureCategoryFrag();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryName = getArguments().getString(ARG_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView content = new TextView(getContext());
        content.setTextSize(18);
        content.setTextColor(Color.DKGRAY);
        content.setPadding(32, 64, 32, 64);
        content.setText("Showing content for: " + categoryName);
        return content;
    }
}