package com.example.airqualityapp.data.model;

import com.google.gson.annotations.SerializedName;

public class CityResponse {
    @SerializedName("name")
    public String name;

    @SerializedName("lat")
    public double lat;

    @SerializedName("lon")
    public double lon;

    @SerializedName("country")
    public String country;
}
