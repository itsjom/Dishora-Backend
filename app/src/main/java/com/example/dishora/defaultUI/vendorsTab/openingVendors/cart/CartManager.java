// CartManager.java (Final, Corrected Version with Save/Remove Methods)
package com.example.dishora.defaultUI.vendorsTab.openingVendors.cart;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.dishora.defaultUI.homeTab.cart.item.CartItem;
import com.example.dishora.defaultUI.homeTab.cart.model.CartModel;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.model.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {

    private static CartManager instance;
    private static final String PREF_NAME = "DishoraCartPrefs";
    private static final String GUEST_USER_ID = "guest_user";
    private static final String TAG = "SESSION_DEBUG";

    private SharedPreferences prefs;
    private final Gson gson = new Gson();

    private final Map<String, List<CartModel>> userCarts = new HashMap<>();
    private String currentUserId;

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void init(Context context) {
        if (prefs == null) {
            prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public void setCurrentUser(String userId) {
        currentUserId = (userId == null || userId.trim().isEmpty()) ? GUEST_USER_ID : userId.trim();
        Log.d(TAG, "CartManager: setCurrentUser called. Active user is now: " + currentUserId);
        if (!userCarts.containsKey(currentUserId)) {
            Log.d("CartManager", "Cart for " + currentUserId + " not in cache. Loading from prefs.");
            List<CartModel> restoredCart = loadCartFromPrefs(currentUserId);
            userCarts.put(currentUserId, restoredCart);
        } else {
            Log.d("CartManager", "Cart for " + currentUserId + " was already in cache. No need to load.");
        }
    }

    public List<CartModel> getCartList() {
        if (currentUserId == null) {
            setCurrentUser(null);
        }
        List<CartModel> cart = userCarts.get(currentUserId);
        int itemCount = (cart == null) ? 0 : cart.size();
        Log.d(TAG, "CartManager: getCartList called for user: " + currentUserId + ". Returning list with " + itemCount + " items.");
        return userCarts.get(currentUserId);
    }

    public void clearCart() {
        if (currentUserId != null) {
            List<CartModel> currentUserCart = getCartList();
            if (currentUserCart != null) {
                currentUserCart.clear();
                saveCartToPrefs(currentUserId);
                Log.d("CartManager", "Cart cleared for user: " + currentUserId);
            }
        }
    }

    public void addToCart(String shopName, String shopAddress, String shopLogoUrl, long businessId, Product product) {
        if (currentUserId == null) {
            Log.e("CartManager", "Cannot add to cart. No user is set.");
            return;
        }
        List<CartModel> currentUserCart = getCartList();
        for (CartModel cart : currentUserCart) {
            if (cart.getBusinessId() == businessId) {
                for (CartItem item : cart.getItems()) {
                    if (item.getProduct().getProductId() == product.getProductId()) {
                        item.setQuantity(item.getQuantity() + 1);
                        saveCartToPrefs(currentUserId);
                        return;
                    }
                }
                cart.getItems().add(new CartItem(product));
                saveCartToPrefs(currentUserId);
                return;
            }
        }
        List<CartItem> newItems = new ArrayList<>();
        newItems.add(new CartItem(product));
        currentUserCart.add(new CartModel(shopName, shopAddress, shopLogoUrl, businessId, newItems));
        saveCartToPrefs(currentUserId);
    }

    // ✅ --- NEW METHODS TO FIX PERSISTENCE ---

    /**
     * Removes a shop's entire cart by its index and immediately saves the changes.
     */
    public void removeShopFromCart(int index) {
        if (currentUserId == null) return;
        List<CartModel> currentUserCart = getCartList();
        if (index >= 0 && index < currentUserCart.size()) {
            currentUserCart.remove(index);
            saveCartToPrefs(currentUserId);
            Log.d("CartManager", "Removed shop at index " + index + " and saved cart for user " + currentUserId);
        }
    }

    /**
     * Manually saves the current state of the cart to SharedPreferences.
     * Essential for persisting changes like quantity updates.
     */
    public void saveCurrentCart() {
        if (currentUserId != null) {
            saveCartToPrefs(currentUserId);
            Log.d("CartManager", "Force-saved cart for user " + currentUserId);
        }
    }

    // --- PRIVATE PERSISTENCE METHODS ---

    private void saveCartToPrefs(String userId) {
        if (prefs == null || userId == null) return;
        List<CartModel> cartList = userCarts.get(userId);
        String json = gson.toJson(cartList);
        prefs.edit().putString("cart_user_" + userId, json).apply();
        Log.d("CartManager", "Cart saved for user " + userId);
    }

    private List<CartModel> loadCartFromPrefs(String userId) {
        if (prefs == null || userId == null) return new ArrayList<>();
        String json = prefs.getString("cart_user_" + userId, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<CartModel>>() {}.getType();
        try {
            List<CartModel> loadedCart = gson.fromJson(json, type);
            return loadedCart != null ? loadedCart : new ArrayList<>();
        } catch (Exception e) {
            Log.e("CartManager", "Error parsing cart JSON for user " + userId, e);
            return new ArrayList<>();
        }
    }
}