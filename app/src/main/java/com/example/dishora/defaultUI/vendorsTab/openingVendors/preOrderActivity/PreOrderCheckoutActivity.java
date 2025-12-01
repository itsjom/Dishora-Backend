package com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.dishora.BuildConfig;
import com.example.dishora.CheckoutConfirmationActivity;
import com.example.dishora.R;
import com.example.dishora.defaultUI.homeTab.cart.shopCart.checkout.AddressPickerDialogFragment;
import com.example.dishora.defaultUI.homeTab.cart.shopCart.checkout.dto.AddressDto;
import com.example.dishora.defaultUI.homeTab.cart.shopCart.checkout.dto.ItemDto;
import com.example.dishora.defaultUI.homeTab.cart.shopCart.checkout.dto.OrderRequestDto;
// Make sure this import points to your actual Product model location
import com.example.dishora.defaultUI.vendorsTab.openingVendors.model.Product; // <-- CHECK THIS IMPORT
import com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.DTO.CheckoutRequestDto;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.DTO.CheckoutResponseDto;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.PayMongoBackendAPI;
import com.example.dishora.utils.PhoneNumberTextWatcher;
import com.example.dishora.utils.SessionManager;
import com.example.dishora.vendorUI.homeTab.schedule.model.ScheduleItem;
import com.example.dishora.vendorUI.profileTab.paymentMethod.model.PaymentMethod;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.IOException;
// Removed reflection import
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreOrderCheckoutActivity extends AppCompatActivity {

    // --- Product/UI Variables ---
    private Product product;
    private String note = "";
    private TextView txtFoodName, txtPrice, txtDate, txtQuantity, txtNote, txtDeliveryTime;
    private ImageView imgFood;
    private Button btnMinus, btnPlus, btnCancel, btnPreOrder;
    private String selectedPaymentMethod = null;
    private RadioGroup radioPaymentMethods;
    private int quantity = 1;
    private boolean isPreorderMode = false;

    // --- Payment Option Variables ---
    private RadioGroup radioPaymentOption;
    private RadioButton radioPayAdvance, radioPayFull;
    private boolean isPayingAdvance = true;
    private double advanceAmountPerItem = 0.0; // Store the vendor's fixed advance amount

    // --- Date Picker Variables ---
    private HashSet<Long> availableUtcTimestamps = new HashSet<>();
    private long firstAvailableDateMs = -1;
    private long lastAvailableDateMs = -1;
    private Calendar selectedDateCalendar = Calendar.getInstance();
    private boolean schedulesLoaded = false;

    // --- Time Picker Variable ---
    private Calendar selectedTime = Calendar.getInstance();

    // --- Address/Input Variables ---
    private TextInputEditText etFullName, etContactNumber;
    private MaterialCardView cardSelectAddress;
    private TextView tvSelectedAddress;
    private CardView mapPreviewContainer;
    private LatLng selectedLatLng;
    private ProgressBar progressBar;
    private AddressDto detailedAddress;

    private long pendingDraftId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_order_checkout);
        product = (Product) getIntent().getSerializableExtra("product");
        note = getIntent().getStringExtra("note");
        String mode = getIntent().getStringExtra("mode");
        isPreorderMode = "preorder".equalsIgnoreCase(mode);

        if (product == null) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Get the fixed advance amount directly from the updated model ---
        advanceAmountPerItem = product.getAdvanceAmount();
        if (advanceAmountPerItem < 0) { // Add a check for negative values
            Log.w("PreOrderCheckout", "Received negative advance amount. Defaulting to 0.");
            advanceAmountPerItem = 0.0;
        }
        // --- End of getting advance amount ---

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        }

        bindUI();
        setupUI();
        setupButtons();
        setupAddressPicker();
        loadAvailablePaymentMethods(product.getVendorId()); // Use correct getter
        if (isPreorderMode) {
            loadAvailableSchedules(product.getVendorId()); // Use correct getter
        } else {
            schedulesLoaded = true; // Not a pre-order, allow date/time selection
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if we are returning from a checkout attempt
        if (this.pendingDraftId != -1) {
            Log.d("PreOrderCheckout", "Resuming activity with pending draftId: " + this.pendingDraftId);

            // Store the ID and clear the flag *before* starting the new activity
            long draftIdToConfirm = this.pendingDraftId;
            this.pendingDraftId = -1;

            // Launch the confirmation screen
            Intent confirmationIntent = new Intent(PreOrderCheckoutActivity.this, CheckoutConfirmationActivity.class);
            confirmationIntent.putExtra("draftId", draftIdToConfirm);
            confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(confirmationIntent);

            // Now finish this activity
            finish();

        } else {
            // This is a normal onResume (e.g., user switched apps)
            // Re-enable the button if it was disabled by the checkout click
            if (btnPreOrder != null && !btnPreOrder.isEnabled()) {
                Log.d("PreOrderCheckout", "Resuming activity, re-enabling button.");
                progressBar.setVisibility(View.GONE);
                btnPreOrder.setEnabled(true);
            }
        }
    }

    private void bindUI() {
        // ... (Binding remains the same) ...
        txtFoodName = findViewById(R.id.txtFoodName);
        txtPrice = findViewById(R.id.txtPrice);
        txtDate = findViewById(R.id.txtDate);
        txtQuantity = findViewById(R.id.txtQuantity);
        txtNote = findViewById(R.id.txtDescription);
        imgFood = findViewById(R.id.imgFood);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        btnCancel = findViewById(R.id.btnCancel);
        btnPreOrder = findViewById(R.id.btnPreOrder);
        radioPaymentMethods = findViewById(R.id.radioPaymentMethods);
        etFullName = findViewById(R.id.etFullName);
        etContactNumber = findViewById(R.id.etContactNumber);
        cardSelectAddress = findViewById(R.id.cardSelectAddress);
        tvSelectedAddress = findViewById(R.id.tvSelectedAddress);
        mapPreviewContainer = findViewById(R.id.mapPreviewContainer);
        progressBar = findViewById(R.id.progressBar);
        txtDeliveryTime = findViewById(R.id.txtDeliveryTime);
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        radioPaymentOption = findViewById(R.id.radioPaymentOption);
        radioPayAdvance = findViewById(R.id.radioPayAdvance);
        radioPayFull = findViewById(R.id.radioPayFull);
    }

    private void displayPaymentMethods(List<PaymentMethod> methods) {
        // ... (This method remains the same) ...
        radioPaymentMethods.removeAllViews();
        boolean hasEnabledMethods = false;
        for (PaymentMethod method : methods) {
            if (!method.isEnabled()) continue;
            String methodName = method.getName().trim().toLowerCase(Locale.ROOT);
            if (isPreorderMode && (methodName.contains("cash") || methodName.contains("cod")) && !methodName.contains("gcash")) {
                continue;
            }

            String paymongoKey;
            int logoRes;
            if (methodName.contains("card")) {
                paymongoKey = "card";
                logoRes = R.drawable.ic_card_icon;
            } else if (methodName.contains("gcash")) {
                paymongoKey = "gcash";
                logoRes = R.drawable.gcash_logo;
            } else if (methodName.contains("maya")) {
                paymongoKey = "paymaya";
                logoRes = R.drawable.maya_logo;
            } else {
                continue;
            }

            RadioButton rb = new RadioButton(this);
            rb.setText(method.getName());
            rb.setTextSize(14f);
            rb.setTag(paymongoKey);
            rb.setId(View.generateViewId());
            rb.setCompoundDrawablesWithIntrinsicBounds(logoRes, 0, 0, 0);
            rb.setCompoundDrawablePadding(16);
            radioPaymentMethods.addView(rb);
            hasEnabledMethods = true;
        }

        if (hasEnabledMethods && radioPaymentMethods.getChildCount() > 0) {
            RadioButton firstRb = (RadioButton) radioPaymentMethods.getChildAt(0);
            firstRb.setChecked(true);
            selectedPaymentMethod = (String) firstRb.getTag();
        } else if (!hasEnabledMethods) {
            TextView noMethodsTv = new TextView(this);
            noMethodsTv.setText(isPreorderMode ? "No online payment methods available." : "No payment methods available.");
            noMethodsTv.setPadding(8, 16, 8, 8);
            radioPaymentMethods.addView(noMethodsTv);
        }

        radioPaymentMethods.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = findViewById(checkedId);
            if (rb != null) {
                selectedPaymentMethod = (String) rb.getTag();
            }
        });
    }

    private void loadAvailablePaymentMethods(long businessId) {
        // ... (This method remains the same) ...
        PayMongoBackendAPI api = ApiClient.getBackendClient(this).create(PayMongoBackendAPI.class);
        api.getBusinessPaymentMethods(businessId).enqueue(new Callback<List<PaymentMethod>>() {
            @Override
            public void onResponse(@NonNull Call<List<PaymentMethod>> call, @NonNull Response<List<PaymentMethod>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayPaymentMethods(response.body());
                } else {
                    Log.e("PreOrderCheckout", "Failed to load payment methods: " + response.code());
                    Toast.makeText(PreOrderCheckoutActivity.this, "Failed to load payment methods", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<PaymentMethod>> call, @NonNull Throwable t) {
                Log.e("PreOrderCheckout", "Error loading payment methods", t);
                Toast.makeText(PreOrderCheckoutActivity.this, "Network error loading payment methods", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openCheckoutUrl(String checkoutUrl) {
        // ... (This method remains the same) ...
        if (checkoutUrl == null || checkoutUrl.isEmpty()) {
            Toast.makeText(this, "Invalid checkout URL received", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setToolbarColor(ContextCompat.getColor(this, R.color.orange))
                    .build();
            customTabsIntent.launchUrl(this, Uri.parse(checkoutUrl));

//            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl));
//            startActivity(browserIntent);
        } catch (Exception e) {
            Log.e("PreOrderCheckout", "Could not launch Custom Tab", e);
            Toast.makeText(this, "Could not open payment page. Please ensure you have a browser installed.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupUI() {
        txtFoodName.setText(product.getItemName());
        txtPrice.setText(String.format(Locale.getDefault(), "₱%.2f", product.getPrice()));
        txtNote.setText(note == null || note.isEmpty() ? "No note added" : note);
        txtQuantity.setText(String.valueOf(quantity));
        Glide.with(this).load(product.getImageUrl()).placeholder(R.drawable.ic_product_placeholder).into(imgFood);

        txtDate.setText("Select a date");
        findViewById(R.id.dateContainer).setOnClickListener(v -> showDatePicker());

        SessionManager session = new SessionManager(this);
        etFullName.setText(session.getFullName());
        etFullName.setEnabled(false);
        etContactNumber.addTextChangedListener(new PhoneNumberTextWatcher());

        findViewById(R.id.timeContainer).setOnClickListener(v -> showTimePicker());
        updateDisplayedTime();

        // --- UPDATED Setup Payment Options ---
        // Only show options if it's a pre-order AND the advance amount is valid
        if (isPreorderMode && advanceAmountPerItem > 0 && advanceAmountPerItem < product.getPrice()) {
            radioPaymentOption.setVisibility(View.VISIBLE);
            updatePaymentOptionsUI(); // Set initial text using fixed amount
            radioPayAdvance.setChecked(true);
            isPayingAdvance = true;
        } else {
            // Not a pre-order, or invalid advance amount. Hide options.
            radioPaymentOption.setVisibility(View.GONE);
            isPayingAdvance = false; // Force full payment
        }

        radioPaymentOption.setOnCheckedChangeListener((group, checkedId) -> {
            isPayingAdvance = (checkedId == R.id.radioPayAdvance);
        });
    }

    // --- UPDATED METHOD to use fixed advance amount ---
    private void updatePaymentOptionsUI() {
        if (product == null) return;

        double fullAmountTotal = product.getPrice() * quantity;
        double advanceAmountTotal = advanceAmountPerItem * quantity; // Use the fixed amount per item

        radioPayAdvance.setText(String.format(Locale.getDefault(), "Pay Advance: ₱%.2f", advanceAmountTotal));
        radioPayFull.setText(String.format(Locale.getDefault(), "Pay Full Price: ₱%.2f", fullAmountTotal));
    }


    private void setupButtons() {
        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                txtQuantity.setText(String.valueOf(quantity));
                // Only update if the options are visible
                if (isPreorderMode && advanceAmountPerItem > 0 && advanceAmountPerItem < product.getPrice()) {
                    updatePaymentOptionsUI();
                }
            }
        });
        btnPlus.setOnClickListener(v -> {
            quantity++;
            txtQuantity.setText(String.valueOf(quantity));
            // Only update if the options are visible
            if (isPreorderMode && advanceAmountPerItem > 0 && advanceAmountPerItem < product.getPrice()) {
                updatePaymentOptionsUI();
            }
        });
        btnCancel.setOnClickListener(v -> finish());
        btnPreOrder.setOnClickListener(v -> {
            startCheckout(selectedPaymentMethod);
        });
    }

    private void setupAddressPicker() {
        // ... (This method remains the same) ...
        cardSelectAddress.setOnClickListener(v -> {
            AddressPickerDialogFragment dialog = new AddressPickerDialogFragment();
            dialog.setListener(new AddressPickerDialogFragment.AddressListener() {
                @Override
                public void onAddressConfirmed(String fullAddress, String street, String barangay,
                                               String city, String province, String postal, String region) {
                    tvSelectedAddress.setText(fullAddress);
                    generateCoordinatesAndShowMap(fullAddress);
                    detailedAddress = new AddressDto();
                    detailedAddress.fullAddress = fullAddress;
                    detailedAddress.streetName = street;
                    detailedAddress.barangay = barangay;
                    detailedAddress.city = city;
                    detailedAddress.province = province;
                    detailedAddress.postalCode = postal;
                    detailedAddress.region = region;
                }
            });
            if (!isFinishing() && !isDestroyed()) {
                dialog.show(getSupportFragmentManager(), "address_picker");
            }
        });
    }

    private boolean isValidPhilippinesNumber(String phone) {
        // ... (This method remains the same) ...
        String digits = phone.replaceAll("\\D", "");
        return digits.matches("^09\\d{9}$");
    }

    private void generateCoordinatesAndShowMap(String fullAddress) {
        // ... (This method remains the same) ...
        new Thread(() -> {
            if (getApplicationContext() == null) return;
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> results = geocoder.getFromLocationName(fullAddress + ", Philippines", 1);
                if (!isFinishing() && !isDestroyed()) {
                    if (results != null && !results.isEmpty()) {
                        Address a = results.get(0);
                        selectedLatLng = new LatLng(a.getLatitude(), a.getLongitude());
                        runOnUiThread(() -> updateMapPreview(selectedLatLng));
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Could not find location.", Toast.LENGTH_SHORT).show();
                            mapPreviewContainer.setVisibility(View.GONE);
                            selectedLatLng = null;
                        });
                    }
                }
            } catch (IOException e) {
                Log.e("PreOrderCheckout", "Geocoder error", e);
                if (!isFinishing() && !isDestroyed()) {
                    runOnUiThread(() -> mapPreviewContainer.setVisibility(View.GONE));
                }
            }
        }).start();
    }

    private void updateMapPreview(LatLng location) {
        // ... (This method remains the same) ...
        mapPreviewContainer.setVisibility(View.VISIBLE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapPreview);
        if (mapFragment == null) return;

        mapFragment.getMapAsync(googleMap -> {
            if (googleMap == null) return;
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(location));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f));
            googleMap.getUiSettings().setAllGesturesEnabled(false);
        });
    }

    private long parseDateToUtcTimestamp(String dateString) {
        // ... (This method remains the same) ...
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = sdf.parse(dateString);
            return date != null ? date.getTime() : -1;
        } catch (ParseException e) {
            Log.e("PreOrderCheckout", "Failed to parse date: " + dateString, e);
            return -1;
        }
    }

    private void loadAvailableSchedules(long businessId) {
        // ... (This method remains the same) ...
        progressBar.setVisibility(View.VISIBLE);
        PayMongoBackendAPI api = ApiClient.getBackendClient(this).create(PayMongoBackendAPI.class);
        api.getBusinessSchedules(businessId).enqueue(new Callback<List<ScheduleItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<ScheduleItem>> call, @NonNull Response<List<ScheduleItem>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    availableUtcTimestamps.clear();
                    firstAvailableDateMs = -1;
                    lastAvailableDateMs = -1;
                    long today = MaterialDatePicker.todayInUtcMilliseconds();

                    for (ScheduleItem item : response.body()) {
                        if (item.getAvailableDate() != null && !item.isFull()) {
                            long timestamp = parseDateToUtcTimestamp(item.getAvailableDate());
                            if (timestamp != -1 && timestamp >= today) {
                                availableUtcTimestamps.add(timestamp);
                                if (firstAvailableDateMs == -1 || timestamp < firstAvailableDateMs) {
                                    firstAvailableDateMs = timestamp;
                                }
                                if (lastAvailableDateMs == -1 || timestamp > lastAvailableDateMs) {
                                    lastAvailableDateMs = timestamp;
                                }
                            }
                        }
                    }
                    schedulesLoaded = true;
                    Log.d("PreOrderCheckout", "Schedules loaded. " + availableUtcTimestamps.size() + " dates available.");
                    if(availableUtcTimestamps.isEmpty()){
                        Toast.makeText(PreOrderCheckoutActivity.this, "No available pre-order dates found for this vendor.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    schedulesLoaded = false;
                    Log.e("PreOrderCheckout", "Failed to load schedules: " + response.code());
                    Toast.makeText(PreOrderCheckoutActivity.this, "Failed to load vendor schedules", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ScheduleItem>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                schedulesLoaded = false;
                Log.e("PreOrderCheckout", "Network error loading schedules", t);
                Toast.makeText(PreOrderCheckoutActivity.this, "Network error loading schedules", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        // ... (This method remains the same) ...
        if (!schedulesLoaded) {
            Toast.makeText(this, "Loading available dates... Please try again.", Toast.LENGTH_SHORT).show();
            if (product != null) {
                loadAvailableSchedules(product.getVendorId());
            }
            return;
        }

        if (availableUtcTimestamps.isEmpty() || firstAvailableDateMs == -1) {
            Toast.makeText(this, "This vendor has no available pre-order dates.", Toast.LENGTH_LONG).show();
            return;
        }

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setStart(firstAvailableDateMs);
        constraintsBuilder.setEnd(lastAvailableDateMs);
        constraintsBuilder.setOpenAt(firstAvailableDateMs);
        constraintsBuilder.setValidator(new SelectableDatesValidator(availableUtcTimestamps));

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Pre-Order Date")
                .setCalendarConstraints(constraintsBuilder.build())
                .setSelection(firstAvailableDateMs)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDateCalendar.setTimeInMillis(selection);
            selectedDateCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));

            SimpleDateFormat sdfDisplay = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
            sdfDisplay.setTimeZone(TimeZone.getTimeZone("UTC"));
            txtDate.setText(sdfDisplay.format(selectedDateCalendar.getTime()));
        });

        if (!isFinishing() && !isDestroyed()) {
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        }
    }

    private void showTimePicker() {
        // ... (This method remains the same) ...
        int hour = selectedTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedTime.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minuteOfHour);
                    updateDisplayedTime();
                },
                hour, minute,
                false);
        dialog.show();
    }

    private void updateDisplayedTime() {
        // ... (This method remains the same) ...
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String timeString = format.format(selectedTime.getTime());
        if (txtDeliveryTime != null) {
            CharSequence currentText = txtDeliveryTime.getText();
            // Use hint resource for comparison
            if (currentText == null || currentText.toString().equals(getString(R.string.select_time_hint))) {
                txtDeliveryTime.setText(timeString);
                txtDeliveryTime.setTextColor(ContextCompat.getColor(this, R.color.black));
            } else {
                txtDeliveryTime.setText(timeString);
            }
        }
    }

    // --- UPDATED Checkout Logic ---
    private void startCheckout(String method) {

        // --- Validation ---
        String contact = etContactNumber.getText().toString().trim();
        String selectedDateStr = txtDate.getText().toString();
        String selectedTimeStr = txtDeliveryTime.getText().toString();

        if (selectedDateStr.equals("Select a date")) {
            Toast.makeText(this, "Please select a delivery date", Toast.LENGTH_SHORT).show();
            findViewById(R.id.dateContainer).requestFocus();
            return;
        }
        // Use hint resource for comparison
        if (selectedTimeStr.isEmpty() || selectedTimeStr.equals(getString(R.string.select_time_hint))) {
            Toast.makeText(this, "Please select a delivery time", Toast.LENGTH_SHORT).show();
            findViewById(R.id.timeContainer).requestFocus();
            return;
        }
        if (!isValidPhilippinesNumber(contact)) {
            etContactNumber.setError("Enter a valid 11-digit PH number (e.g. 0912-345-6789)");
            etContactNumber.requestFocus();
            return;
        }
        if (detailedAddress == null) {
            Toast.makeText(this, "Please select a valid delivery address.", Toast.LENGTH_SHORT).show();
            cardSelectAddress.requestFocus();
            return;
        }
        // Check payment option only if it's visible
        if (radioPaymentOption.getVisibility() == View.VISIBLE && radioPaymentOption.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a payment option", Toast.LENGTH_SHORT).show();
            radioPaymentOption.requestFocus();
            return;
        }
        if (selectedPaymentMethod == null) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            radioPaymentMethods.requestFocus();
            return;
        }
        // --- END VALIDATION ---

        progressBar.setVisibility(View.VISIBLE);
        btnPreOrder.setEnabled(false);

        PayMongoBackendAPI backend = ApiClient.getBackendClient(this).create(PayMongoBackendAPI.class);

        // --- UPDATED Price logic using fixed advance amount ---
        double fullUnitPrice = product.getPrice();
        double finalUnitPrice; // Price PER ITEM to charge now
        String itemNameForCheckout;

        // Check the 'isPayingAdvance' boolean (only relevant if options are visible)
        if (radioPaymentOption.getVisibility() == View.VISIBLE && isPayingAdvance) {
            finalUnitPrice = advanceAmountPerItem; // Use the fixed advance amount from product
            itemNameForCheckout = "Advance Payment: " + product.getItemName();
        } else {
            // Default to full price (if not pre-order, invalid advance, or 'Pay Full' selected)
            finalUnitPrice = fullUnitPrice;
            itemNameForCheckout = product.getItemName();
        }

        // Calculate TOTAL amount to charge now in centavos
        int finalTotalAmountInCentavos = (int) Math.round((finalUnitPrice * quantity) * 100);
        int finalQuantity = quantity;

        SessionManager session = new SessionManager(this);

        OrderRequestDto orderDetails = new OrderRequestDto();
        orderDetails.userId = session.getUserId();
        orderDetails.businessId = product.getVendorId();

        orderDetails.total = finalUnitPrice * finalQuantity; // Total price to be charged NOW

        SimpleDateFormat backendSdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        backendSdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        orderDetails.deliveryDate = backendSdfDate.format(selectedDateCalendar.getTime());

        SimpleDateFormat backendSdfTime = new SimpleDateFormat("HH:mm:ss", Locale.US);
        orderDetails.deliveryTime = backendSdfTime.format(selectedTime.getTime());

        detailedAddress.phoneNumber = contact;
        orderDetails.address = detailedAddress;

        List<ItemDto> items = new ArrayList<>();
        ItemDto item = new ItemDto();
        item.productId = product.getProductId();
        item.productName = product.getItemName();
        item.productDescription = product.getDescription();
        item.quantity = finalQuantity;
        item.priceAtOrderTime = product.getPrice(); // Always send the original full price
        item.isPreOrder = isPreorderMode; // Still need this flag
        items.add(item);
        orderDetails.items = items;

        String orderDetailsJson = new Gson().toJson(orderDetails);
        Log.d("PreOrderCheckout", "Serialized Metadata: " + orderDetailsJson);

        CheckoutRequestDto request = new CheckoutRequestDto(
                finalTotalAmountInCentavos, // Send the TOTAL amount to charge now
                itemNameForCheckout,
                finalQuantity, // Quantity is still relevant for description
                (int) product.getVendorId(),
                method,
                orderDetailsJson
        );

        // Inside startCheckout method...
        backend.createCheckout(request).enqueue(new Callback<CheckoutResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<CheckoutResponseDto> call, @NonNull Response<CheckoutResponseDto> response) {
                progressBar.setVisibility(View.GONE);
                // Keep button disabled here in success case

                if (response.isSuccessful() && response.body() != null) {
                    String checkoutUrl = response.body().getCheckoutUrl();
                    String checkoutId = response.body().getCheckoutId(); // Optional: You might need this later
                    long draftId = response.body().getDraftId(); // <-- Get draftId from backend response

                    // Validate both URL and draftId
                    // ...
                    if (checkoutUrl != null && !checkoutUrl.isEmpty() && draftId > 0) {

                        // 1. Set the pending flag with the draftId
                        // 1. Set the pending flag with the draftId
                        PreOrderCheckoutActivity.this.pendingDraftId = draftId;

                        // 2. Open the browser (Using Custom Tabs)
                        openCheckoutUrl(checkoutUrl);

                        // 3. DO NOT launch confirmation or finish() here.
                        //    We will wait for onResume() to be called when the user returns.
                    } else {
                        Log.e("PreOrderCheckout", "Invalid checkout URL or draftId received from backend. URL: " + checkoutUrl + ", DraftID: " + draftId);
                        Toast.makeText(PreOrderCheckoutActivity.this, "Error starting payment process.", Toast.LENGTH_SHORT).show();
                        btnPreOrder.setEnabled(true); // Re-enable button on this specific error
                    }
                } else {
                    // Handle backend API error (e.g., 400, 500)
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (IOException e) { /* ignore */ }
                    Log.e("PreOrderCheckout", "Error creating checkout (API): " + response.code() + " - " + errorBody);
                    Toast.makeText(PreOrderCheckoutActivity.this, "Error creating checkout (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    btnPreOrder.setEnabled(true); // Re-enable on API error
                }
            }



            @Override
            public void onFailure(@NonNull Call<CheckoutResponseDto> call, @NonNull Throwable t) {
                // Handle network failure
                progressBar.setVisibility(View.GONE);
                btnPreOrder.setEnabled(true); // Re-enable on network failure
                Log.e("PreOrderCheckout", "Network error creating checkout", t);
                Toast.makeText(PreOrderCheckoutActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    // --- Date Validator Inner Class ---
    private static class SelectableDatesValidator implements CalendarConstraints.DateValidator {
        // ... (This class remains the same) ...
        private final HashSet<Long> enabledDates;
        public SelectableDatesValidator(HashSet<Long> enabledDates) { this.enabledDates = enabledDates; }
        public static final Parcelable.Creator<SelectableDatesValidator> CREATOR = new Parcelable.Creator<SelectableDatesValidator>() {
            @Override
            public SelectableDatesValidator createFromParcel(Parcel source) {
                long[] array = source.createLongArray();
                HashSet<Long> dates = new HashSet<>();
                if (array != null) {
                    for (long l : array) {
                        dates.add(l);
                    }
                }
                return new SelectableDatesValidator(dates);
            }
            @Override public SelectableDatesValidator[] newArray(int size) { return new SelectableDatesValidator[size]; }
        };
        @Override public int describeContents() { return 0; }
        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            long[] array = new long[enabledDates.size()];
            int i = 0;
            for (Long l : enabledDates) {
                array[i++] = l;
            }
            dest.writeLongArray(array);
        }
        @Override public boolean isValid(long date) { return enabledDates.contains(date); }
        @Override public int hashCode() { return enabledDates.hashCode(); }
        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            SelectableDatesValidator that = (SelectableDatesValidator) obj;
            return enabledDates.equals(that.enabledDates);
        }
    }
}