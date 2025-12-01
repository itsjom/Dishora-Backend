package com.example.dishora.vendorUI.profileTab.paymentMethod;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.PayMongoBackendAPI;
import com.example.dishora.vendorUI.profileTab.paymentMethod.adapter.PaymentMethodAdapter;
import com.example.dishora.vendorUI.profileTab.paymentMethod.model.PaymentDetailsBody;
import com.example.dishora.vendorUI.profileTab.paymentMethod.model.PaymentMethod;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentMethodActivity extends AppCompatActivity implements PaymentMethodAdapter.OnPaymentMethodClickListener {

    private RecyclerView rvPaymentMethods;
    private PaymentMethodAdapter adapter;
    private MaterialButton btnSave;
    private List<PaymentMethod> methods = new ArrayList<>();

    private PayMongoBackendAPI api;
    private long businessId; // Now we’ll populate it from SharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        // Toolbar
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        ImageView btnSearch = findViewById(R.id.btnSearch);
        toolbarTitle.setText("Add payment methods");
        btnSearch.setVisibility(View.GONE);

        btnBack.setOnClickListener(v -> {
           finish();
        });

        rvPaymentMethods = findViewById(R.id.rvPaymentMethods);
        btnSave = findViewById(R.id.btnSave);

        rvPaymentMethods.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Retrieve businessId saved during login
        SharedPreferences prefs = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        businessId = prefs.getLong("business_id", -1);

        if (businessId == -1) {
            Toast.makeText(this, "Business ID missing. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // ✅ Use authorized backend client (adds JWT)
        api = ApiClient.getBackendClient(this).create(PayMongoBackendAPI.class);

        // Load available payment methods for this business
        fetchPaymentMethods();

        btnSave.setOnClickListener(v -> savePaymentMethods());
    }

    private void fetchPaymentMethods() {
        api.getBusinessPaymentMethods(businessId).enqueue(new Callback<List<PaymentMethod>>() {
            @Override
            public void onResponse(Call<List<PaymentMethod>> call, Response<List<PaymentMethod>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    methods.clear();
                    methods.addAll(response.body());
                    adapter = new PaymentMethodAdapter(methods, PaymentMethodActivity.this);
                    rvPaymentMethods.setAdapter(adapter);
                } else {
                    Toast.makeText(PaymentMethodActivity.this,
                            "Failed to fetch methods. Code: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PaymentMethod>> call, Throwable t) {
                Toast.makeText(PaymentMethodActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void savePaymentMethods() {
        if (adapter == null) return;

        List<Long> enabledMasterIds = new ArrayList<>();
        for (PaymentMethod m : adapter.getMethods()) {
            if (m.isEnabled()) {
                enabledMasterIds.add(m.getMasterMethodId());
            }
        }

        // Wrap into the object expected by backend DTO
        Map<String, List<Long>> body = new HashMap<>();
        body.put("payment_Methods", enabledMasterIds); // must match backend DTO key

        api.saveBusinessPaymentMethods(businessId, body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(PaymentMethodActivity.this,"Payment methods updated!", Toast.LENGTH_SHORT).show();
                            fetchPaymentMethods();
                        } else {
                            Toast.makeText(PaymentMethodActivity.this,
                                    "Save failed. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(PaymentMethodActivity.this,
                                "Error saving: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onMethodClick(PaymentMethod method) {
        // We will now show a dialog to edit the details
        if (!method.isEnabled()) {
            Toast.makeText(this, "Please enable this method first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (method.getId() == 0) {
            Toast.makeText(this, "Please tap 'Save Payment Methods' first to confirm this new method.", Toast.LENGTH_LONG).show();
            return;
        }

        showEditDetailsDialog(method);
    }

    private void showEditDetailsDialog(PaymentMethod method) {
        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_payment_details, null);
        TextInputEditText etAccountName = dialogView.findViewById(R.id.etAccountName);
        TextInputEditText etAccountNumber = dialogView.findViewById(R.id.etAccountNumber);

        // Pre-fill with existing data
        etAccountName.setText(method.getAccountName());
        etAccountNumber.setText(method.getAccountNumber());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit " + method.getName() + " Details")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etAccountName.getText().toString().trim();
                    String newNumber = etAccountNumber.getText().toString().trim();

                    // Call the new API to save these details
                    savePaymentDetails(method, newName, newNumber);
                })
                .show();
    }

    private void savePaymentDetails(PaymentMethod method, String accountName, String accountNumber) {
        // Use the ID of the 'business_payment_method' link
        long businessPaymentMethodId = method.getId();

        PaymentDetailsBody body = new PaymentDetailsBody(businessPaymentMethodId, accountName, accountNumber);

        api.savePaymentMethodDetails(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PaymentMethodActivity.this, "Details saved!", Toast.LENGTH_SHORT).show();

                    // ✅ IMPORTANT: Update local data and refresh adapter
                    method.setAccountName(accountName);
                    method.setAccountNumber(accountNumber);

                    int position = methods.indexOf(method);
                    if (position != -1) {
                        adapter.notifyItemChanged(position);
                    }
                } else {
                    Toast.makeText(PaymentMethodActivity.this,
                            "Failed to save details. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(PaymentMethodActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}