package com.example.airqualityapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.airqualityapp.data.model.AirQualityResponse;
import com.example.airqualityapp.data.network.AirQualityApiService;
import com.example.airqualityapp.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoordinatesActivity extends AppCompatActivity {

    private EditText editTextLat;
    private EditText editTextLon;
    private Button buttonShowData;
    private TextView textViewCoordinatesData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinates);

        editTextLat = findViewById(R.id.editTextLat);
        editTextLon = findViewById(R.id.editTextLon);
        buttonShowData = findViewById(R.id.buttonShowData);
        textViewCoordinatesData = findViewById(R.id.textViewCoordinatesData);

        buttonShowData.setOnClickListener(v -> {
            String latStr = editTextLat.getText().toString().trim();
            String lonStr = editTextLon.getText().toString().trim();

            if (!latStr.isEmpty() && !lonStr.isEmpty()) {
                try {
                    double lat = Double.parseDouble(latStr);
                    double lon = Double.parseDouble(lonStr);
                    loadAirQualityData(lat, lon);
                } catch (NumberFormatException e) {
                    textViewCoordinatesData.setText("Zadejte platná čísla pro lat a lon.");
                }
            } else {
                textViewCoordinatesData.setText("Zadejte lat i lon.");
            }
        });
    }

    private void loadAirQualityData(double lat, double lon) {
        textViewCoordinatesData.setText("Načítám data pro souřadnice: " + lat + ", " + lon + " ...");

        String apiKey = getString(R.string.openweather_api_key);
        AirQualityApiService apiService = RetrofitClient.getClient().create(AirQualityApiService.class);
        Call<AirQualityResponse> call = apiService.getAirQualityData(lat, lon, apiKey);

        call.enqueue(new Callback<AirQualityResponse>() {
            @Override
            public void onResponse(Call<AirQualityResponse> call, Response<AirQualityResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().list.isEmpty()) {
                    AirQualityResponse responseBody = response.body();
                    int aqi = responseBody.list.get(0).main.aqi;
                    AirQualityResponse.Components c = responseBody.list.get(0).components;

                    StringBuilder sb = new StringBuilder();
                    sb.append("Souřadnice: ").append(lat).append(", ").append(lon).append("\n");
                    sb.append("AQI: ").append(aqi).append("\n\n");
                    sb.append("CO: ").append(c.co).append(" μg/m³\n");
                    sb.append("NO: ").append(c.no).append(" μg/m³\n");
                    sb.append("NO2: ").append(c.no2).append(" μg/m³\n");
                    sb.append("O3: ").append(c.o3).append(" μg/m³\n");
                    sb.append("SO2: ").append(c.so2).append(" μg/m³\n");
                    sb.append("PM2.5: ").append(c.pm2_5).append(" μg/m³\n");
                    sb.append("PM10: ").append(c.pm10).append(" μg/m³\n");
                    sb.append("NH3: ").append(c.nh3).append(" μg/m³\n");

                    textViewCoordinatesData.setText(sb.toString());
                } else {
                    textViewCoordinatesData.setText("Nepodařilo se načíst data pro zadané souřadnice.");
                }
            }

            @Override
            public void onFailure(Call<AirQualityResponse> call, Throwable t) {
                textViewCoordinatesData.setText("Chyba: " + t.getMessage());
            }
        });
    }
}
