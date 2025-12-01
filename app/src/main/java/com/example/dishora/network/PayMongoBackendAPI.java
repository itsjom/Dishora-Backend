package com.example.dishora.network;

import com.example.dishora.defaultUI.homeTab.cart.shopCart.checkout.dto.OrderRequestDto;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.DTO.CheckoutRequestDto;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.DTO.CheckoutResponseDto;
import com.example.dishora.defaultUI.vendorsTab.openingVendors.preOrderActivity.DTO.OrderStatusResponseDto;
import com.example.dishora.vendorUI.homeTab.schedule.model.ScheduleItem;
import com.example.dishora.vendorUI.profileTab.paymentMethod.model.PaymentDetailsBody;
import com.example.dishora.vendorUI.profileTab.paymentMethod.model.PaymentMethod;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PayMongoBackendAPI {
    // Create checkout — now returns our DTO instead of ResponseBody
    @POST("payment/create-checkout")
    Call<CheckoutResponseDto> createCheckout(@Body CheckoutRequestDto request);

    // Fetch all vendor payment methods (this will call your new backend endpoint)
    @GET("business/{id}/payment-methods")
    Call<List<PaymentMethod>> getBusinessPaymentMethods(@Path("id") long vendorId);

    // Business saving endpoint stays — for vendor management UI
    @POST("business/{id}/payment-methods")
    Call<ResponseBody> saveBusinessPaymentMethods(@Path("id") long businessId, @Body Map<String, List<Long>> body);

    // NEW METHOD FOR SAVING PAYMENT DETAILS
    @POST("business/payment-methods/details")
    Call<ResponseBody> savePaymentMethodDetails(@Body PaymentDetailsBody body);

    @POST("orders")
    Call<JsonObject> createOrder(@Body OrderRequestDto orderRequest);

    @GET("payment/status/{checkoutId}") // The endpoint path you will create on your server
    Call<JsonObject> getOrderStatusByCheckoutId(@Path("checkoutId") String checkoutId);

    @GET("v1/business/{businessId}/schedules") // <-- Endpoint URL might be different
    Call<List<ScheduleItem>> getBusinessSchedules(@Path("businessId") long businessId);

    @GET("payment/status-by-draft/{draftId}")
    Call<OrderStatusResponseDto> getOrderStatusByDraftId(@Path("draftId") long draftId);
}
