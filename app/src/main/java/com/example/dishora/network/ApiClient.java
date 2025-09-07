package com.example.dishora.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://dishora.azurewebsites.net/";
    private static final String PAYMONGO_BASE_URL = "https://api.paymongo.com/v1/";

    private static Retrofit backendRetrofit;
    private static Retrofit payMongoRetrofit;

    public static OkHttpClient getClient() {
        // Logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
    }

    // ✅ For your backend (login, register, create-intent, attach-method, etc.)
    public static Retrofit getBackendClient() {
        if (backendRetrofit == null) {
            backendRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getClient())
                    .build();
        }
        return backendRetrofit;
    }

    // ✅ For PayMongo public API (create payment methods with PUBLIC KEY)
    public static Retrofit getPayMongoClient() {
        if (payMongoRetrofit == null) {
            payMongoRetrofit = new Retrofit.Builder()
                    .baseUrl(PAYMONGO_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getClient())
                    .build();
        }
        return payMongoRetrofit;
    }
}
