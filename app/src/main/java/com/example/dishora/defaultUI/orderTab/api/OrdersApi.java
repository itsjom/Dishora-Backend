package com.example.dishora.defaultUI.orderTab.api;

import com.example.dishora.defaultUI.orderTab.model.Order;
import com.example.dishora.defaultUI.orderTab.orderDetails.model.OrderDetail;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OrdersApi {
    @GET("orders/my-orders")
    Call<List<Order>> getMyOrders();

    @GET("orders/{id}")
    Call<OrderDetail> getOrderDetails(@Path("id") long orderId);

    @POST("orders/{id}/cancel")
    Call<Void> cancelOrder(@Path("id") long orderId);
}