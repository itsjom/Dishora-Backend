package com.example.dishora.defaultUI.homeTab.cart.shopCart.checkout;

import static android.content.ContentValues.TAG;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.dishora.BuildConfig;
import com.example.dishora.R;
import com.example.dishora.defaultUI.homeTab.cart.CustomerCartActivity;
import com.example.dishora.defaultUI.homeTab.cart.shopCart.checkout.dto.AddressDto;
import com.example.dishora.defaultUI.homeTab.cart.shopCart.checkout.dto.ItemDto;
import com.example.dishora.defaultUI.homeTab.cart.shopCart.checkout.dto.OrderRequestDto;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.cart.CartManager;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.DTO.CheckoutRequestDto;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.DTO.CheckoutResponseDto;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.PayMongoBackendAPI;
import com.example.dishora.utils.PhoneNumberTextWatcher;
import com.example.dishora.utils.SessionManager;
import com.example.dishora.vendorUI.profileTab.paymentMethod.model.PaymentMethod;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopCartCheckoutActivity extends AppCompatActivity {

    private MaterialCardView cardSelectAddress;
    private TextView tvSelectedAddress;
    private TextInputEditText etFullName, etContactNumber, etDeliveryDate;
    private CardView mapPreviewContainer;
    private RadioGroup rgPaymentMethods;
    private String selectedPaymentMethod;
    private LatLng selectedLatLng;
    private TextInputEditText etDeliveryTime;
    private Calendar selectedTime = Calendar.getInstance();
    private Calendar selectedDate = Calendar.getInstance();
    private Button btnPlaceOrder;
    private ProgressBar progressBar;
    private boolean isAwaitingPaymentResult = false;
    private String lastCheckoutId;

    // Variable to hold the detailed address
    private AddressDto detailedAddress;

    private final ActivityResultLauncher<Intent> startAutocomplete = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent intent = result.getData(); if (intent != null) {
                Place place = Autocomplete.getPlaceFromIntent(intent);
                handleSelectedPlace(place);
            }
        } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
            Toast.makeText(this, "Error selecting address: " + Objects.requireNonNull(Autocomplete.getStatusFromIntent(result.getData())).getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_cart_checkout);

        Log.d(TAG, "Checkout screen opened");
        etFullName = findViewById(R.id.etFullName);
        etContactNumber = findViewById(R.id.etContactNumber);
        etContactNumber.addTextChangedListener(new PhoneNumberTextWatcher());

        SessionManager session = new SessionManager(this);
        etFullName.setText(session.getFullName());
        Log.d(TAG, "Session loaded for userId=" + session.getUserId());

        etDeliveryDate = findViewById(R.id.etDeliveryDate);
        updateDisplayedDate();

        etDeliveryTime = findViewById(R.id.etDeliveryTime);
        updateDisplayedTime();

        etDeliveryTime.setOnClickListener(v -> showTimePicker());
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        }
        cardSelectAddress = findViewById(R.id.cardSelectAddress);
        tvSelectedAddress = findViewById(R.id.tvSelectedAddress);
        mapPreviewContainer = findViewById(R.id.mapPreviewContainer);
        rgPaymentMethods = findViewById(R.id.rgPaymentMethods);

        // --- UPDATED CLICK LISTENER ---
        cardSelectAddress.setOnClickListener(v -> {
            AddressPickerDialogFragment dialog = new AddressPickerDialogFragment();
            dialog.setListener(new AddressPickerDialogFragment.AddressListener() {
                @Override
                public void onAddressConfirmed(String fullAddress, String street, String barangay,
                                               String city, String province, String postal, String region) {

                    // 1. Update the UI (user only sees the full address)
                    tvSelectedAddress.setText(fullAddress);
                    generateCoordinatesAndShowMap(fullAddress);

                    // 2. Create and store the detailed address object
                    detailedAddress = new AddressDto();
                    detailedAddress.fullAddress = fullAddress;
                    detailedAddress.streetName = street;    // Map 'street' to 'streetName'
                    detailedAddress.barangay = barangay;
                    detailedAddress.city = city;
                    detailedAddress.province = province;
                    detailedAddress.postalCode = postal;  // Map 'postal' to 'postalCode'
                    detailedAddress.region = region;
                }
            });
            dialog.show(getSupportFragmentManager(), "address_picker");
        });
        // --- END OF UPDATE ---

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        int cartIndex = getIntent().getIntExtra("cartIndex", -1);
        if (cartIndex >= 0 && cartIndex < CartManager.getInstance().getCartList().size()) {
            long vendorId = CartManager.getInstance().getCartList().get(cartIndex).getBusinessId();
            loadAvailablePaymentMethods(vendorId);
        } else {
            Toast.makeText(this, "Invalid cart index", Toast.LENGTH_SHORT).show();
        }
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        progressBar = findViewById(R.id.progressBar);
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAwaitingPaymentResult) {
            isAwaitingPaymentResult = false;
            progressBar.setVisibility(View.VISIBLE);
            btnPlaceOrder.setEnabled(false);
            Toast.makeText(this, "Verifying payment status...", Toast.LENGTH_SHORT).show();
            verifyPaymentAndFinalizeOrder();
        }
    }

    private void verifyPaymentAndFinalizeOrder() {
        if (lastCheckoutId == null || lastCheckoutId.isEmpty()) { progressBar.setVisibility(View.GONE); btnPlaceOrder.setEnabled(true); return; }
        PayMongoBackendAPI api = ApiClient.getBackendClient(this).create(PayMongoBackendAPI.class);
        api.getOrderStatusByCheckoutId(lastCheckoutId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE); btnPlaceOrder.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ShopCartCheckoutActivity.this, "Payment successful! Order placed.", Toast.LENGTH_LONG).show();
                    int cartIndex = getIntent().getIntExtra("cartIndex", -1);
                    if (cartIndex != -1) { CartManager.getInstance().removeShopFromCart(cartIndex); }
                    Intent intent = new Intent(ShopCartCheckoutActivity.this, CustomerCartActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else { Toast.makeText(ShopCartCheckoutActivity.this, "Payment was not completed. Please try again.", Toast.LENGTH_LONG).show(); }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(ShopCartCheckoutActivity.this, "Could not verify payment: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void generateCoordinatesAndShowMap(String fullAddress) {
        mapPreviewContainer.setVisibility(View.VISIBLE);
        Geocoder geocoder = new Geocoder(this);

        new Thread(() -> {
            try {
                List<Address> results = geocoder.getFromLocationName(fullAddress, 1);
                if (results != null && !results.isEmpty()) {
                    Address a = results.get(0);
                    LatLng coords = new LatLng(a.getLatitude(), a.getLongitude()); selectedLatLng = coords;
                    runOnUiThread(() -> updateMapPreview(coords));
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Could not find location.", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) { Log.e(TAG, "Geocoder error", e);
            }
        })
                .start();
    }

    private void handleSelectedPlace(Place place) {
        String address = place.getAddress();
        selectedLatLng = place.getLatLng();
        if (address != null)
            tvSelectedAddress.setText(address);
        if (selectedLatLng != null)
            updateMapPreview(selectedLatLng);
    }

    private void updateMapPreview(LatLng location) {
        mapPreviewContainer.setVisibility(View.VISIBLE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapPreview);
        if (mapFragment == null)
            return;
        mapFragment.getMapAsync(googleMap -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(location));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f));
            googleMap.getUiSettings().setAllGesturesEnabled(false);
        });
    }

    private boolean isValidPhilippinesNumber(String phone) {
        String digits = phone.replaceAll("\\D", "");
        return digits.matches("^09\\d{9}$");
    }

    private void displayPaymentMethods(List<PaymentMethod> methods) {
        rgPaymentMethods.removeAllViews();
        boolean hasEnabled = false;
        for (PaymentMethod method : methods) {
            if (!method.isEnabled()) continue;
            hasEnabled = true;
            String name = method.getName().trim().toLowerCase(Locale.ROOT);
            String key; int logoRes;
            if (name.contains("card")) {
                key = "card"; logoRes = R.drawable.ic_card_icon;
            } else if (name.contains("gcash")) {
                key = "gcash"; logoRes = R.drawable.gcash_logo;
            } else if (name.contains("maya")) {
                key = "paymaya"; logoRes = R.drawable.maya_logo;
            } else if (name.contains("cash") || name.contains("cod")) {
                key = "cod"; logoRes = R.drawable.ic_cod;
            } else continue;
            RadioButton rb = new RadioButton(this);
            rb.setText(method.getName());
            rb.setTextSize(14f); rb.setTag(key);
            rb.setCompoundDrawablesWithIntrinsicBounds(logoRes, 0, 0, 0);
            rb.setCompoundDrawablePadding(16); rgPaymentMethods.addView(rb);
        } if (!hasEnabled) {
            TextView tv = new TextView(this);
            tv.setText("This vendor has not set up any payment methods yet.");
            tv.setTextColor(getColor(R.color.gray)); tv.setPadding(8, 8, 8, 8);
            rgPaymentMethods.addView(tv);
        } else {
            rgPaymentMethods.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton rb = findViewById(checkedId);
                if (rb != null) selectedPaymentMethod = (String) rb.getTag();
            });
            if (rgPaymentMethods.getChildCount() > 0) ((RadioButton) rgPaymentMethods.getChildAt(0)).setChecked(true);
        }
    }

    private void loadAvailablePaymentMethods(long businessId) {
        PayMongoBackendAPI api = ApiClient.getBackendClient(this).create(PayMongoBackendAPI.class);
        api.getBusinessPaymentMethods(businessId).enqueue(new Callback<List<PaymentMethod>>() {
            @Override public void onResponse(Call<List<PaymentMethod>> call, Response<List<PaymentMethod>> response) {
                if (response.isSuccessful() && response.body() != null) displayPaymentMethods(response.body());
                else Toast.makeText(ShopCartCheckoutActivity.this, "Failed to load payment methods", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<List<PaymentMethod>> call, Throwable t) {
                Toast.makeText(ShopCartCheckoutActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void updateDisplayedDate() {
        Calendar today = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
        etDeliveryDate.setText(format.format(today.getTime()));
        selectedDate.setTime(today.getTime());
    }

    private void showTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedTime.set(Calendar.MINUTE, minute);
            updateDisplayedTime();
        },
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),false);
        dialog.show();
    }


    private void updateDisplayedTime() {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String timeString = format.format(selectedTime.getTime());
        etDeliveryTime.setText(timeString);
    }

    private void placeOrder() {
        Log.d(TAG, "Attempting to place order...");
        String contact = etContactNumber.getText().toString().trim();
        if (!isValidPhilippinesNumber(contact)) {
            etContactNumber.setError("Enter a valid 11-digit PH number (e.g. 0912-345-6789)");
            etContactNumber.requestFocus();
            return;
        }

        // --- UPDATED VALIDATION ---
        if (detailedAddress == null) {
            Toast.makeText(this, "Please select a delivery address.", Toast.LENGTH_SHORT).show();
            return;
        }
        // --- END OF UPDATE ---

        if (selectedPaymentMethod == null) {
            Toast.makeText(this, "Select a payment method first.", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar now = Calendar.getInstance();
        Calendar deliveryDateTime = (Calendar) selectedDate.clone();
        deliveryDateTime.set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY));
        deliveryDateTime.set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE));
        now.add(Calendar.MINUTE, 30);

        if (deliveryDateTime.before(now)) {
            Toast.makeText(this, "Please select a delivery time at least 30 minutes from now.", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);

        int cartIndex = getIntent().getIntExtra("cartIndex", -1);
        if (cartIndex < 0 || cartIndex >= CartManager.getInstance().getCartList().size()) {
            Toast.makeText(this, "Cart not found.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnPlaceOrder.setEnabled(true);
            return;
        }

        var cart = CartManager.getInstance().getCartList().get(cartIndex);
        SessionManager session = new SessionManager(this);

        OrderRequestDto dto = new OrderRequestDto();
        dto.userId = session.getUserId();
        dto.businessId = cart.getBusinessId();
        dto.paymentMethodId = resolvePaymentMethodId(selectedPaymentMethod);
        dto.total = cart.calculateTotal();
        dto.deliveryDate = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());
        dto.deliveryTime = new java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(selectedTime.getTime());

        // --- UPDATED ADDRESS DTO CREATION ---
        detailedAddress.phoneNumber = contact; // Add the contact number to the object
        dto.address = detailedAddress;         // Assign the complete DTO
        // --- END OF UPDATE ---

        List<ItemDto> items = new ArrayList<>();
        cart.getItems().forEach(ci -> {
            ItemDto i = new ItemDto();
            i.productId = ci.getProduct().getProductId();
            i.productName = ci.getProduct().getItemName();
            i.productDescription = ci.getProduct().getDescription();
            i.quantity = ci.getQuantity();
            i.priceAtOrderTime = ci.getProduct().getPrice();
            i.isPreOrder = false;
            items.add(i);
        });
        dto.items = items;

        PayMongoBackendAPI api = ApiClient.getBackendClient(this).create(PayMongoBackendAPI.class);

        if ("gcash".equals(selectedPaymentMethod) || "paymaya".equals(selectedPaymentMethod) || "card".equals(selectedPaymentMethod)) {

            String orderDetailsJson = new Gson().toJson(dto);
            Log.d(TAG, "Serialized OrderDetailsMetadata: " + orderDetailsJson);
            int amountCentavos = (int) Math.round(cart.calculateTotal() * 100);
            String itemName;
            if (cart.getItems().size() == 1) {
                itemName = cart.getItems().get(0).getProduct().getItemName();
            } else {
                itemName = cart.getItems().get(0).getProduct().getItemName() + " + " + (cart.getItems().size() - 1) + " more item" + (cart.getItems().size() > 2 ? "s" : "");
            }
            int quantity = cart.getItems().size();
            int vendorId = (int) cart.getBusinessId();
            CheckoutRequestDto checkoutReq = new CheckoutRequestDto(amountCentavos, itemName, quantity, vendorId, selectedPaymentMethod, orderDetailsJson);

            Log.d(TAG, "Starting PayMongo checkout with metadata: " + new Gson().toJson(checkoutReq));
            api.createCheckout(checkoutReq).enqueue(new Callback<CheckoutResponseDto>() {
                @Override
                public void onResponse(Call<CheckoutResponseDto> call, Response<CheckoutResponseDto> response) {
                    progressBar.setVisibility(View.GONE); btnPlaceOrder.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) {
                        isAwaitingPaymentResult = true;
                        lastCheckoutId = response.body().getCheckoutId();
                        String checkoutUrl = response.body().getCheckoutUrl();
                        Log.d(TAG, "Redirecting to: " + checkoutUrl);
                        Toast.makeText(ShopCartCheckoutActivity.this, "Redirecting to payment...", Toast.LENGTH_SHORT).show();
                        openCheckoutUrl(checkoutUrl);
                    } else {
                        String detailedError = "Unknown error";
                        if (response.errorBody() != null) {
                            try { detailedError = response.errorBody().string(); }
                            catch (IOException e) { Log.e(TAG, "Error parsing error body", e); }
                        }
                        Log.e(TAG, "Payment initialization failed. Code: " + response.code() + ". Details: " + detailedError);
                        Toast.makeText(ShopCartCheckoutActivity.this, "Payment initialization failed (" + response.code() + "). Please check logs for details.", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<CheckoutResponseDto> call, Throwable t) {
                    progressBar.setVisibility(View.GONE); btnPlaceOrder.setEnabled(true);
                    Toast.makeText(ShopCartCheckoutActivity.this, "Payment error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        // This is for COD
        Log.d(TAG, "Submitting COD order payload: " + new Gson().toJson(dto));
        api.createOrder(dto).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ShopCartCheckoutActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                    CartManager.getInstance().removeShopFromCart(cartIndex);
                    Intent intent = new Intent(ShopCartCheckoutActivity.this, CustomerCartActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    progressBar.setVisibility(View.GONE); btnPlaceOrder.setEnabled(true);
                    Toast.makeText(ShopCartCheckoutActivity.this, "Failed: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE); btnPlaceOrder.setEnabled(true);
                Toast.makeText(ShopCartCheckoutActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private long resolvePaymentMethodId(String key) {
        if (key == null) return 0;
        switch (key) {
            case "card": return 1;
            case "gcash": return 2;
            case "paymaya": return 3;
            case "cod": return 4;
            default: return 0;
        }
    }

    private void openCheckoutUrl(String checkoutUrl) {
        if (checkoutUrl == null || checkoutUrl.isEmpty()) {
            Toast.makeText(this, "Could not get payment URL.", Toast.LENGTH_SHORT).show();
            return;
        }
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().setShowTitle(true).setToolbarColor(ContextCompat.getColor(this, R.color.orange)).build();
        customTabsIntent.launchUrl(this, Uri.parse(checkoutUrl));
    }
}