package com.example.dishora.defaultUI.homeTab.search.filter.api;

import com.example.dishora.defaultUI.homeTab.search.model.SearchResultItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FilterApiService {

    @GET("api/filters/{category}")
    Call<List<String>> getFilters(@Path("category") String category);

    @GET("api/search")
    Call<List<SearchResultItem>> getSearchResults(
            @Query("query") String query,
            @Query("filter") String filter
    );
}
