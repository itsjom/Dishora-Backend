package com.example.dishora.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://dishora-mobile-env.eba-jym3ahey.ap-southeast-1.elasticbeanstalk.com/api/";
    private static final String PAYMONGO_BASE_URL = "https://api.paymongo.com/v1/";

    private static Retrofit backendRetrofitAuth;
    private static Retrofit backendRetrofitNoAuth;
    private static Retrofit payMongoRetrofit;

    // --- Logging
    private static HttpLoggingInterceptor getLoggingInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                msg -> android.util.Log.d("HTTP_LOG", msg)
        );
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return loggingInterceptor;
    }

    // --- Authorized client
    private static OkHttpClient getAuthorizedClient(Context context) {
        return new OkHttpClient.Builder()
                .addInterceptor(getLoggingInterceptor())
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    SharedPreferences prefs = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                    String token = prefs.getString("jwt_token", null);

                    Request.Builder builder = original.newBuilder();
                    if (token != null && !token.isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                })
                .build();
    }

    // --- No auth client
    private static OkHttpClient getNoAuthClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(getLoggingInterceptor())
                .build();
    }

    // --- Authorized backend
    public static Retrofit getBackendClient(Context context) {
        if (backendRetrofitAuth == null) {
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
                    .create();

            backendRetrofitAuth = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(getAuthorizedClient(context))
                    .build();
        }
        return backendRetrofitAuth;
    }

    // --- Public backend
    public static Retrofit getBackendClient() {
        if (backendRetrofitNoAuth == null) {
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
                    .create();

            backendRetrofitNoAuth = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(getNoAuthClient())
                    .build();
        }
        return backendRetrofitNoAuth;
    }

    // --- PayMongo
    public static Retrofit getPayMongoClient() {
        if (payMongoRetrofit == null) {
            payMongoRetrofit = new Retrofit.Builder()
                    .baseUrl(PAYMONGO_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getNoAuthClient())
                    .build();
        }
        return payMongoRetrofit;
    }
}