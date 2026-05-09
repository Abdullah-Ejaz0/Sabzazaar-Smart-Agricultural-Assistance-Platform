package com.example.sabzazaar.activities.scan;

import com.example.sabzazaar.R;
import com.example.sabzazaar.activities.main.MainActivity;
import com.example.sabzazaar.utils.TTSManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sabzazaar.R;
import com.example.sabzazaar.activities.main.MainActivity;
import com.example.sabzazaar.models.ScanResponse;
import com.example.sabzazaar.utils.TTSManager;

public class ScanTreatmentActivity extends AppCompatActivity {
    private TTSManager ttsManager;
    private ScanResponse scanResult;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_treatment);

        scanResult = (ScanResponse) getIntent().getSerializableExtra("scan_result");

        ttsManager = new TTSManager(this);

        if (scanResult != null) {
            updateUI();
        }

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btnDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return to main screen after completion
                Intent intent = new Intent(ScanTreatmentActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.btnListen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakTreatmentInfo();
            }
        });

        updateListenButtonUI();
    }

    private void updateUI() {
        if (scanResult.recommendations != null && scanResult.recommendations.pesticide != null) {
            ScanResponse.PesticideInfo pesticide = scanResult.recommendations.pesticide;
            ((TextView) findViewById(R.id.tvRecommendedTreatment)).setText(pesticide.recommendation != null ? pesticide.recommendation : "Recommended Treatment");
            
            if (pesticide.chemicals != null && !pesticide.chemicals.isEmpty()) {
                StringBuilder names = new StringBuilder();
                StringBuilder dosages = new StringBuilder();
                StringBuilder frequencies = new StringBuilder();
                
                for (ScanResponse.Chemical chemical : pesticide.chemicals) {
                    names.append(chemical.name).append("\n");
                    dosages.append(chemical.dosage).append("\n");
                    frequencies.append(chemical.frequency).append("\n");
                }
                
                ((TextView) findViewById(R.id.tvTreatmentName)).setText(names.toString().trim());
                ((TextView) findViewById(R.id.tvDosage)).setText("Dosage:\n" + dosages.toString().trim());
                ((TextView) findViewById(R.id.tvFrequency)).setText("Frequency:\n" + frequencies.toString().trim());
            } else {
                ((TextView) findViewById(R.id.tvTreatmentName)).setText("Contact local officer");
                ((TextView) findViewById(R.id.tvDosage)).setText("");
                ((TextView) findViewById(R.id.tvFrequency)).setText("");
            }
        }
    }

    private void updateListenButtonUI() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        android.widget.ImageButton btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            btnListen.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    private void speakTreatmentInfo() {
        if (scanResult == null) return;
        
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");

        String text = "Treatment for " + scanResult.diseaseName + ". Follow these steps to treat your crop.";
        if (lang.equals("ur")) {
            text = "کا علاج۔ اپنی فصل کے علاج کے لیے ان اقدامات پر عمل کریں۔" + scanResult.diseaseName;
        } else if (lang.equals("pa")) {
            text = "دا علاج۔ اپنی فصل دے علاج لئی انہاں گلاں تے عمل کرو۔" + scanResult.diseaseName;
        }
        ttsManager.speak(text);
    }

    @Override
    protected void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}

