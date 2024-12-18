package com.example.airqualityapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {

    private Button buttonByCity;
    private Button buttonByMap;
    private Button buttonByCoordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        buttonByCity = findViewById(R.id.buttonByCity);
        buttonByMap = findViewById(R.id.buttonByMap);
        buttonByCoordinates = findViewById(R.id.buttonByCoordinates);

        buttonByCity.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
            startActivity(intent);
        });

        buttonByMap.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, MapActivity.class);
            startActivity(intent);
        });

        buttonByCoordinates.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, CoordinatesActivity.class);
            startActivity(intent);
        });
    }
}
