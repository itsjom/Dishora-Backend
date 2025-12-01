package com.example.dishora.defaultUI.homeTab.search.filter.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

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

/**
 * Controller that binds the Filter UI (inside an included layout) and provides all logic
 * previously living in FilterFragment.
 *
 * Required IDs inside the included layout:
 *  - Button:       tabDiet, tabCalories, tabHalal
 *  - Button:       applyButton, clearButton
 *  - ChipGroup:    selectedFiltersContainer
 *  - LinearLayout: filtersCheckboxContainer
 *  - (Optional) ImageButton: buttonBack  -> will call onClose.run() if present
 */
public class FilterSectionController {

    public interface OnFiltersApplied {
        void onApplied(ArrayList<String> selected);
    }

    private final View root;
    private final OnFiltersApplied onApply;
    private final Runnable onClose;

    private Button tabDiet, tabCalories, tabHalal, btnApply, btnClear;
    private ChipGroup selectedFiltersContainer;
    private LinearLayout filtersCheckboxContainer;

    // Selections + data
    private final Set<String> selectedFilters = new HashSet<>();
    private final List<String> dietFilters = new ArrayList<>();
    private final List<String> caloriesFilters = new ArrayList<>();
    private final List<String> halalFilters = new ArrayList<>();
    private String currentCategory = "Diet";

    public FilterSectionController(View rootIncludedLayout,
                                   OnFiltersApplied onApply,
                                   Runnable onClose) {
        this.root = rootIncludedLayout;
        this.onApply = onApply;
        this.onClose = onClose;
        bindViews();
        wireEvents();
        // Default tab
        selectTab(tabDiet, "Diet");
        fetchFilters("Diet");
    }

    private void bindViews() {
        tabDiet = root.findViewById(R.id.tabDiet);
        tabCalories = root.findViewById(R.id.tabCalories);
        tabHalal = root.findViewById(R.id.tabHalal);

        btnApply = root.findViewById(R.id.applyButton);
        btnClear = root.findViewById(R.id.clearButton);

        selectedFiltersContainer = root.findViewById(R.id.selectedFiltersContainer);
        filtersCheckboxContainer = root.findViewById(R.id.filtersCheckboxContainer);

        ImageButton buttonBack = root.findViewById(R.id.buttonBack);
        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> {
                if (onClose != null) onClose.run();
            });
        }
    }

    private void wireEvents() {
        tabDiet.setOnClickListener(v -> {
            selectTab(tabDiet, "Diet");
            fetchFilters("Diet");
        });
        tabCalories.setOnClickListener(v -> {
            selectTab(tabCalories, "Calories");
            fetchFilters("Calories");
        });
        tabHalal.setOnClickListener(v -> {
            selectTab(tabHalal, "Halal");
            fetchFilters("Halal");
        });

        btnApply.setOnClickListener(v -> applyFilters());
        btnClear.setOnClickListener(v -> clearFilters());
    }

    private void applyFilters() {
        if (onApply != null) onApply.onApplied(new ArrayList<>(selectedFilters));
        if (onClose != null) onClose.run();
    }

    private void clearFilters() {
        selectedFilters.clear();
        updateChips();

        // Uncheck all checkboxes
        for (int i = 0; i < filtersCheckboxContainer.getChildCount(); i++) {
            View child = filtersCheckboxContainer.getChildAt(i);
            CheckBox checkBox = child.findViewById(R.id.checkBoxFilter);
            if (checkBox != null) checkBox.setChecked(false);
        }
    }

    private void fetchFilters(String category) {
        FilterApiService apiService = ApiClient.getBackendClient().create(FilterApiService.class);
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
                    Toast.makeText(root.getContext(), "No filters found", Toast.LENGTH_SHORT).show();
                    loadFilters(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(root.getContext(), "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadFilters(new ArrayList<>());
            }
        });
    }

    private void selectTab(Button selectedTab, String category) {
        Button[] allTabs = {tabDiet, tabCalories, tabHalal};

        // UNSELECTED TABS
        for (Button tab : allTabs) {
            tab.setSelected(false);
            tab.setBackgroundColor(ContextCompat.getColor(root.getContext(), android.R.color.white));
            tab.setTextColor(ContextCompat.getColor(root.getContext(), R.color.black));
        }

        // SELECTED TABS
        selectedTab.setSelected(true);
        selectedTab.setBackgroundColor(ContextCompat.getColor(root.getContext(), R.color.colorPrimary));
        selectedTab.setTextColor(ContextCompat.getColor(root.getContext(), R.color.white));
        currentCategory = category; // <- reliable, don’t depend on text color
    }

    private void loadFilters(List<String> filters) {
        filtersCheckboxContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(root.getContext());

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
        updateChips();
    }

    private void updateChips() {
        selectedFiltersContainer.removeAllViews();
        for (String filter : new ArrayList<>(selectedFilters)) {
            Chip chip = new Chip(root.getContext());

            // style
            try {
                ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(
                        root.getContext(), null, 0, R.style.FilterChipStyle
                );
                chip.setChipDrawable(chipDrawable);
            } catch (Exception ignored) {
                // If style missing, still render a default chip
            }

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
        switch (currentCategory.toLowerCase()) {
            case "calories": return caloriesFilters;
            case "halal":    return halalFilters;
            default:         return dietFilters;
        }
    }

    // Optional helper if you want to preset some chips from outside
    public void setPreselected(List<String> presets) {
        selectedFilters.clear();
        if (presets != null) selectedFilters.addAll(presets);
        updateChips();
        loadFilters(getCurrentTabFilters());
    }
}

