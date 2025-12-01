package com.example.dishora.defaultUI.homeTab.search.filter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dishora.R;
import com.example.dishora.defaultUI.homeTab.search.filter.api.FilterApiService;
import com.example.dishora.network.ApiClient;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilterFragment extends Fragment {

    private Button tabDiet, tabCalories, tabHalal, btnApply, btnClear;
    private ChipGroup selectedFiltersContainer;
    private LinearLayout filtersCheckboxContainer;
    private final Set<String> selectedFilters = new HashSet<>();

    // Lists from API
    private List<String> dietFilters = new ArrayList<>();
    private List<String> caloriesFilters = new ArrayList<>();
    private List<String> halalFilters = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container, false);

        tabDiet = view.findViewById(R.id.tabDiet);
        tabCalories = view.findViewById(R.id.tabCalories);
        tabHalal = view.findViewById(R.id.tabHalal);

        selectedFiltersContainer = view.findViewById(R.id.selectedFiltersContainer);
        filtersCheckboxContainer = view.findViewById(R.id.filtersCheckboxContainer);

        btnApply = view.findViewById(R.id.applyButton);
        btnClear = view.findViewById(R.id.clearButton);

        // Default tab
        selectTab(tabDiet);
        fetchFilters("Diet");

        tabDiet.setOnClickListener(v -> {
            selectTab(tabDiet);
            fetchFilters("Diet");
        });

        tabCalories.setOnClickListener(v -> {
            selectTab(tabCalories);
            fetchFilters("Calories");
        });

        tabHalal.setOnClickListener(v -> {
            selectTab(tabHalal);
            fetchFilters("Halal");
        });

        ImageButton buttonBack = view.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> requireActivity().onBackPressed());

        btnApply.setOnClickListener(v -> applyFilters());
        btnClear.setOnClickListener(v -> clearFilters());

        return view;
    }

    private void applyFilters() {
        Bundle result = new Bundle();
        result.putStringArrayList("selected_filters", new ArrayList<>(selectedFilters));
        getParentFragmentManager().setFragmentResult("filter_result", result);

        // Go back to the previous fragment
        getParentFragmentManager().popBackStack();
    }

    private void clearFilters() {
        selectedFilters.clear();
        updateChips();

        // Uncheck all checkboxes
        for (int i = 0; i < filtersCheckboxContainer.getChildCount(); i++) {
            View child = filtersCheckboxContainer.getChildAt(i);
            CheckBox checkBox = child.findViewById(R.id.checkBoxFilter);
            checkBox.setChecked(false);
        }
    }

    private void fetchFilters(String category) {
        // ✅ Pass context here
        FilterApiService apiService = ApiClient
                .getBackendClient()
                .create(FilterApiService.class);

        apiService.getFilters(category).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> filters = response.body();
                    switch (category.toLowerCase()) {
                        case "diet":
                            dietFilters.clear();
                            dietFilters.addAll(filters);
                            break;
                        case "calories":
                            caloriesFilters.clear();
                            caloriesFilters.addAll(filters);
                            break;
                        case "halal":
                            halalFilters.clear();
                            halalFilters.addAll(filters);
                            break;
                    }
                    loadFilters(filters);
                } else {
                    Toast.makeText(requireContext(), "No filters found", Toast.LENGTH_SHORT).show();
                    loadFilters(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(requireContext(), "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadFilters(new ArrayList<>());
            }
        });
    }

    private void selectTab(Button selectedTab) {
        Button[] allTabs = {tabDiet, tabCalories, tabHalal};

        for (Button tab : allTabs) {
            tab.setSelected(false);  // Unselect all
        }

        selectedTab.setSelected(true);  // Select the clicked one
    }


    private void loadFilters(List<String> filters) {
        filtersCheckboxContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (String filter : filters) {
            View itemView = inflater.inflate(R.layout.item_filter_checkbox, filtersCheckboxContainer, false);
            CheckBox checkBox = itemView.findViewById(R.id.checkBoxFilter);
            checkBox.setText(filter);
            checkBox.setChecked(selectedFilters.contains(filter));
            checkBox.setBackground(null);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedFilters.add(filter);
                else selectedFilters.remove(filter);
                updateChips();
            });

            filtersCheckboxContainer.addView(itemView);
        }
    }

    private void updateChips() {
        selectedFiltersContainer.removeAllViews();
        for (String filter : new ArrayList<>(selectedFilters)) {
            Chip chip = new Chip(getContext());

            // Apply custom style via ChipDrawable
            ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(
                    getContext(),
                    null,
                    0,
                    R.style.FilterChipStyle // 🔹 your custom chip style
            );
            chip.setChipDrawable(chipDrawable);

            chip.setText(filter);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                selectedFilters.remove(filter);
                updateChips();
                loadFilters(getCurrentTabFilters());
            });

            selectedFiltersContainer.addView(chip);
        }
    }

    private List<String> getCurrentTabFilters() {
        if (tabCalories.getCurrentTextColor() == Color.WHITE) return caloriesFilters;
        if (tabHalal.getCurrentTextColor() == Color.WHITE) return halalFilters;
        return dietFilters;
    }
}
