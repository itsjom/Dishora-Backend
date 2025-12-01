package com.example.dishora.vendorUI.orderTab.model;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dishora.network.ApiClient;
import com.example.dishora.vendorUI.orderTab.api.OrderItemsApi;

import java.util.ArrayList;
import java.util.HashMap; // <-- This is fine
import java.util.LinkedHashMap; // <-- ✅ 1. ADD THIS IMPORT
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderViewModel extends ViewModel {

    private OrderItemsApi orderApi;

    // --- LiveData now holds GROUPED orders ---
    private final MutableLiveData<List<GroupedOrder>> _pendingOrders = new MutableLiveData<>();
    private final MutableLiveData<List<GroupedOrder>> _preparingOrders = new MutableLiveData<>();
    private final MutableLiveData<List<GroupedOrder>> _forDeliveryOrders = new MutableLiveData<>();
    private final MutableLiveData<List<GroupedOrder>> _completedOrders = new MutableLiveData<>();
    private final MutableLiveData<List<GroupedOrder>> _cancelledOrders = new MutableLiveData<>();

    // --- MediatorLiveData to combine all GROUPED lists ---
    private final MediatorLiveData<List<GroupedOrder>> _allOrders = new MediatorLiveData<>();

    // Public LiveData that Fragments will observe
    public final LiveData<List<GroupedOrder>> pendingOrders = _pendingOrders;
    public final LiveData<List<GroupedOrder>> preparingOrders = _preparingOrders;
    public final LiveData<List<GroupedOrder>> forDeliveryOrders = _forDeliveryOrders;
    public final LiveData<List<GroupedOrder>> completedOrders = _completedOrders;
    public final LiveData<List<GroupedOrder>> cancelledOrders = _cancelledOrders;
    public final LiveData<List<GroupedOrder>> allOrders = _allOrders;

    public void init(Context context) {
        if (orderApi == null) {
            orderApi = ApiClient.getBackendClient(context).create(OrderItemsApi.class);

            _allOrders.addSource(_pendingOrders, orders -> combineAllOrders());
            _allOrders.addSource(_preparingOrders, orders -> combineAllOrders());
            _allOrders.addSource(_forDeliveryOrders, orders -> combineAllOrders());
            _allOrders.addSource(_completedOrders, orders -> combineAllOrders());
            _allOrders.addSource(_cancelledOrders, orders -> combineAllOrders());
        }
    }

    private void combineAllOrders() {
        List<GroupedOrder> combinedList = new ArrayList<>();
        if (_pendingOrders.getValue() != null) combinedList.addAll(_pendingOrders.getValue());
        if (_preparingOrders.getValue() != null) combinedList.addAll(_preparingOrders.getValue());
        if (_forDeliveryOrders.getValue() != null) combinedList.addAll(_forDeliveryOrders.getValue());
        if (_completedOrders.getValue() != null) combinedList.addAll(_completedOrders.getValue());
        if (_cancelledOrders.getValue() != null) combinedList.addAll(_cancelledOrders.getValue());
        _allOrders.setValue(combinedList);
    }

    public void loadAllOrders() {
        if (orderApi == null) return;
        Log.d("OrderViewModel", "--- Starting to load all orders ---");
        // We pass 'false' for the 'isPreOrder' flag, adjust as needed.
        loadOrdersForStatus("Pending", false, _pendingOrders);
        loadOrdersForStatus("Preparing", false, _preparingOrders);
        loadOrdersForStatus("For Delivery", false, _forDeliveryOrders);
        loadOrdersForStatus("Completed", false, _completedOrders);
        loadOrdersForStatus("Cancelled", false, _cancelledOrders);
    }

    /**
     * Modified to fetch OrderItems, then group them, then post List<GroupedOrder>.
     */
    private void loadOrdersForStatus(String status, boolean isPreOrder, MutableLiveData<List<GroupedOrder>> liveData) {
        Log.d("OrderViewModel", "Fetching items for status: " + status);

        // Call your backend's GET /api/OrderItems/mine endpoint
        orderApi.getMyOrderItems(status, isPreOrder).enqueue(new Callback<List<OrderItem>>() {
            @Override
            public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                Log.d("OrderViewModel", "API Response for " + status + " - Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d("OrderViewModel", "SUCCESS: Received " + response.body().size() + " items for " + status);

                    // --- NEW GROUPING STEP ---
                    List<GroupedOrder> groupedList = groupItems(response.body());
                    Log.d("OrderViewModel", "Grouped " + response.body().size() + " items into " + groupedList.size() + " orders for " + status);
                    liveData.setValue(groupedList);

                } else {
                    Log.w("OrderViewModel", "Response not successful or body is null for " + status);
                    liveData.setValue(new ArrayList<>()); // Post empty list
                }
            }

            @Override
            public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                Log.e("OrderViewModel", "API call FAILED for " + status, t);
                liveData.setValue(new ArrayList<>()); // Post empty list
            }
        });
    }

    /**
     * Helper method to group a flat list of OrderItems into GroupedOrders.
     * ✅ NOW USES THE 'OrderId' (long) FOR RELIABLE GROUPING.
     */
    private List<GroupedOrder> groupItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        // ✅ 2. CHANGE THIS LINE TO USE LinkedHashMap
        Map<Long, GroupedOrder> groupedMap = new LinkedHashMap<>();

        for (OrderItem item : items) {
            // Use the OrderId as the key
            long mainOrderId = item.getOrderId();

            if (!groupedMap.containsKey(mainOrderId)) {
                // This is the first time we see this order, create a new group
                groupedMap.put(mainOrderId, new GroupedOrder(item));
            } else {
                // This order group already exists, just add the item to it
                groupedMap.get(mainOrderId).addItem(item);
            }
        }

        // Return the list of all groups
        return new ArrayList<>(groupedMap.values());
    }


    /**
     * ✅ NEW EFFICIENT METHOD
     * Updates the status for all items in a group with ONE API call
     * to your new /api/OrderItems/group-status endpoint.
     */
    public void updateGroupStatus(GroupedOrder group, String newStatus, Context context) {
        if (orderApi == null || group == null) return;

        // 1. Create the new request body using the group's real OrderId
        GroupStatusUpdateRequest body = new GroupStatusUpdateRequest(group.getOrderId(), newStatus);

        Log.d("OrderViewModel", "Updating group " + group.getOrderId() + " to " + newStatus);

        // 2. Make the single API call
        orderApi.updateOrderStatusByGroupId(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Order updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    // 3. Reload all data on success
                    loadAllOrders();
                } else {
                    // Handle API errors (like 404 Not Found, 400 Bad Request)
                    Toast.makeText(context, "Update Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("OrderViewModel", "Group update failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle network errors (no internet, server down)
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("OrderViewModel", "Group update failed on network.", t);
            }
        });
    }
}