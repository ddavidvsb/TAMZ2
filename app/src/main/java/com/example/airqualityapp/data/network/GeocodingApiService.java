package com.example.airqualityapp.data.network;

import com.example.airqualityapp.data.model.CityResponse;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingApiService {
    @GET("geo/1.0/direct")
    Call<List<CityResponse>> getCityCoordinates(
            @Query("q") String cityName,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );
}
