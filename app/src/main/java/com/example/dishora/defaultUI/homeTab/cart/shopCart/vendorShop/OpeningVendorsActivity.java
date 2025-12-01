package com.example.dishora.defaultUI.homeTab.cart.shopCart.vendorShop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dishora.R;
import com.example.dishora.defaultUI.homeTab.cart.shopCart.ShopCartDetailActivity;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.VendorDetail;

public class OpeningVendorsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening_vendors);

        // Toolbar
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        ImageView btnSearch = findViewById(R.id.btnSearch);
        toolbarTitle.setText("Add more items");

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, ShopCartDetailActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // read vendor info from the intent
        String name = getIntent().getStringExtra("name");
        String address = getIntent().getStringExtra("address");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        long businessId = getIntent().getLongExtra("businessId", -1);

        // pass it into the fragment
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("address", address);
        bundle.putString("imageUrl", imageUrl);
        bundle.putLong("businessId", businessId);

        VendorDetail fragment = new VendorDetail();
        fragment.setArguments(bundle);

        // load the fragment into this activity’s container
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.vendor_fragment_container, fragment)
                .commit();
    }
}