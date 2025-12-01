package com.example.dishora.vendorUI.homeTab.schedule;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.network.ApiClient;
import com.example.dishora.vendorUI.homeTab.schedule.adapter.ScheduleAdapter;
import com.example.dishora.vendorUI.homeTab.schedule.api.VendorApiService;
import com.example.dishora.vendorUI.homeTab.schedule.model.ScheduleCreateRequest; // ✅ Import Request Model
import com.example.dishora.vendorUI.homeTab.schedule.model.ScheduleItem; // ✅ Import Response Model
import com.example.dishora.databinding.ActivityPreOrderScheduleBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PreOrderScheduleActivity extends AppCompatActivity {

    private static final String TAG = "ScheduleActivity";
    private ActivityPreOrderScheduleBinding binding;
    private EditText etAvailableDate;
    private EditText etMaxOrders;
    private Button btnSaveSchedule;
    private RecyclerView rvSchedule;
    private ScheduleAdapter scheduleAdapter;
    private List<ScheduleItem> scheduleList;

    // API Service instance
    private VendorApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreOrderScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        ImageView btnSearch = findViewById(R.id.btnSearch);
        toolbarTitle.setText("Pre-Order Schedule");
        btnSearch.setVisibility(View.GONE);
        btnBack.setOnClickListener(v -> finish());

        // Initialize UI components
        etAvailableDate = binding.etAvailableDate;
        etMaxOrders = binding.etMaxOrders;
        btnSaveSchedule = binding.btnSaveSchedule;
        rvSchedule = binding.rvSchedule;

        // Initialize API Service
        Retrofit authorizedClient = ApiClient.getBackendClient(this);
        apiService = authorizedClient.create(VendorApiService.class);

        // 1. Date Picker Logic
        etAvailableDate.setOnClickListener(v -> showDatePickerDialog());

        // 2. Save Button Logic
        btnSaveSchedule.setOnClickListener(v -> saveSchedule());

        // 3. Setup RecyclerView for existing schedules
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fetch the latest schedule data every time the activity comes to the foreground
        fetchExistingSchedule();
    }

    private void showDatePickerDialog() {
        // ... (DatePicker logic is correct, remains the same) ...
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Display selected date in the format YYYY-MM-DD
                    String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    etAvailableDate.setText(date);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Prevent past dates
        datePickerDialog.show();
    }

    private void saveSchedule() {
        String date = etAvailableDate.getText().toString().trim();
        String maxOrdersStr = etMaxOrders.getText().toString().trim();

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select an available date.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int maxOrders = Integer.parseInt(maxOrdersStr);
            if (maxOrders <= 0) {
                Toast.makeText(this, "Please enter a valid order capacity (> 0).", Toast.LENGTH_SHORT).show();
                return;
            }

            // Build the request object
            ScheduleCreateRequest request = new ScheduleCreateRequest(date, maxOrders);

            // Call API to create/update schedule
            apiService.postPreOrderSchedule(request).enqueue(new Callback<ScheduleItem>() {
                @Override
                public void onResponse(Call<ScheduleItem> call, Response<ScheduleItem> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        handleSaveSuccess(response.body());
                    } else {
                        // Log detailed error and show generic error to user
                        Log.e(TAG, "Save failed. Code: " + response.code() + ", Message: " + response.message());
                        Toast.makeText(PreOrderScheduleActivity.this, "Failed to save schedule. Please check server logs.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ScheduleItem> call, Throwable t) {
                    Log.e(TAG, "Network failure during save: " + t.getMessage(), t);
                    Toast.makeText(PreOrderScheduleActivity.this, "Network Error. Check internet connection.", Toast.LENGTH_LONG).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number for capacity.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSaveSuccess(ScheduleItem newItem) {
        // Look for the date in the existing list to check if we need to update or insert
        int existingIndex = -1;
        for (int i = 0; i < scheduleList.size(); i++) {
            if (scheduleList.get(i).getAvailableDate().equals(newItem.getAvailableDate())) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex != -1) {
            // Update existing item in the list
            scheduleList.set(existingIndex, newItem);
            scheduleAdapter.notifyItemChanged(existingIndex);
            Toast.makeText(this, "Schedule updated for " + newItem.availableDate + " (Capacity: " + newItem.maxOrders + ")", Toast.LENGTH_SHORT).show();
        } else {
            // Insert new item at the top
            scheduleList.add(0, newItem);
            scheduleAdapter.notifyItemInserted(0);
            rvSchedule.scrollToPosition(0);
            Toast.makeText(this, "New schedule saved for " + newItem.availableDate, Toast.LENGTH_SHORT).show();
        }

        // Clear inputs after successful save/update
        etAvailableDate.setText("");
        etMaxOrders.setText("");
    }

    private void fetchExistingSchedule() {
        apiService.getPreOrderSchedules().enqueue(new Callback<List<ScheduleItem>>() {
            @Override
            public void onResponse(Call<List<ScheduleItem>> call, Response<List<ScheduleItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    scheduleList.clear();
                    scheduleList.addAll(response.body());
                    scheduleAdapter.notifyDataSetChanged();
                    Toast.makeText(PreOrderScheduleActivity.this, "Schedules loaded.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Fetch failed. Code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(PreOrderScheduleActivity.this, "Failed to fetch schedules.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ScheduleItem>> call, Throwable t) {
                Log.e(TAG, "Network failure during fetch: " + t.getMessage(), t);
                Toast.makeText(PreOrderScheduleActivity.this, "Network connection error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        scheduleList = new ArrayList<>();
        scheduleAdapter = new ScheduleAdapter(scheduleList);
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        rvSchedule.setAdapter(scheduleAdapter);
    }
}