package com.example.airqualityapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AirQualityResponse {
    @SerializedName("coord")
    public Coord coord;

    @SerializedName("list")
    public List<AirQualityData> list;

    public static class Coord {
        @SerializedName("lon")
        public double lon;
        @SerializedName("lat")
        public double lat;
    }

    public static class AirQualityData {
        @SerializedName("main")
        public Main main;

        @SerializedName("components")
        public Components components;
    }

    public static class Main {
        @SerializedName("aqi")
        public int aqi;
    }

    public static class Components {
        @SerializedName("co")
        public float co;
        @SerializedName("no")
        public float no;
        @SerializedName("no2")
        public float no2;
        @SerializedName("o3")
        public float o3;
        @SerializedName("so2")
        public float so2;
        @SerializedName("pm2_5")
        public float pm2_5;
        @SerializedName("pm10")
        public float pm10;
        @SerializedName("nh3")
        public float nh3;
    }
}
