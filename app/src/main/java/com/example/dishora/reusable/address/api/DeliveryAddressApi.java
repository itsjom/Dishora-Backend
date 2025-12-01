package com.example.dishora.reusable.address.api;

import com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.model.UserAddress;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * This interface defines the API endpoints related to user addresses for Retrofit.
 */
public interface DeliveryAddressApi {

    /**
     * Fetches all saved addresses for the current user.
     * The actual endpoint might be more specific, e.g., "users/me/addresses"
     * to ensure you only get the addresses for the logged-in user.
     * @return A Retrofit Call object containing a list of UserAddress.
     */
    @GET("addresses")
    Call<List<UserAddress>> getAddresses();

    /**
     * Creates a new address on the backend.
     * The UserAddress object passed in the body will be serialized to JSON.
     * @param address The UserAddress object to be created.
     * @return A Retrofit Call object containing the newly created UserAddress
     * (often returned by the server with an ID).
     */
    @POST("addresses")
    Call<UserAddress> createAddress(@Body UserAddress address);

}
