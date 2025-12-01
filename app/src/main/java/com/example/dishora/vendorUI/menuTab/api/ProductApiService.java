package com.example.dishora.vendorUI.menuTab.api;

import com.example.dishora.vendorUI.menuTab.api.responseModel.UploadResponse;
import com.example.dishora.vendorUI.menuTab.model.Product;
import com.example.dishora.vendorUI.menuTab.request.ProductUpdateRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductApiService {

    // ========= Upload Product ==========
    @Multipart
    @POST("products/upload")
    Call<UploadResponse> uploadProduct(
            @Part MultipartBody.Part image,
            @Part("item_name") RequestBody itemName,
            @Part("price") RequestBody price,
            @Part("advance_amount") RequestBody advancePrice, // <-- ADDED
            @Part("business_id") RequestBody businessId,
            @Part("product_category_id") RequestBody productCategoryId,
            @Part("is_pre_order") RequestBody isPreOrder,
            @Part("is_available") RequestBody isAvailable,
            @Part("description") RequestBody description,
            @Part("delivery_cutoff_time") RequestBody cutOffTime,
            @Part List<MultipartBody.Part> dietary_specification_ids
    );

    // ========= Get Single Product ==========
    @GET("products/{id}")
    Call<Product> getProduct(@Path("id") long productId);

    // ========= Get Vendor Products (ALL, vendor-side view) ==========
    @GET("products/vendor/{businessId}")
    Call<List<Product>> getVendorProducts(@Path("businessId") long businessId);

    // ========= Update Availability ==========
    @PATCH("products/{id}/availability")
    Call<Void> updateAvailability(
            @Path("id") long productId,
            @Query("isAvailable") boolean isAvailable
    );

    // ========= Update Product (no image) ==========
    @PUT("products/{id}")
    Call<Void> updateProduct(
            @Path("id") long id,
            @Body ProductUpdateRequest productUpdateRequest
    );

    // ========= Update Product (with image) ==========
//    @Multipart
//    @PUT("products/{id}/upload")
//    Call<Void> updateProductWithImage(
//            @Path("id") long id,
//            @Part MultipartBody.Part image,
//            @Part("item_name") RequestBody itemName,
//            @Part("price") RequestBody price,
//            @Part("product_category_id") RequestBody categoryId,
//            @Part("is_pre_order") RequestBody isPreOrder,
//            @Part("is_available") RequestBody isAvailable,
//            @Part("description") RequestBody description
//
//    );

    @Multipart
    @PUT("products/{id}/upload")
    Call<UploadResponse> updateProductWithImage(
            @Path("id") long id,
            @Part MultipartBody.Part image,
            @Part("item_name") RequestBody itemName,
            @Part("price") RequestBody price,
            @Part("advance_amount") RequestBody advancePrice, // <-- ADDED
            @Part("product_category_id") RequestBody productCategoryId,
            @Part("is_pre_order") RequestBody isPreOrder,
            @Part("is_available") RequestBody isAvailable,
            @Part("description") RequestBody description,
            @Part("delivery_cutoff_time") RequestBody cutOffTime,
            @Part List<MultipartBody.Part> dietary_specification_ids

    );

    // ========= Delete Product ==========
    @DELETE("products/{id}")
    Call<Void> deleteProduct(@Path("id") long id);
}