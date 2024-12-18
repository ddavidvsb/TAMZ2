package com.example.airqualityapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.example.airqualityapp.data.model.AirQualityResponse;
import com.example.airqualityapp.data.model.CityResponse;
import com.example.airqualityapp.data.network.AirQualityApiService;
import com.example.airqualityapp.data.network.GeocodingApiService;
import com.example.airqualityapp.data.network.RetrofitClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "myPrefs";
    private static final String KEY_LAST_CITY = "lastCity";
    private static final String KEY_FAV_CITIES = "favCities";

    private TextView textViewData;
    private AutoCompleteTextView editTextCity;
    private Button buttonLoadCity;
    private Button buttonSaveCity;

    private ArrayAdapter<String> adapter;
    private List<String> citySuggestionsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewData = findViewById(R.id.textViewData);
        editTextCity = findViewById(R.id.editTextCity);
        buttonLoadCity = findViewById(R.id.buttonLoadCity);
        buttonSaveCity = findViewById(R.id.buttonSaveCity);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String lastCity = prefs.getString(KEY_LAST_CITY, null);

        citySuggestionsList = new ArrayList<>(Arrays.asList("London", "Berlin", "Paris", "Prague", "New York", "Tokyo", "Delhi", "Sydney"));

        String favCitiesStr = prefs.getString(KEY_FAV_CITIES, "");
        if (!favCitiesStr.isEmpty()) {
            String[] favCities = favCitiesStr.split(",");
            for (String city : favCities) {
                if (!city.trim().isEmpty() && !citySuggestionsList.contains(city.trim())) {
                    citySuggestionsList.add(city.trim());
                }
            }
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, citySuggestionsList);
        editTextCity.setAdapter(adapter);

        if (lastCity != null) {
            textViewData.setText("Poslední hledané město: " + lastCity + "\nZadejte nové město nebo vyberte z nabídky.");
        } else {
            textViewData.setText("Zadejte město nebo vyberte z nabídky.");
        }

        editTextCity.setThreshold(0);
        editTextCity.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                editTextCity.showDropDown();
            }
        });
        editTextCity.setOnClickListener(v -> editTextCity.showDropDown());

        buttonLoadCity.setOnClickListener(v -> {
            String cityName = editTextCity.getText().toString().trim();
            if (!cityName.isEmpty()) {
                loadAirQualityForCity(cityName);
            } else {
                textViewData.setText("Zadejte město prosím");
            }
        });

        buttonSaveCity.setOnClickListener(v -> {
            String cityName = editTextCity.getText().toString().trim();
            if (!cityName.isEmpty()) {
                if (!citySuggestionsList.contains(cityName)) {
                    citySuggestionsList.add(cityName);
                    adapter.notifyDataSetChanged();
                }
                saveFavoriteCities(cityName);
                textViewData.setText("Město " + cityName + " uloženo do oblíbených.");
            } else {
                textViewData.setText("Nejprve zadejte město, které chcete uložit.");
            }
        });
    }

    private void loadAirQualityForCity(String cityName) {
        textViewData.setText("Hledám město: " + cityName);

        String apiKey = getString(R.string.openweather_api_key);
        GeocodingApiService geoService = RetrofitClient.getClient().create(GeocodingApiService.class);
        Call<List<CityResponse>> call = geoService.getCityCoordinates(cityName, 1, apiKey);

        call.enqueue(new Callback<List<CityResponse>>() {
            @Override
            public void onResponse(Call<List<CityResponse>> call, Response<List<CityResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    CityResponse city = response.body().get(0);
                    double lat = city.lat;
                    double lon = city.lon;
                    loadAirQualityData(cityName, lat, lon);
                } else {
                    textViewData.setText("Nepodařilo se najít souřadnice pro " + cityName);
                }
            }

            @Override
            public void onFailure(Call<List<CityResponse>> call, Throwable t) {
                textViewData.setText("Chyba: " + t.getMessage());
            }
        });
    }

    private void loadAirQualityData(String cityName, double lat, double lon) {
        textViewData.setText("Načítám data z API pro " + cityName + " (" + lat + ", " + lon + ") ...");

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
                    sb.append("Město: ").append(cityName).append("\n");
                    sb.append("AQI: ").append(aqi).append("\n\n");
                    sb.append("CO: ").append(c.co).append(" μg/m³\n");
                    sb.append("NO: ").append(c.no).append(" μg/m³\n");
                    sb.append("NO2: ").append(c.no2).append(" μg/m³\n");
                    sb.append("O3: ").append(c.o3).append(" μg/m³\n");
                    sb.append("SO2: ").append(c.so2).append(" μg/m³\n");
                    sb.append("PM2.5: ").append(c.pm2_5).append(" μg/m³\n");
                    sb.append("PM10: ").append(c.pm10).append(" μg/m³\n");
                    sb.append("NH3: ").append(c.nh3).append(" μg/m³\n");

                    textViewData.setText(sb.toString());
                    saveLastCity(cityName);
                } else {
                    textViewData.setText("Nepodařilo se načíst data pro " + cityName);
                }
            }

            @Override
            public void onFailure(Call<AirQualityResponse> call, Throwable t) {
                textViewData.setText("Chyba: " + t.getMessage());
            }
        });
    }

    private void saveLastCity(String cityName) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_LAST_CITY, cityName).apply();
    }

    private void saveFavoriteCities(String cityName) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String favCitiesStr = prefs.getString(KEY_FAV_CITIES, "");
        List<String> favCities = new ArrayList<>();
        if (!favCitiesStr.isEmpty()) {
            favCities.addAll(Arrays.asList(favCitiesStr.split(",")));
        }
        if (!favCities.contains(cityName)) {
            favCities.add(cityName);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < favCities.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(favCities.get(i));
        }
        prefs.edit().putString(KEY_FAV_CITIES, sb.toString()).apply();
    }
}
