package com.example.dishora.defaultUI.homeTab.cart.shopCart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.defaultUI.homeTab.cart.item.CartItem;
import com.example.dishora.defaultUI.homeTab.cart.model.CartModel;
import com.example.dishora.defaultUI.homeTab.cart.shopCart.adapter.ShopItemsAdapter;
import com.example.dishora.defaultUI.homeTab.cart.shopCart.checkout.ShopCartCheckoutActivity;
import com.example.dishora.defaultUI.homeTab.cart.shopCart.vendorShop.OpeningVendorsActivity;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.cart.CartManager;

import java.util.List;

public class ShopCartDetailActivity extends AppCompatActivity implements ShopItemsAdapter.OnCartUpdateListener {

    private TextView tvShopName, tvSubtotal, tvServiceFee, tvVAT, tvTotal;
    private RecyclerView recyclerShopCart;
    private CartModel currentCart;
    private LinearLayout layoutAddMoreItems;
    private int cartIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_cart_detail);

        // Toolbar
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        ImageView btnSearch = findViewById(R.id.btnSearch);
        toolbarTitle.setText("My Cart");
        btnSearch.setVisibility(View.GONE);
        btnBack.setOnClickListener(v -> finish());

        // Views
        tvShopName = findViewById(R.id.tvShopName);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvServiceFee = findViewById(R.id.tvServiceFee);
        // tvVAT = findViewById(R.id.tvVAT);
        tvTotal = findViewById(R.id.tvTotal);
        recyclerShopCart = findViewById(R.id.recyclerShopCart);
        layoutAddMoreItems = findViewById(R.id.layoutAddMoreItems);

        cartIndex = getIntent().getIntExtra("cartIndex", -1);
        List<CartModel> allCarts = CartManager.getInstance().getCartList();

        if (cartIndex == -1 || cartIndex >= allCarts.size()) {
            Toast.makeText(this, "Error: Could not find cart.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentCart = allCarts.get(cartIndex);

        tvShopName.setText(currentCart.getShopName());
        recyclerShopCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerShopCart.setAdapter(new ShopItemsAdapter(currentCart.getItems(), this, this));

        layoutAddMoreItems.setOnClickListener(v -> {
            Intent intent = new Intent(this, OpeningVendorsActivity.class);
            intent.putExtra("name", currentCart.getShopName());
            intent.putExtra("address", currentCart.getShopAddress());
            intent.putExtra("imageUrl", currentCart.getShopLogoUrl());
            intent.putExtra("businessId", currentCart.getBusinessId());
            intent.putExtra("hideToolbar", true);
            startActivity(intent);
            finish();
        });

        updateSummary();

        Button btnReview = findViewById(R.id.btnReviewPayment);
        btnReview.setOnClickListener(v -> {
            // ✅ 2. LAUNCH THE CHECKOUT ACTIVITY

            // First, make sure the cart is not empty before proceeding
            if (currentCart.getItems().isEmpty()) {
                Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create an intent to start the checkout process
            Intent intent = new Intent(ShopCartDetailActivity.this, ShopCartCheckoutActivity.class);

            // ✅ 3. (Optional but Recommended) PASS DATA TO THE CHECKOUT SCREEN
            // This is useful so the checkout screen knows which vendor's cart to process
            // and can display the total amount.
            intent.putExtra("cartIndex", cartIndex);

            // Launch the activity
            startActivity(intent);
        });
    }

    // ✅ --- IMPLEMENTATION OF THE LISTENER INTERFACE ---

    @Override
    public void onCartUpdated() {
        // This is called when quantity changes or a non-final item is removed.
        updateSummary();
        // The changes will be saved automatically when the user leaves the screen (in onPause).
    }

    @Override
    public void onCartEmpty() {
        // This is called by the adapter ONLY when the last item has been removed.

        // ✅ 1. Use the new manager method that handles removing AND saving.
        CartManager.getInstance().removeShopFromCart(cartIndex);

        Toast.makeText(this, "Shop removed from cart", Toast.LENGTH_SHORT).show();
        finish();
    }

    // ✅ --- ADD onPause() TO SAVE CHANGES ON EXIT ---

    @Override
    protected void onPause() {
        super.onPause();
        // This is called whenever the user leaves this activity.
        // It's the perfect place to save any changes to item quantities.
        CartManager.getInstance().saveCurrentCart();
    }

    private void updateSummary() {
        double subtotal = 0;
        for (CartItem item : currentCart.getItems()) {
            subtotal += item.getProduct().getPrice() * item.getQuantity();
        }
        double service = subtotal * 0.03;
        // double vat = subtotal * 0.12;
        double total = subtotal + service;

        tvSubtotal.setText(String.format("₱%.2f", subtotal));
        tvServiceFee.setText(String.format("₱%.2f", service));
        // tvVAT.setText(String.format("₱%.2f", vat));
        tvTotal.setText(String.format("₱%.2f", total));
    }
}