package com.example.dishora;

import android.content.Intent;
// REMOVE import android.net.Uri; // No longer needed for this method
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar; // Import ProgressBar
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

// Import your DTO and API client
import com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.DTO.OrderStatusResponseDto;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.PayMongoBackendAPI;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutConfirmationActivity extends AppCompatActivity {

    private static final String TAG = "OrderConfirmActivity";

    private ImageView imgStatusIcon;
    private TextView txtStatusTitle;
    private TextView txtStatusMessage;
    private Button btnGoToOrders;
    private Button btnClose;
    private ProgressBar progressBar; // Add ProgressBar

    // --- Polling Variables ---
    private Handler pollingHandler;
    private Runnable pollingRunnable;
    private int pollingAttempts = 0;
    private final int MAX_POLLING_ATTEMPTS = 10; // Poll for 20 seconds (10 * 2s)
    private final int POLLING_INTERVAL = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_confirmation);

        imgStatusIcon = findViewById(R.id.imgStatusIcon);
        txtStatusTitle = findViewById(R.id.txtStatusTitle);
        txtStatusMessage = findViewById(R.id.txtStatusMessage);
        btnGoToOrders = findViewById(R.id.btnGoToOrders);
        btnClose = findViewById(R.id.btnClose);
        progressBar = findViewById(R.id.progressBar);

        pollingHandler = new Handler(Looper.getMainLooper());

        handleIntent(getIntent()); // Process the intent that started the activity

        Log.d(TAG, "onCreate finished.");

        btnClose.setOnClickListener(v -> {
            stopPolling(); // Stop any pending polling
            finish();
        });

        btnGoToOrders.setOnClickListener(v -> {
            // TODO: Implement navigation to your "My Orders" screen
            Toast.makeText(this, "Navigate to My Orders (Not Implemented)", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, MyOrdersActivity.class);
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            // startActivity(intent);
            finish(); // Close confirmation after navigating
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent called.");
        // If activity is already running and receives a new intent (less likely in this flow)
        stopPolling();
        handleIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called.");
        stopPolling(); // Clean up handler on destroy
    }

    // --- THIS METHOD IS UPDATED ---
    private void handleIntent(Intent intent) {
        Log.d(TAG, "handleIntent received intent.");
        if (intent != null) {
            // Get draftId directly from intent extras passed by PreOrderCheckoutActivity
            long draftId = intent.getLongExtra("draftId", -1); // -1 is default if not found

            if (draftId != -1 && draftId > 0) { // Also check if > 0
                Log.d(TAG, "handleIntent: Received draftId: " + draftId);
                showProcessingUI(draftId); // Show "Processing..."
                startPolling(draftId); // Start checking the status
            } else {
                // Handle case where draftId wasn't passed correctly or is invalid
                Log.e(TAG, "handleIntent: draftId not found or invalid in Intent extras. Value: " + draftId);
                showGenericError();
            }
        } else {
            Log.e(TAG, "handleIntent: Intent was null.");
            showGenericError();
        }
        // --- REMOVED ALL THE OLD DEEP LINK PARSING LOGIC ---
        // (No need to check ACTION_VIEW, scheme, host, path, query parameters)
    }
    // --- END OF UPDATED METHOD ---


    private void startPolling(long draftId) {
        stopPolling(); // Stop any previous polling
        pollingAttempts = 0;

        Log.d(TAG, "startPolling: Initiating polling for draftId: " + draftId);

        pollingRunnable = () -> {
            Log.d(TAG, "Polling... Attempt: " + (pollingAttempts + 1));
            PayMongoBackendAPI api = ApiClient.getBackendClient(this).create(PayMongoBackendAPI.class);
            api.getOrderStatusByDraftId(draftId).enqueue(new Callback<OrderStatusResponseDto>() {
                @Override
                public void onResponse(@NonNull Call<OrderStatusResponseDto> call, @NonNull Response<OrderStatusResponseDto> response) {
                    Log.d(TAG, "startPolling: onResponse received. Code: " + response.code());
                    if (isDestroyed() || isFinishing()) return; // Check if activity is still valid

                    if (response.isSuccessful() && response.body() != null) {
                        String status = response.body().getStatus();
                        Log.i(TAG, "startPolling: API returned status: '" + status + "'");
                        switch (status) {
                            case "processed":
                                Log.d(TAG, "Polling success: Order is processed.");
                                showSuccessUI(draftId);
                                break;
                            case "pending":
                                pollingAttempts++;
                                if (pollingAttempts < MAX_POLLING_ATTEMPTS) {
                                    // Not processed yet, poll again
                                    pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL);
                                } else {
                                    // Timed out
                                    Log.w(TAG, "Polling timed out. Showing pending status.");
                                    showPendingUI(draftId);
                                }
                                break;
                            default: // "not_found" or other
                                Log.e(TAG, "Polling error: Draft not found or invalid status from API.");
                                showGenericError();
                                break;
                        }
                    } else {
                        Log.e(TAG, "Polling API response error: " + response.code());
                        showGenericError(); // Show error on bad response
                    }
                }

                @Override
                public void onFailure(@NonNull Call<OrderStatusResponseDto> call, @NonNull Throwable t) {
                    if (isDestroyed() || isFinishing()) return; // Check if activity is still valid
                    Log.e(TAG, "startPolling: Polling API onFailure", t);
                    // Don't show generic error immediately, maybe network temporary. Try again.
                    pollingAttempts++;
                    if (pollingAttempts < MAX_POLLING_ATTEMPTS) {
                        pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL);
                    } else {
                        Log.w(TAG, "Polling timed out after network failure.");
                        showGenericError();
                    }
                }
            });
        };

        // Start the first poll immediately
        pollingHandler.post(pollingRunnable);
    }

    private void stopPolling() {
        if (pollingHandler != null && pollingRunnable != null) {
            Log.d(TAG, "stopPolling: Removing polling callbacks.");
            pollingHandler.removeCallbacks(pollingRunnable);
        }
    }

    // --- UI STATE METHODS (Added null checks for safety) ---
    private void showProcessingUI(long draftId) {
        Log.d(TAG, "showProcessingUI called for draftId: " + draftId);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (imgStatusIcon != null) imgStatusIcon.setVisibility(View.GONE);
        if (txtStatusTitle != null) txtStatusTitle.setText("Confirming Payment...");
        if (txtStatusMessage != null) txtStatusMessage.setText("Please wait while we confirm your order (Ref: " + draftId + ").");
        if (btnGoToOrders != null) btnGoToOrders.setVisibility(View.GONE);
        if (btnClose != null) btnClose.setVisibility(View.VISIBLE);
    }

    private void showSuccessUI(long draftId) {
        Log.d(TAG, "showSuccessUI called for draftId: " + draftId);
        stopPolling();
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (imgStatusIcon != null) {
            imgStatusIcon.setVisibility(View.VISIBLE);
            imgStatusIcon.setImageResource(R.drawable.ic_success_placeholder); // Replace with your success icon
        }
        if (txtStatusTitle != null) {
            txtStatusTitle.setText("Payment Successful!");
            // Make sure you have a color named 'green' in your colors.xml
            try {
                txtStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.green));
            } catch (Exception e) {
                Log.w(TAG, "R.color.green not found, using default text color.");
                // Use a default color if 'green' isn't defined
                // txtStatusTitle.setTextColor(ContextCompat.getColor(this, android.R.color.primary_text_light));
            }
        }
        if (txtStatusMessage != null) txtStatusMessage.setText("Your order (Ref: " + draftId + ") is confirmed. Check 'My Orders' for details.");
        if (btnGoToOrders != null) btnGoToOrders.setVisibility(View.VISIBLE);
        if (btnClose != null) btnClose.setVisibility(View.VISIBLE);
    }

    private void showCancelledUI() {
        Log.d(TAG, "showCancelledUI called.");
        // This case should ideally not happen in this flow, but handle defensively
        stopPolling();
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (imgStatusIcon != null) {
            imgStatusIcon.setVisibility(View.VISIBLE);
            imgStatusIcon.setImageResource(R.drawable.ic_failed_placeholder); // Replace with your cancel/fail icon
        }
        if (txtStatusTitle != null) {
            txtStatusTitle.setText("Payment Cancelled");
            txtStatusTitle.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark)); // Example color
        }
        if (txtStatusMessage != null) txtStatusMessage.setText("Your payment was not completed. Please try again.");
        if (btnGoToOrders != null) btnGoToOrders.setVisibility(View.GONE);
        if (btnClose != null) btnClose.setVisibility(View.VISIBLE);
    }

    private void showPendingUI(long draftId) {
        Log.d(TAG, "showPendingUI called for draftId: " + draftId);
        stopPolling();
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (imgStatusIcon != null) {
            imgStatusIcon.setVisibility(View.VISIBLE);
            imgStatusIcon.setImageResource(R.drawable.ic_success_placeholder); // Still success
        }
        if (txtStatusTitle != null) {
            txtStatusTitle.setText("Payment Received!");
            // Make sure you have a color named 'green' in your colors.xml
            try {
                txtStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.green));
            } catch (Exception e) {
                Log.w(TAG, "R.color.green not found, using default text color.");
            }
        }
        if (txtStatusMessage != null) txtStatusMessage.setText("Your payment (Ref: " + draftId + ") is being processed. It may take a moment to appear in 'My Orders'.");
        if (btnGoToOrders != null) btnGoToOrders.setVisibility(View.VISIBLE);
        if (btnClose != null) btnClose.setVisibility(View.VISIBLE);
    }

    private void showGenericError() {
        Log.e(TAG, "showGenericError called.");
        stopPolling();
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (imgStatusIcon != null) {
            imgStatusIcon.setVisibility(View.VISIBLE);
            imgStatusIcon.setImageResource(R.drawable.ic_failed_placeholder); // Use cancel/fail icon
        }
        if (txtStatusTitle != null) {
            txtStatusTitle.setText("Something went wrong");
            txtStatusTitle.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
        if (txtStatusMessage != null) txtStatusMessage.setText("Could not display order confirmation details.");
        if (btnGoToOrders != null) btnGoToOrders.setVisibility(View.GONE);
        if (btnClose != null) btnClose.setVisibility(View.VISIBLE);
    }
}

