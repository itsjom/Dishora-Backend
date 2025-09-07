package com.example.dishora.defaultUI.homeTab;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dishora.R;
import com.example.dishora.adapters.ImageAdapter;
import com.example.dishora.defaultUI.homeTab.categorySection.CategoryFrag;
import com.example.dishora.defaultUI.homeTab.featuredDealsSection.FeatureCategoryFrag;
import com.example.dishora.defaultUI.homeTab.search.SearchFragment;
import com.example.dishora.defaultUI.homeTab.search.filter.FilterFragment;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class HomeFragment extends Fragment {

    private SearchView searchView;
    private LinearLayout categoryContainer, featuredContainer;
    private CircleIndicator3 indicator;
    private ViewPager2 viewPager;
    private ImageAdapter imageAdapter;
    private List<String> imageList;
    private Handler handler = new Handler();
    private Runnable carouselRunnable;
    private int currentPage = 0;

    private final String[] categories = {"All", "Gluten-free", "Vegetarian", "Vegan", "Buffet", "Drinks", "Desserts", "Specials"};
    private final String[] featureCategories = {"All", "Platter", "Combo", "Family", "Solo"};

    private TextView selectedCategoryBtn = null;
    private TextView selectedFeatureBtn = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView greetingText = view.findViewById(R.id.greetTextView);
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        greetingText.setText("Hi, " + capitalizeFirstLetter(username));

//        searchView = view.findViewById(R.id.searchView);
//        setupSearchView();
//        customizeSearchView();

        // Find the search plate (the underline container inside SearchView)
//        View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_plate);

        // Remove the background (which draws the underline)
//        if (searchPlate != null) {
//            searchPlate.setBackground(null); // removes underline
//        }

//        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
//        if (closeButton != null) {
//            closeButton.setBackground(null); // removes ripple effect
//        }

        viewPager = view.findViewById(R.id.viewPager1);
        indicator = view.findViewById(R.id.cardIndicator);
        indicator.setViewPager(viewPager);

        categoryContainer = view.findViewById(R.id.categoryBtnContainer);
        featuredContainer = view.findViewById(R.id.featuredBtnContainer);

        setupCarousel();
        setupCategoryButtons();
        setupFeatureButtons();

        EditText homeSearchEditText = view.findViewById(R.id.searchEditText);
        ImageButton btnHomeFilter = view.findViewById(R.id.btnFilter);

        // typing listener
        homeSearchEditText.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                String query = homeSearchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    openSearchFragment(query);
                }
                return true;
            }
            return false;
        });

        // filter button click
        btnHomeFilter.setOnClickListener(v -> {
            // Example: navigate to filter fragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FilterFragment()) // change to your filter fragment
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void openSearchFragment(String query) {
        SearchFragment searchFragment = new SearchFragment();

        // Pass query to SearchFragment
        Bundle bundle = new Bundle();
        bundle.putString("search_query", query);
        searchFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit();
    }

//    private void performHomeSearch(String query) {
//        // You can either:
//        // 1. Start SearchFragment with the query
//        // 2. Or filter your RecyclerView directly
//        Bundle bundle = new Bundle();
//        bundle.putString("query", query);
//
//        SearchFragment searchFragment = new SearchFragment();
//        searchFragment.setArguments(bundle);
//
//        requireActivity().getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.fragment_container, searchFragment)
//                .addToBackStack(null)
//                .commit();
//    }

//    private void setupSearchView() {
//        // Make the SearchView clickable
//        searchView.setFocusable(false);
//        searchView.setClickable(true);
//        searchView.clearFocus();
//
//        // Click listener for entire SearchView
//        searchView.setOnClickListener(v -> {
//            // Hide search icon
//            searchView.setIconified(false);
//            searchView.setQueryHint(""); // optional: remove hint text too
//
//            // Also remove the search icon manually
//            int searchIconId = searchView.getContext()
//                    .getResources()
//                    .getIdentifier("android:id/search_mag_icon", null, null);
//            ImageView searchIcon = searchView.findViewById(searchIconId);
//            if (searchIcon != null) {
//                searchIcon.setImageDrawable(null); // removes icon image
//            }
//
//            openSearchFragment(""); // Your method to open SearchFragment
//        });
//
//        // Optional: Restore icon when losing focus
//        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus) {
//                int searchIconId = searchView.getContext()
//                        .getResources()
//                        .getIdentifier("android:id/search_mag_icon", null, null);
//                ImageView searchIcon = searchView.findViewById(searchIconId);
//                if (searchIcon != null) {
//                    searchIcon.setImageResource(android.R.drawable.ic_menu_search); // restore icon
//                }
//            }
//        });
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                // Bundle to send the search term
//                Bundle bundle = new Bundle();
//                bundle.putString("query", query);
//
//                SearchFragment searchFragment = new SearchFragment();
//                searchFragment.setArguments(bundle);
//
//                // Navigate to SearchFragment
//                requireActivity().getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.fragment_container, searchFragment)
//                        .addToBackStack(null)
//                        .commit();
//
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
//    }

//    private void customizeSearchView() {
//        // Remove underline (search_plate)
//        View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_plate);
//        if (searchPlate != null) {
//            searchPlate.setBackground(null);
//        }
//
//        // Remove ripple from close button
//        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
//        if (closeButton != null) {
//            closeButton.setBackground(null);
//        }
//
//        // Remove ripple from search icon as well
//        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
//        if (searchIcon != null) {
//            searchIcon.setBackground(null);
//        }
//
//        // Apply your custom rounded background to the whole SearchView
//        searchView.setBackgroundResource(R.drawable.rounded_searchview);
//
//        // Remove extra paddings (default SearchView adds them internally)
//        searchView.setIconifiedByDefault(false);
//        searchView.setQueryHint("Search..."); // optional
//    }

//    private void openSearchFragment(String query) {
//        Bundle bundle = new Bundle();
//        bundle.putString("query", query);
//
//        SearchFragment searchFragment = new SearchFragment();
//        searchFragment.setArguments(bundle);
//
//        requireActivity().getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.fragment_container, searchFragment) // Use your actual container ID
//                .addToBackStack(null)
//                .commit();
//    }


    private void setupCarousel() {
        imageList = new ArrayList<>();
        imageList.add("https://www.seriouseats.com/thmb/sNOqOuOaiILj05PSuunyT3FuyPY=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/Filipino-Features-Soups-and-Stews-1e81ba12ce10481caf3ff58981c347ab.jpg");
        imageList.add("https://www.seriouseats.com/thmb/bRjLgLzq4vIoDCjQcCuWg_bRAQo=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/Filipino-Features-Pulutan-f41edbc2a3f548f3bfe893160de7af1e.jpg");
        imageList.add("https://shef.com/homemade-food/wp-content/uploads/filipino-food-philippines-history-homemade.jpeg");

        imageAdapter = new ImageAdapter(requireContext(), imageList);
        viewPager.setAdapter(imageAdapter);
        indicator.setViewPager(viewPager);
        autoScrollCarousel();
    }

    private void autoScrollCarousel() {
        carouselRunnable = () -> {
            if (currentPage >= imageList.size()) currentPage = 0;
            viewPager.setCurrentItem(currentPage++, true);
            handler.postDelayed(carouselRunnable, 5000);
        };
        handler.postDelayed(carouselRunnable, 5000);
    }

    private void setupCategoryButtons() {
        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            TextView button = createStyledButton(category);

            if (i == 0) selectCategory(button, category);

            button.setOnClickListener(v -> selectCategory(button, category));
            categoryContainer.addView(button);
        }
    }

    private void setupFeatureButtons() {
        for (int i = 0; i < featureCategories.length; i++) {
            String feature = featureCategories[i];
            TextView button = createStyledButton(feature);

            if (i == 0) selectFeature(button, feature);

            button.setOnClickListener(v -> selectFeature(button, feature));
            featuredContainer.addView(button);
        }
    }

    private TextView createStyledButton(String text) {
        TextView button = new TextView(requireContext());
        button.setText(text);
        button.setTextSize(14);
        button.setPadding(40, 20, 40, 20);
        button.setTextColor(Color.BLACK);
        button.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_category_button));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(12, 12, 12, 12);
        button.setLayoutParams(params);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setElevation(6f);
            button.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 60f);
                }
            });
            button.setClipToOutline(true);
        }

        return button;
    }

    private void selectCategory(TextView newSelected, String category) {
        if (selectedCategoryBtn != null) {
            selectedCategoryBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_category_button));
        }
        newSelected.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.selected_category_background));
        selectedCategoryBtn = newSelected;

        CategoryFrag fragment = CategoryFrag.newInstance(category);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.categoryFrgmentContainer, fragment)
                .commit();
    }

    private void selectFeature(TextView newSelected, String category) {
        if (selectedFeatureBtn != null) {
            selectedFeatureBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_category_button));
        }
        newSelected.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.selected_category_background));
        selectedFeatureBtn = newSelected;

        FeatureCategoryFrag fragment = FeatureCategoryFrag.newInstance(category);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.featuredFrgmentContainer, fragment)
                .commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(carouselRunnable);
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

}
