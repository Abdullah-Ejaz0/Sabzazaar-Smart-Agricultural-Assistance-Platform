package com.example.sabzazaar;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SoilHealthActivity extends AppCompatActivity {

    private EditText phInput, nInput, pInput, kInput;
    private ProgressBar phMeter, nMeter, pMeter, kMeter;
    private TextView phValue, nValue, pValue, kValue, soilSuggestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_health);

        phInput = findViewById(R.id.phInput);
        nInput = findViewById(R.id.nInput);
        pInput = findViewById(R.id.pInput);
        kInput = findViewById(R.id.kInput);

        phMeter = findViewById(R.id.phMeter);
        nMeter = findViewById(R.id.nMeter);
        pMeter = findViewById(R.id.pMeter);
        kMeter = findViewById(R.id.kMeter);

        phValue = findViewById(R.id.phValue);
        nValue = findViewById(R.id.nValue);
        pValue = findViewById(R.id.pValue);
        kValue = findViewById(R.id.kValue);
        soilSuggestion = findViewById(R.id.soilSuggestion);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btnUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSoilMeters();
            }
        });

        findViewById(R.id.btnListen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement TTS
            }
        });

        updateSoilMeters();
    }

    private void updateSoilMeters() {
        try {
            float ph = Float.parseFloat(phInput.getText().toString());
            int n = Integer.parseInt(nInput.getText().toString());
            int p = Integer.parseInt(pInput.getText().toString());
            int k = Integer.parseInt(kInput.getText().toString());

            phValue.setText(String.valueOf(ph));
            nValue.setText(String.valueOf(n));
            pValue.setText(String.valueOf(p));
            kValue.setText(String.valueOf(k));

            phMeter.setProgress((int) ((ph / 14.0f) * 100));
            nMeter.setProgress(Math.min(100, n));
            pMeter.setProgress(Math.min(100, p));
            kMeter.setProgress(Math.min(100, k));

            StringBuilder suggestion = new StringBuilder();
            if (ph < 6) suggestion.append("Soil too acidic. Add lime. ");
            else if (ph > 7.5) suggestion.append("Soil too alkaline. Add sulfur. ");
            else suggestion.append("pH is good. ");

            if (n < 40) suggestion.append("Low nitrogen. Add urea. ");
            else if (n > 80) suggestion.append("Nitrogen high. Avoid more. ");
            else suggestion.append("Nitrogen OK. ");

            if (p < 20) suggestion.append("Low phosphorus. Add DAP. ");
            if (k < 30) suggestion.append("Low potassium. Add potash. ");

            if (suggestion.length() == 0) suggestion.append("Your soil is balanced.");
            soilSuggestion.setText(suggestion.toString());

        } catch (NumberFormatException e) {
            // Handle error
        }
    }
}
