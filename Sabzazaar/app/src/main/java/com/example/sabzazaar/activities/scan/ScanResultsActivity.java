package com.example.sabzazaar.activities.scan;

import com.example.sabzazaar.R;
import com.example.sabzazaar.activities.main.ChatbotActivity;
import com.example.sabzazaar.utils.TTSManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sabzazaar.R;
import com.example.sabzazaar.activities.main.ChatbotActivity;
import com.example.sabzazaar.models.ScanResponse;
import com.example.sabzazaar.utils.TTSManager;

import java.util.Locale;

public class ScanResultsActivity extends AppCompatActivity {
    private TTSManager ttsManager;
    private ScanResponse scanResult;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_results);

        scanResult = (ScanResponse) getIntent().getSerializableExtra("scan_result");
        String imageUriStr = getIntent().getStringExtra("image_uri");

        ttsManager = new TTSManager(this);

        if (scanResult != null) {
            updateUI();
        }

        if (imageUriStr != null) {
            ImageView ivPreview = findViewById(R.id.ivScanPreview);
            if (imageUriStr.startsWith("http")) {
                com.bumptech.glide.Glide.with(this)
                        .load(imageUriStr)
                        .into(ivPreview);
            } else {
                ivPreview.setImageURI(Uri.parse(imageUriStr));
            }
            ivPreview.setColorFilter(null);
        }

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btnSeeTreatment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanResultsActivity.this, ScanTreatmentActivity.class);
                intent.putExtra("scan_result", scanResult);
                startActivity(intent);
            }
        });

        findViewById(R.id.btnWhy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanResultsActivity.this, ScanDetailsActivity.class);
                intent.putExtra("scan_result", scanResult);
                startActivity(intent);
            }
        });

        findViewById(R.id.btnAskExpert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanResultsActivity.this, ChatbotActivity.class);
                intent.putExtra("scan_result", scanResult);
                startActivity(intent);
            }
        });

        findViewById(R.id.btnListen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakResult();
            }
        });

        updateListenButtonUI();
    }

    private void updateUI() {
        TextView tvDiseaseName = findViewById(R.id.tvDiseaseName);
        TextView tvConfidence = findViewById(R.id.tvConfidence);
        TextView tvRiskLevel = findViewById(R.id.tvRiskLevel);
        View riskIndicator = findViewById(R.id.llRiskIndicator);

        tvDiseaseName.setText(scanResult.diseaseName);
        tvConfidence.setText(String.format(Locale.getDefault(), "Confidence: %.1f%%", scanResult.confidence));
        tvRiskLevel.setText(scanResult.riskLevel.toUpperCase());

        if (scanResult.riskLevel.equalsIgnoreCase("high")) {
            riskIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFE7E7));
            tvRiskLevel.setTextColor(0xFFB71C1C);
            findViewById(R.id.btnSeeTreatment).setVisibility(View.VISIBLE);
        } else if (scanResult.riskLevel.equalsIgnoreCase("medium")) {
            riskIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFF4E7));
            tvRiskLevel.setTextColor(0xFFE67E22);
            findViewById(R.id.btnSeeTreatment).setVisibility(View.VISIBLE);
        } else {
            // Low risk or healthy
            riskIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE7FFE7));
            tvRiskLevel.setTextColor(0xFF1B5E20);
            
            if (scanResult.diseaseName.contains("Healthy")) {
                findViewById(R.id.btnSeeTreatment).setVisibility(View.GONE);
                tvConfidence.setText("The crop appears to be healthy.");
            } else {
                findViewById(R.id.btnSeeTreatment).setVisibility(View.VISIBLE);
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

    private void speakResult() {
        if (scanResult == null) return;
        
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");

        String text = "Scan Result. Your crop has " + scanResult.riskLevel + " risk of " + scanResult.diseaseName + ". Please check treatment.";
        if (lang.equals("ur")) {
            text = "اسکین کا نتیجہ۔ آپ کی فصل میں " + scanResult.diseaseName + " کا " + scanResult.riskLevel + " خطرہ ہے۔ براہ کرم علاج چیک کریں۔";
        } else if (lang.equals("pa")) {
            text = "سکین دا نتیجہ۔ تہاڈی فصل وچ " + scanResult.diseaseName + " دا " + scanResult.riskLevel + " خطرہ اے۔ مہربانی کر کے علاج ویکھو۔";
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

