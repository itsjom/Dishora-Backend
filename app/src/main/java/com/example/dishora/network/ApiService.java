package com.example.dishora.network;

import com.example.dishora.models.LoginRequest;
import com.example.dishora.models.LoginResponse;
import com.example.dishora.models.RegisterRequest;
import com.example.dishora.models.RegisterResponse;
import com.example.dishora.models.UnreadCountResponse;
import com.example.dishora.models.VendorStatusResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @POST("users/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("users/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    /** Vendor Registration **/
    @Multipart
    @POST("vendorRegistration")
    Call<ResponseBody> registerVendor(
            @Part("VendorJson") RequestBody VendorJson,
            @Part("BusinessJson") RequestBody BusinessJson,
            @Part("OpeningHoursJson") RequestBody OpeningHoursJson,

            @Part MultipartBody.Part BusinessImage,
            @Part MultipartBody.Part BirRegFile,
            @Part MultipartBody.Part BusinessPermitFile,
            @Part MultipartBody.Part ValidIdFile,
            @Part MultipartBody.Part MayorPermitFile
    );

    @GET("vendorRegistration/status")
    Call<VendorStatusResponse> getVendorStatus();

    //@GET("messages/unread-count")
    //Call<UnreadCountResponse> getUnreadCount();
}
