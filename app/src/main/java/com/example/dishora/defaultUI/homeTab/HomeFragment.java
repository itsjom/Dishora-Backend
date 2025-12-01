package com.example.dishora.defaultUI.homeTab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.ProgressBar; // <-- ADD THIS IMPORT
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.dishora.R;
import com.example.dishora.adapters.ImageAdapter;
import com.example.dishora.defaultUI.homeTab.vendorSection.VendorAdapter;
import com.example.dishora.defaultUI.homeTab.cart.CustomerCartActivity;
import com.example.dishora.defaultUI.homeTab.categorySection.CategoryFrag;
import com.example.dishora.defaultUI.homeTab.featuredDealsSection.FeatureCategoryFrag;
import com.example.dishora.defaultUI.homeTab.search.SearchFragment;
import com.example.dishora.defaultUI.homeTab.search.filter.FilterFragment;
import com.example.dishora.defaultUI.homeTab.vendorSection.Vendor;
import com.example.dishora.network.ApiClient;
import com.example.dishora.defaultUI.homeTab.vendorSection.api.VendorNearbyApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    // (Your other variables are unchanged)
    private SearchView searchView;
    private LinearLayout categoryContainer, featuredContainer;
    private CircleIndicator3 indicator;
    private ViewPager2 viewPager;
    private ImageAdapter imageAdapter;
    private List<String> imageList;
    private Handler handler = new Handler();
    private Runnable carouselRunnable;
    private int currentPage = 0;
    private TextView toolbarTitle;
    private final String[] categories = {"All", "Gluten-free", "Vegetarian", "Vegan", "Buffet", "Drinks", "Desserts", "Specials"};
    private final String[] featureCategories = {"All", "Platter", "Combo", "Family", "Solo"};
    private TextView selectedCategoryBtn = null;
    private TextView selectedFeatureBtn = null;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private double userLatitude = 0.0;
    private double userLongitude = 0.0;

    // VENDORS
    private RecyclerView vendorRecyclerView;
    private VendorAdapter vendorAdapter;
    private List<Vendor> vendorList;
    private ProgressBar vendorProgressBar; // <-- ADD THIS

    // API
    private VendorNearbyApi apiService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ApiService using your ApiClient (the no-auth version)
        apiService = ApiClient.getBackendClient().create(VendorNearbyApi.class);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize the permission request launcher
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        // Permission is granted. Get the location.
                        getDeviceLocation();
                    } else {
                        // Permission denied.
                        Toast.makeText(getContext(), "Permission denied. Showing all vendors.", Toast.LENGTH_SHORT).show();
                        // Call the vendor setup without location
                        setupVendorList();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Toolbar
        toolbarTitle = view.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Profile");

        SharedPreferences prefs = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        toolbarTitle.setText("Hi, " + capitalizeFirstLetter(username));

        viewPager = view.findViewById(R.id.viewPager1);
        indicator = view.findViewById(R.id.cardIndicator);
        indicator.setViewPager(viewPager);

        categoryContainer = view.findViewById(R.id.categoryBtnContainer);
        featuredContainer = view.findViewById(R.id.featuredBtnContainer);
        vendorRecyclerView = view.findViewById(R.id.vendorRecyclerView);
        vendorProgressBar = view.findViewById(R.id.vendorProgressBar); // <-- FIND THE VIEW

        setupCarousel();
        setupCategoryButtons();
        setupFeatureButtons();

        checkLocationPermissionAndFetchVendors();

        // (Your listeners are unchanged)
        EditText homeSearchEditText = view.findViewById(R.id.searchEditText);
        ImageButton btnHomeFilter = view.findViewById(R.id.btnFilter);

        ImageView ivCart = view.findViewById(R.id.ivCart);
        ivCart.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CustomerCartActivity.class);
            startActivity(intent);
        });

        ImageView ivNotification = view.findViewById(R.id.ivNotification);
        ivNotification.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Notifications clicked", Toast.LENGTH_SHORT).show();
        });

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

        btnHomeFilter.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FilterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void checkLocationPermissionAndFetchVendors() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation();
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        userLatitude = location.getLatitude();
                        userLongitude = location.getLongitude();
                    } else {
                        Toast.makeText(getContext(), "Could not get location. Using default.", Toast.LENGTH_SHORT).show();
                    }
                    setupVendorList();
                });
    }

    private void openSearchFragment(String query) {
        SearchFragment searchFragment = new SearchFragment();

        Bundle bundle = new Bundle();
        bundle.putString("search_query", query);
        searchFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupCarousel() {
        // (Unchanged)
        imageList = new ArrayList<>();
        imageList.add("https...jpg");
        imageList.add("https...jpg");
        imageList.add("https...jpeg");

        imageAdapter = new ImageAdapter(requireContext(), imageList);
        viewPager.setAdapter(imageAdapter);
        indicator.setViewPager(viewPager);
        autoScrollCarousel();
    }

    private void autoScrollCarousel() {
        // (Unchanged)
        carouselRunnable = () -> {
            if (currentPage >= imageList.size()) currentPage = 0;
            viewPager.setCurrentItem(currentPage++, true);
            handler.postDelayed(carouselRunnable, 5000);
        };
        handler.postDelayed(carouselRunnable, 5000);
    }

    private void setupCategoryButtons() {
        // (Unchanged)
        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            TextView button = createStyledButton(category);
            if (i == 0) selectCategory(button, category);
            button.setOnClickListener(v -> selectCategory(button, category));
            categoryContainer.addView(button);
        }
    }

    private void setupFeatureButtons() {
        // (Unchanged)
        for (int i = 0; i < featureCategories.length; i++) {
            String feature = featureCategories[i];
            TextView button = createStyledButton(feature);
            if (i == 0) selectFeature(button, feature);
            button.setOnClickListener(v -> selectFeature(button, feature));
            featuredContainer.addView(button);
        }
    }

    /**
     * ##### THIS METHOD IS NOW UPDATED #####
     * It now shows the progress bar before the call and hides it on success/failure.
     */
    private void setupVendorList() {
        // 1. Initialize the list and adapter first (empty)
        vendorList = new ArrayList<>();
        vendorAdapter = new VendorAdapter(getContext(), vendorList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        vendorRecyclerView.setLayoutManager(layoutManager);
        vendorRecyclerView.setAdapter(vendorAdapter);

        // 2. Log what we're fetching
        if (userLatitude != 0.0 && userLongitude != 0.0) {
            Log.d("HomeFragment", "Fetching vendors near: " + userLatitude + ", " + userLongitude);
        } else {
            Log.d("HomeFragment", "Fetching default vendors (lat/lon is 0).");
        }

        // 3. START LOADING: Show progress bar, hide list
        vendorProgressBar.setVisibility(View.VISIBLE);
        vendorRecyclerView.setVisibility(View.GONE);

        // 4. Make the network call
        Call<List<Vendor>> call = apiService.getNearbyVendors(userLatitude, userLongitude);

        call.enqueue(new Callback<List<Vendor>>() {
            @Override
            public void onResponse(Call<List<Vendor>> call, Response<List<Vendor>> response) {
                // STOP LOADING: Hide progress bar
                vendorProgressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    // SUCCESS!
                    vendorList.clear();
                    vendorList.addAll(response.body());
                    vendorAdapter.notifyDataSetChanged();

                    // Show the list
                    vendorRecyclerView.setVisibility(View.VISIBLE);

                    Log.d("HomeFragment", "Found " + response.body().size() + " vendors.");
                } else {
                    // API returned an error (e.g., 404, 500)
                    Log.e("HomeFragment", "API Error: " + response.code() + " - " + response.message());
                    Toast.makeText(getContext(), "Could not load vendors.", Toast.LENGTH_SHORT).show();
                    // Keep list GONE
                }
            }

            @Override
            public void onFailure(Call<List<Vendor>> call, Throwable t) {
                // STOP LOADING: Hide progress bar
                vendorProgressBar.setVisibility(View.GONE);

                // Network call itself failed (e.g., no internet, DNS error)
                Log.e("HomeFragment", "Network Failure: " + t.getMessage());
                Toast.makeText(getContext(), "Network error. Check connection.", Toast.LENGTH_SHORT).show();
                // Keep list GONE
            }
        });
    }

    private TextView createStyledButton(String text) {
        // (Unchanged)
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
        // (Unchanged)
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
        // (Unchanged)
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
        // (Unchanged)
        super.onPause();
        handler.removeCallbacks(carouselRunnable);
    }

    private String capitalizeFirstLetter(String input) {
        // (Unchanged)
        if (input == null || input.isEmpty()) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

}