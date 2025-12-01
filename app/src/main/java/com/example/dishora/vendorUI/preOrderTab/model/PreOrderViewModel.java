package com.example.dishora.vendorUI.preOrderTab.model;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.dishora.network.ApiClient;
import com.example.dishora.utils.SessionManager;
import com.example.dishora.vendorUI.preOrderTab.PreOrderApi; // Your import is correct

import java.util.Collections; // ✅ FIX: Import this
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreOrderViewModel extends ViewModel {

    public MutableLiveData<List<GroupedPreOrder>> allPreOrders = new MutableLiveData<>();
    public MutableLiveData<List<GroupedPreOrder>> pendingOrders = new MutableLiveData<>();
    public MutableLiveData<List<GroupedPreOrder>> preparingOrders = new MutableLiveData<>();
    public MutableLiveData<List<GroupedPreOrder>> forDeliveryOrders = new MutableLiveData<>();
    public MutableLiveData<List<GroupedPreOrder>> completedOrders = new MutableLiveData<>();
    public MutableLiveData<List<GroupedPreOrder>> cancelledOrders = new MutableLiveData<>();

    private PreOrderApi apiService;
    private Context appContext;

    public void init(Context context) {
        if (apiService == null) {
            appContext = context.getApplicationContext();
            apiService = ApiClient.getBackendClient(appContext).create(PreOrderApi.class);
        }
    }

    public void loadAllPreOrders() {
        SessionManager sessionManager = new SessionManager(appContext);
        String authToken = "Bearer " + sessionManager.getToken();

        apiService.getVendorPreOrders(authToken).enqueue(new Callback<List<GroupedPreOrder>>() {
            @Override
            public void onResponse(Call<List<GroupedPreOrder>> call, Response<List<GroupedPreOrder>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GroupedPreOrder> all = response.body();

                    allPreOrders.postValue(all);
                    pendingOrders.postValue(filterByStatus(all, "Pending"));
                    preparingOrders.postValue(filterByStatus(all, "Preparing"));
                    forDeliveryOrders.postValue(filterByStatus(all, "For Delivery"));
                    completedOrders.postValue(filterByStatus(all, "Completed"));
                    cancelledOrders.postValue(filterByStatus(all, "Cancelled"));
                } else {
                    // ✅ FIX: Post empty lists if response is not successful
                    postEmptyLists();
                }
            }
            @Override
            public void onFailure(Call<List<GroupedPreOrder>> call, Throwable t) {
                // ✅ FIX: Post empty lists on a total network failure
                postEmptyLists();
            }
        });
    }

    /**
     * ✅ FIX: Add this helper method
     * Posts an empty list to all LiveData objects.
     * This will trigger the fragment's observer and hide the progress bar.
     */
    private void postEmptyLists() {
        allPreOrders.postValue(Collections.emptyList());
        pendingOrders.postValue(Collections.emptyList());
        preparingOrders.postValue(Collections.emptyList());
        forDeliveryOrders.postValue(Collections.emptyList());
        completedOrders.postValue(Collections.emptyList());
        cancelledOrders.postValue(Collections.emptyList());
    }

    private List<GroupedPreOrder> filterByStatus(List<GroupedPreOrder> orders, String status) {
        return orders.stream()
                .filter(order -> order.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    public void updateGroupStatus(GroupedPreOrder group, String newStatus, Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String authToken = "Bearer " + sessionManager.getToken();

        apiService.updatePreOrderStatus(authToken, group.getGroupId(), newStatus).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Order " + group.getGroupId() + " updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    // Refresh all data
                    loadAllPreOrders();
                } else {
                    Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}