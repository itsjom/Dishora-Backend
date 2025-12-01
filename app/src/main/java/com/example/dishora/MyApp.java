package com.example.dishora;

import android.app.Application;

import com.example.dishora.defaultUI.vendorsTab.openingVendors.cart.CartManager;
import com.example.dishora.utils.GeoapifyHelper;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Geoapify once for whole app
        GeoapifyHelper.init("f46420d3ff7a4ca5bdfcd1b3c75a2951");
        android.util.Log.d("MyApp", "Geoapify initialized with API key");

        CartManager.getInstance().init(this);
    }
}