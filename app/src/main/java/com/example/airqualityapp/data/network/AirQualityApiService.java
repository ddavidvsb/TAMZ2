package com.example.airqualityapp.data.network;

import com.example.airqualityapp.data.model.AirQualityResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AirQualityApiService {
    @GET("data/2.5/air_pollution")
    Call<AirQualityResponse> getAirQualityData(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey
    );
}
