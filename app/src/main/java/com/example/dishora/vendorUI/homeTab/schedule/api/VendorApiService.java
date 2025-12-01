package com.example.dishora.vendorUI.homeTab.schedule.api;

import com.example.dishora.vendorUI.homeTab.schedule.model.ScheduleCreateRequest;
import com.example.dishora.vendorUI.homeTab.schedule.model.ScheduleItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface VendorApiService {

    // GET: /api/vendor/schedule
    @GET("vendor/schedule")
    Call<List<ScheduleItem>> getPreOrderSchedules();

    // POST: /api/vendor/schedule
    // Used for both creating a new schedule or updating capacity for an existing date
    @POST("vendor/schedule")
    Call<ScheduleItem> postPreOrderSchedule(@Body ScheduleCreateRequest request);

    // NOTE: If your vendor's businessId is passed as a header or path variable,
    // you'll need to modify these methods accordingly (e.g., @Path or @Header annotations).
}