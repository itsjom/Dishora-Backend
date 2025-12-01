package com.example.dishora.utils;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GeoapifyHelper {

    private static final String BASE_URL = "https://api.geoapify.com/v1/geocode/";
    private static String apiKey;

    // Call this once in Application or MainActivity
    public static void init(String key) {
        apiKey = key;
    }

    public static class GeoResult {
        public final String formattedAddress;
        public final double latitude;
        public final double longitude;

        public GeoResult(String formattedAddress, double latitude, double longitude) {
            this.formattedAddress = formattedAddress;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String toString() {
            return formattedAddress + " (" + latitude + ", " + longitude + ")";
        }
    }

    // For single geocode
    public interface GeoapifyCallback {
        void onSuccess(GeoResult result);
        void onError(Exception e);
    }

    // For autocomplete (multiple suggestions)
    public interface GeoapifyListCallback {
        void onSuccess(List<GeoResult> results);
        void onError(Exception e);
    }

    // 🔍 AUTOCOMPLETE
    public static void autocomplete(String query, GeoapifyListCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError(new IllegalStateException("Geoapify API key not initialized!"));
            return;
        }

        String url = BASE_URL + "autocomplete?text=" + Uri.encode(query)
                + "&limit=10"  // give user a bit more expanded list
                + "&filter=rect:122.9,13.1,124.6,14.0"   // Camarines Sur bounding box
                + "&bias=proximity:123.1948,13.6218"     // Center results around Naga City
                + "&apiKey=" + apiKey;

        Log.d("GeoapifyHelper", "Requesting: " + url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError(new IOException("Unexpected code " + response));
                    return;
                }
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    JSONArray features = json.getJSONArray("features");

                    List<GeoResult> list = new ArrayList<>();
                    for (int i = 0; i < features.length(); i++) {
                        JSONObject feature = features.getJSONObject(i);
                        JSONObject properties = feature.getJSONObject("properties");
                        JSONObject geometry = feature.getJSONObject("geometry");

                        String formatted = properties.getString("formatted");
                        JSONArray coords = geometry.getJSONArray("coordinates");
                        double lon = coords.getDouble(0);
                        double lat = coords.getDouble(1);

                        list.add(new GeoResult(formatted, lat, lon));
                    }
                    Log.d("GeoapifyHelper", "Raw response: " + body);
                    callback.onSuccess(list);

                } catch (JSONException e) {
                    callback.onError(e);
                }
            }
        });
    }
}