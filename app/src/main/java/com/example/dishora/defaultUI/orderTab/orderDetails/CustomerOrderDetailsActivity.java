package com.example.dishora.defaultUI.orderTab.orderDetails;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout; // Add a loading overlay to your XML
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.dishora.R;
import com.example.dishora.defaultUI.orderTab.api.OrdersApi;
import com.example.dishora.defaultUI.orderTab.model.Order;
import com.example.dishora.defaultUI.orderTab.orderDetails.model.OrderDetail;
import com.example.dishora.defaultUI.orderTab.orderDetails.model.OrderItem;
import com.example.dishora.network.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerOrderDetailsActivity extends AppCompatActivity {

    private TextView tvOrderId, tvPlacedDate, tvVendor, tvDeliveryDate, tvTotal, tvPaymentStatus;
    private Chip chipStatus;
    private LinearLayout containerItems;
    private MaterialButton btnCancelOrder;
    private FrameLayout loadingOverlay; // For showing a loading spinner

    private long orderId = -1L;
    private OrderDetail currentOrder;

    // Helper now takes the ID
    public static void start(Context context, long orderId) {
        Intent intent = new Intent(context, CustomerOrderDetailsActivity.class);
        intent.putExtra("order_id", orderId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order_details);

        // --- Toolbar ---
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        btnBack.setOnClickListener(v -> finish());
        toolbarTitle.setText("Order Details");
        // ---

        // --- Bind views ---
        tvOrderId = findViewById(R.id.tvOrderId);
        tvPlacedDate = findViewById(R.id.tvPlacedDate);
        tvVendor = findViewById(R.id.tvVendor);
        tvDeliveryDate = findViewById(R.id.tvDeliveryDate);
        tvTotal = findViewById(R.id.tvTotal);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        chipStatus = findViewById(R.id.chipStatus);
        containerItems = findViewById(R.id.containerItems);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        // loadingOverlay = findViewById(R.id.loadingOverlay); // Uncomment if you add a loading layout

        if (getIntent() != null) {
            orderId = getIntent().getLongExtra("order_id", -1L);
        }

        if (orderId == -1L) {
            Toast.makeText(this, "Order ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnCancelOrder.setOnClickListener(v -> showCancelDialog());
        loadOrderDetails(); // Fetch data from the backend
    }

    @Override
    protected void onResume() {
        super.onResume();
        // This re-loads data when the user returns,
        // so they see any status updates from the vendor.
        loadOrderDetails();
    }

    private void loadOrderDetails() {
        // showLoading(true);
        OrdersApi api = ApiClient.getBackendClient(this).create(OrdersApi.class);
        api.getOrderDetails(orderId).enqueue(new Callback<OrderDetail>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetail> call, @NonNull Response<OrderDetail> response) {
                // showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentOrder = response.body();
                    populateOrderDetails(); // This will now work!
                } else {
                    Toast.makeText(CustomerOrderDetailsActivity.this, "Failed to load details: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<OrderDetail> call, @NonNull Throwable t) {
                // showLoading(false);
                Toast.makeText(CustomerOrderDetailsActivity.this, "Network Error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateOrderDetails() {
        if (currentOrder == null) return;

        tvOrderId.setText(String.format(Locale.getDefault(), "Order #%d", currentOrder.getId()));
        tvPlacedDate.setText(String.format("Placed on %s", currentOrder.getPlacedDate()));
        tvVendor.setText(String.format("Vendor: %s", currentOrder.getVendorName()));
        tvTotal.setText(String.format(Locale.getDefault(), "₱%.2f", currentOrder.getTotal()));
        tvDeliveryDate.setText(String.format("Delivery Date: %s", currentOrder.getDeliveryDate()));
        tvPaymentStatus.setText(currentOrder.isPaid() ? "Paid" : "Cash on Delivery");

        // Chip styling
        chipStatus.setText(currentOrder.getStatus());
        int chipColor;
        int chipTextColor = ContextCompat.getColor(this, R.color.white);
        switch (currentOrder.getStatus().toLowerCase(Locale.ROOT)) {
            case "pending":
                chipColor = ContextCompat.getColor(this, R.color.orange);
                btnCancelOrder.setVisibility(View.VISIBLE);
                break;
            case "preparing": // NEW
                chipColor = ContextCompat.getColor(this, R.color.blue);
                btnCancelOrder.setVisibility(View.GONE);
                break;
            case "for delivery": // NEW
                chipColor = ContextCompat.getColor(this, R.color.red);
                btnCancelOrder.setVisibility(View.GONE);
                break;
            case "completed":
                chipColor = ContextCompat.getColor(this, R.color.green);
                btnCancelOrder.setVisibility(View.GONE);
                break;
            case "cancelled":
                chipColor = ContextCompat.getColor(this, R.color.gray);
                btnCancelOrder.setVisibility(View.GONE);
                break;
            default: // Fallback
                chipColor = ContextCompat.getColor(this, R.color.gray); // Use gray as default
                btnCancelOrder.setVisibility(View.GONE);
                break;
        }
        chipStatus.setChipBackgroundColor(ColorStateList.valueOf(chipColor));
        chipStatus.setTextColor(chipTextColor);

        // Inflate item rows
        containerItems.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        if (currentOrder.getItems() != null) {
            for (OrderItem item : currentOrder.getItems()) {
                View row = inflater.inflate(R.layout.item_order_detail_row, containerItems, false);
                TextView tvProductName = row.findViewById(R.id.tvProductName);
                TextView tvQuantity = row.findViewById(R.id.tvQuantity);
                TextView tvPrice = row.findViewById(R.id.tvPrice);
                TextView tvSubtotal = row.findViewById(R.id.tvSubtotal);

                tvProductName.setText(item.getProductName());
                tvQuantity.setText(String.valueOf(item.getQuantity()));
                tvPrice.setText(String.format(Locale.getDefault(), "₱%.2f", item.getPrice()));
                tvSubtotal.setText(String.format(Locale.getDefault(), "₱%.2f", item.getSubtotal()));
                containerItems.addView(row);
            }
        }
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order?")
                .setPositiveButton("Yes", (dialog, which) -> cancelOrderOnBackend())
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelOrderOnBackend() {
        // showLoading(true);
        OrdersApi api = ApiClient.getBackendClient(this).create(OrdersApi.class);
        api.cancelOrder(orderId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                // showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(CustomerOrderDetailsActivity.this, "Order successfully cancelled.", Toast.LENGTH_SHORT).show();
                    loadOrderDetails(); // Re-fetch data to show the "Cancelled" status from server
                } else {
                    Toast.makeText(CustomerOrderDetailsActivity.this, "Could not cancel order. It may have been processed.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                // showLoading(false);
                Toast.makeText(CustomerOrderDetailsActivity.this, "Network Error. Could not cancel.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // private void showLoading(boolean isLoading) {
    //     if (loadingOverlay != null) {
    //         loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    //     }
    // }
}