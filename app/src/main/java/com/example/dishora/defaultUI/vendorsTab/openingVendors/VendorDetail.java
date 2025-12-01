package com.example.dishora.defaultUI.vendorsTab.openingVendors;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dishora.R;
import com.example.dishora.defaultUI.homeTab.cart.shopCart.ShopCartDetailActivity;
import com.example.dishora.defaultUI.homeTab.search.filter.controller.FilterSectionController;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.adapter.MenuAdapter;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.api.ProductApiService;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.model.Product;
import com.example.dishora.network.ApiClient;
import com.example.dishora.utils.chat.ChatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorDetail extends Fragment {

    private TextView storeName, storeRating;
    private ImageView vendorLogo;
    private RecyclerView recyclerMenu;
    private MenuAdapter menuAdapter;

    private View filtersLayout;
    private LinearLayout btnFilter;
    private FilterSectionController filterController;
    private boolean isExpanded = false;

    private Button btnMessageVendor;

    // ✅ make vendor info available everywhere in this fragment
    private String vendorName;
    private String vendorAddress;
    private String vendorLogoUrl;
    private long businessId;
    private long vendorUserId;

    public VendorDetail() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_detail, container, false);

        // Toolbar
        ImageView btnBack = view.findViewById(R.id.btnBack);
        TextView toolbarTitle = view.findViewById(R.id.toolbarTitle);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        toolbarTitle.setText("Vendor Shop");

        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Access header views
        View header = view.findViewById(R.id.vendorHeader);
        storeName   = header.findViewById(R.id.txtStoreName);
        storeRating = header.findViewById(R.id.vendorRating);
        vendorLogo  = header.findViewById(R.id.vendorLogo);
        btnMessageVendor = header.findViewById(R.id.messageBtn);

        recyclerMenu  = view.findViewById(R.id.menuRecyclerView);
        btnFilter     = view.findViewById(R.id.btnFilter);
        filtersLayout = view.findViewById(R.id.filtersLayout);

        if (getArguments() != null) {
            // pull values from navigation arguments
            vendorName    = getArguments().getString("name", "");
            String rating = getArguments().getString("rating", "");
            vendorLogoUrl = getArguments().getString("imageUrl", "");
            vendorAddress = getArguments().getString("address", "");
            businessId    = getArguments().getLong("businessId", -1);
            vendorUserId  = getArguments().getLong("vendorUserId", -1);

            Log.d("VendorDetail",
                    "Loaded vendor details → name: " + vendorName +
                            ", address: " + vendorAddress +
                            ", logoUrl: " + vendorLogoUrl +
                            ", businessId: " + businessId);

            // fill vendor header
            storeName.setText(vendorName);
            storeRating.setText(rating);

            Glide.with(this)
                    .load(vendorLogoUrl)
                    .placeholder(R.drawable.ic_vendor_placeholder)
                    .error(R.drawable.ic_vendor_placeholder)
                    .into(vendorLogo);

            // load product list
            loadStoreDetails(businessId);
        }

        btnMessageVendor.setOnClickListener(v -> {
            if (vendorUserId != -1) {
                // Create an Intent to start ChatActivity
                Intent intent = new Intent(getActivity(), ChatActivity.class);

                // Pass the vendor's info as the recipient
                intent.putExtra("RECIPIENT_USER_ID", String.valueOf(vendorUserId));
                intent.putExtra("RECIPIENT_NAME", vendorName);

                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Could not start chat. Vendor info missing.", Toast.LENGTH_SHORT).show();
            }
        });

        btnFilter.setOnClickListener(v -> {
            if (!isExpanded) {
                expand(filtersLayout);
                isExpanded = true;
                if (filterController == null) {
                    filterController = new FilterSectionController(
                            filtersLayout,
                            selected -> Toast.makeText(getContext(),
                                    "Selected: " + selected, Toast.LENGTH_SHORT).show(),
                            () -> collapse(filtersLayout)
                    );
                }
            } else {
                collapse(filtersLayout);
                isExpanded = false;
            }
        });

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean hideToolbar = requireActivity().getIntent().getBooleanExtra("hideToolbar", false);
        if (hideToolbar) {
            View toolbar = view.findViewById(R.id.toolbarVendor);
            if (toolbar != null) toolbar.setVisibility(View.GONE);
        }
    }

    private void loadStoreDetails(long businessId) {
        ProductApiService api = ApiClient.getBackendClient().create(ProductApiService.class);

        api.getCustomerProducts(businessId).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();

                    // ✅ now these fields (vendorName, vendorAddress, vendorLogoUrl) are valid here
                    menuAdapter = new MenuAdapter(
                            requireActivity(),
                            products,
                            vendorName,
                            vendorAddress,
                            vendorLogoUrl,
                            businessId
                    );

                    recyclerMenu.setLayoutManager(new GridLayoutManager(getContext(), 1));
                    recyclerMenu.setAdapter(menuAdapter);

                    if (products.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "No available products at the moment", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(),
                            "Failed to load vendor products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(requireContext(),
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- expand / collapse animations ---
    private void expand(View view) {
        view.setVisibility(View.VISIBLE);
        view.setPivotY(0f);
        view.setPivotX(view.getWidth() / 2f);
        view.setAlpha(0f);
        view.setScaleY(0f);
        view.animate()
                .alpha(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();
    }

    private void collapse(View view) {
        view.setPivotY(0f);
        view.setPivotX(view.getWidth() / 2f);
        view.animate()
                .alpha(0f)
                .scaleY(0f)
                .setDuration(300)
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }
}