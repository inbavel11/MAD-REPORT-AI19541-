package com.example.disasapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WeatherActivity extends AppCompatActivity {

    // Temperature Conversion (Fahrenheit ↔ Celsius)
    private EditText fahrenheitInput, windSpeedInput, tempFahrenheitInput, windSpeedMphInput;
    private Button convertTempBtn, convertWindSpeedBtn, calculateWCBtn;
    private TextView celsiusOutput, windSpeedOutput, windChillOutput;

    // Relative Humidity Calculation
    private EditText tempInput, dewPointInput;
    private Button calculateRHBtn;
    private TextView rhOutput;

    // Heat Index Calculation
    private EditText heatIndexTempInput, heatIndexRhInput;
    private Button calculateHIBtn;
    private TextView heatIndexOutput;

    // Station Pressure Calculation
    private EditText altimeterInput, elevationInput;
    private Button calculateStationPressureBtn;
    private TextView stationPressureOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Initialize UI components
        fahrenheitInput = findViewById(R.id.fahrenheit_input);
        convertTempBtn = findViewById(R.id.convert_temp_btn);
        celsiusOutput = findViewById(R.id.celsius_output);

        windSpeedInput = findViewById(R.id.wind_speed_input);
        convertWindSpeedBtn = findViewById(R.id.convert_wind_speed_btn);
        windSpeedOutput = findViewById(R.id.wind_speed_output);

        tempFahrenheitInput = findViewById(R.id.temp_fahrenheit_input);
        windSpeedMphInput = findViewById(R.id.wind_speed_mph_input);
        calculateWCBtn = findViewById(R.id.calculate_wc_btn);
        windChillOutput = findViewById(R.id.wind_chill_output);

        // Set up Temperature Conversion button
        convertTempBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double fahrenheit = Double.parseDouble(fahrenheitInput.getText().toString());
                    double celsius = (fahrenheit - 32) * 5 / 9;
                    celsiusOutput.setText(String.format("%.2f °C", celsius));
                } catch (NumberFormatException e) {
                    celsiusOutput.setText("Invalid input");
                }
            }
        });

        // Set up Wind Speed Conversion button
        convertWindSpeedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double mph = Double.parseDouble(windSpeedInput.getText().toString());
                    double knots = mph * 0.868976;
                    double mSec = mph * 0.44704;
                    double kmHr = mph * 1.60934;
                    String result = String.format("Knots: %.2f\nm/s: %.2f\nkm/h: %.2f", knots, mSec, kmHr);
                    windSpeedOutput.setText(result);
                } catch (NumberFormatException e) {
                    windSpeedOutput.setText("Invalid input");
                }
            }
        });

        // Set up Wind Chill button
        calculateWCBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double tempFahrenheit = Double.parseDouble(tempFahrenheitInput.getText().toString());
                    double windSpeedMph = Double.parseDouble(windSpeedMphInput.getText().toString());
                    double windChillIndex = 35.74 + 0.6215 * tempFahrenheit - 35.75 * Math.pow(windSpeedMph, 0.16) +
                            0.4275 * tempFahrenheit * Math.pow(windSpeedMph, 0.16);
                    windChillOutput.setText(String.format("Wind Chill Index: %.2f °F", windChillIndex));
                } catch (NumberFormatException e) {
                    windChillOutput.setText("Invalid input");
                }
            }
        });


    }
}
