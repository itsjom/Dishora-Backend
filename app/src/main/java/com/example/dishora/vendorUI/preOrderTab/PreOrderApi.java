package com.example.dishora.vendorUI.preOrderTab;

import com.example.dishora.vendorUI.preOrderTab.model.GroupedPreOrder;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PreOrderApi {

    /**
     * Fetches a list of all pre-orders for the authenticated vendor.
     * UPDATE THE URL to match your C# controller's route.
     */
    @GET("api/preorders/vendor")
    Call<List<GroupedPreOrder>> getVendorPreOrders(
            @Header("Authorization") String authToken
    );

    /**
     * Updates the status of a specific pre-order group.
     * UPDATE THE URL to match your C# controller's route.
     */
    @PUT("api/preorders/{groupId}/status")
    Call<Void> updatePreOrderStatus(
            @Header("Authorization") String authToken,
            @Path("groupId") String groupId,
            @Query("newStatus") String newStatus
    );

}