package com.example.dishora.defaultUI.homeTab.cart;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.defaultUI.homeTab.cart.adapter.CartAdapter;
import com.example.dishora.defaultUI.homeTab.cart.model.CartModel;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.cart.CartManager;

import java.util.ArrayList;
import java.util.List;

public class CustomerCartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartModel> cartList;
    private TextView emptyCartMessage; // Optional: Add a TextView in your XML with this ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_cart);

        // Toolbar
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        ImageView btnSearch = findViewById(R.id.btnSearch);
        toolbarTitle.setText("My Cart");
        btnSearch.setVisibility(View.GONE);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerCarts);
        emptyCartMessage = findViewById(R.id.emptyCartMessage); // Optional
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize with an empty list. The data will be loaded in onResume.
        cartList = new ArrayList<>();
        cartAdapter = new CartAdapter(cartList, this);
        recyclerView.setAdapter(cartAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "CartActivity: onResume triggered. Refreshing UI.");

        // ✅ --- THE CORRECTED REFRESH LOGIC ---

        // 1. Get the latest cart list for the CURRENTLY active user from the manager.
        List<CartModel> currentUsersCart = CartManager.getInstance().getCartList();

        Log.d(TAG, "CartActivity: Received list from manager with " + currentUsersCart.size() + " items.");

        // 2. Clear the local list that the adapter is using.
        cartList.clear();

        // 3. Add all items from the correct user's cart into our local list.
        cartList.addAll(currentUsersCart);

        // 4. Notify the adapter that the underlying data has changed completely.
        cartAdapter.notifyDataSetChanged();

        // 5. (Optional but recommended) Handle the empty state UI.
        if (cartList.isEmpty()) {
            if (emptyCartMessage != null) emptyCartMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            if (emptyCartMessage != null) emptyCartMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}