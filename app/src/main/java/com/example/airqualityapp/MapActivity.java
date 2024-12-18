package com.example.airqualityapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.views.overlay.Overlay;

import com.example.airqualityapp.data.model.AirQualityResponse;
import com.example.airqualityapp.data.network.AirQualityApiService;
import com.example.airqualityapp.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity {

    private MapView map;
    private Button buttonShowData;
    private Button buttonCancelSelection;
    private TextView textViewMapData;
    private double selectedLat = Double.NaN;
    private double selectedLon = Double.NaN;
    private static final String TAG = "MapActivity";

    private Marker currentMarker = null;
    private MapEventsOverlay eventsOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        map = findViewById(R.id.map);
        buttonShowData = findViewById(R.id.buttonShowData);
        buttonCancelSelection = findViewById(R.id.buttonCancelSelection);
        textViewMapData = findViewById(R.id.textViewMapData);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        map.setMultiTouchControls(true);
        map.getController().setZoom(5.0);
        map.getController().setCenter(new GeoPoint(50.08804, 14.42076)); // Praha

        eventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                Log.d(TAG, "Map clicked at: " + p.getLatitude() + ", " + p.getLongitude());

                selectedLat = p.getLatitude();
                selectedLon = p.getLongitude();

                removeCurrentMarkerIfExists();

                currentMarker = new Marker(map);
                currentMarker.setPosition(p);
                currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                currentMarker.setTitle("Vybrané místo");
                map.getOverlays().add(currentMarker);
                map.invalidate();

                textViewMapData.setText("Vybrané souřadnice: " + selectedLat + ", " + selectedLon + "\nKlikněte na 'Zobraz data' pro načtení AQI");
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });

        map.getOverlays().add(eventsOverlay);

        buttonShowData.setOnClickListener(v -> {
            if (Double.isNaN(selectedLat) || Double.isNaN(selectedLon)) {
                Toast.makeText(this, "Nejdříve klikněte na mapu pro výběr místa", Toast.LENGTH_SHORT).show();
            } else {
                loadAirQualityData(selectedLat, selectedLon);
            }
        });

        buttonCancelSelection.setOnClickListener(v -> {
            selectedLat = Double.NaN;
            selectedLon = Double.NaN;
            removeCurrentMarkerIfExists();
            map.invalidate();
            textViewMapData.setText("Výběr zrušen, klikněte na mapu pro nové vybrání místa.");
        });
    }

    private void removeCurrentMarkerIfExists() {
        if (currentMarker != null) {
            map.getOverlays().remove(currentMarker);
            currentMarker = null;
        }
    }

    private void loadAirQualityData(double lat, double lon) {
        String apiKey = getString(R.string.openweather_api_key);
        AirQualityApiService apiService = RetrofitClient.getClient().create(AirQualityApiService.class);
        Call<AirQualityResponse> call = apiService.getAirQualityData(lat, lon, apiKey);

        textViewMapData.setText("Načítám data pro souřadnice: " + lat + ", " + lon + " ...");

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

                    textViewMapData.setText(sb.toString());
                } else {
                    textViewMapData.setText("Nepodařilo se načíst data pro zadané souřadnice.");
                }
            }

            @Override
            public void onFailure(Call<AirQualityResponse> call, Throwable t) {
                textViewMapData.setText("Chyba: " + t.getMessage());
            }
        });
    }
}
