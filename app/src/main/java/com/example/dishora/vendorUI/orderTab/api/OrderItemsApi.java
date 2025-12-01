package com.example.dishora.vendorUI.orderTab.api;

import com.example.dishora.vendorUI.orderTab.model.GroupStatusUpdateRequest;
import com.example.dishora.vendorUI.orderTab.model.OrderItem;
import com.example.dishora.vendorUI.orderTab.model.StatusUpdateRequest;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit interface for vendor order endpoints.
 * Uses token-based authentication (Bearer token automatically added by ApiClient).
 */
public interface OrderItemsApi {

    // Fetch all order items belonging to the logged‑in vendor (token identifies vendor)
    @GET("Orderitems/mine")
    Call<List<OrderItem>> getMyOrderItems(
            @Query("status") String status,
            @Query("isPreOrder") Boolean isPreOrder
    );

    @PUT("OrderItems/group-status")
    Call<Void> updateOrderStatusByGroupId(@Body GroupStatusUpdateRequest body);

    // Update order‑item status by ID
    // @PUT("orderitems/{id}/status")
    // Call<Void> updateStatus(@Path("id") long orderItemId, @Body StatusUpdateRequest request);
}