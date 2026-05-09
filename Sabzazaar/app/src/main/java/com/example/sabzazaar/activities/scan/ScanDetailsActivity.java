package com.example.sabzazaar.activities.scan;

import com.example.sabzazaar.R;
import com.example.sabzazaar.models.ScanResponse;
import com.example.sabzazaar.adapters.ScanDetailAdapter;
import com.example.sabzazaar.utils.TTSManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ScanDetailsActivity extends AppCompatActivity {
    private TTSManager ttsManager;
    private ScanResponse scanResult;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_details);

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

        findViewById(R.id.btnBackBottom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        View.OnClickListener ttsListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakDetails();
            }
        };

        findViewById(R.id.btnListen).setOnClickListener(ttsListener);
        findViewById(R.id.btnVoiceBig).setOnClickListener(ttsListener);

        updateListenButtonUI();
    }

    private void updateUI() {
        RecyclerView rvDetails = findViewById(R.id.rvScanDetails);
        rvDetails.setLayoutManager(new LinearLayoutManager(this));
        
        List<ScanDetailAdapter.DetailItem> items = new ArrayList<>();
        
        if (scanResult.llmReport != null && !scanResult.llmReport.isEmpty()) {
            // Simple parsing of LLM report into sections
            String[] sections = scanResult.llmReport.split("\n\n");
            for (String section : sections) {
                if (section.trim().isEmpty()) continue;
                
                String title = "Advisory";
                String body = section;
                
                // Try to extract title if it starts with a number or heading
                if (section.contains("\n")) {
                    int firstLineEnd = section.indexOf("\n");
                    title = section.substring(0, firstLineEnd).replace("#", "").replace("*", "").trim();
                    body = section.substring(firstLineEnd).trim();
                } else if (section.startsWith("1)") || section.startsWith("2)") || section.startsWith("3)") || section.startsWith("4)")) {
                    int colonIdx = section.indexOf(")");
                    if (colonIdx != -1) {
                        title = "Section " + section.substring(0, colonIdx + 1);
                        body = section.substring(colonIdx + 1).trim();
                    }
                }
                items.add(new ScanDetailAdapter.DetailItem(title, body));
            }
        } else if (scanResult.recommendations != null) {
            items.add(new ScanDetailAdapter.DetailItem("Pathogen", scanResult.recommendations.pathogen));
            if (scanResult.recommendations.fertilizer != null) {
                items.add(new ScanDetailAdapter.DetailItem("Fertilizer Advice", scanResult.recommendations.fertilizer.recommendation));
            }
            if (scanResult.recommendations.pesticide != null) {
                items.add(new ScanDetailAdapter.DetailItem("Pesticide Advice", scanResult.recommendations.pesticide.recommendation));
            }
        }
        
        if (items.isEmpty()) {
            items.add(new ScanDetailAdapter.DetailItem("Information", "No additional details available at this time."));
        }
        
        rvDetails.setAdapter(new ScanDetailAdapter(items));
    }

    private void updateListenButtonUI() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        android.widget.ImageButton btnListen = findViewById(R.id.btnListen);
        android.widget.ImageButton btnVoiceBig = findViewById(R.id.btnVoiceBig);

        if (btnListen != null) {
            btnListen.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            btnListen.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
        if (btnVoiceBig != null) {
            btnVoiceBig.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            btnVoiceBig.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    private void speakDetails() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");

        String text = "Disease details. Here you can see the causes, symptoms, and prevention for the detected disease.";
        if (lang.equals("ur")) {
            text = "بیماری کی تفصیلات۔ یہاں آپ تشخیص شدہ بیماری کی وجوہات، علامات اور روک تھام دیکھ سکتے ہیں۔";
        } else if (lang.equals("pa")) {
            text = "بیماری دیاں تفصیلاں۔ ایتھے تسیں لبھی گئی بیماری دیاں وجہاں، نشانیاں تے بچاؤ ویکھ سکدے او۔";
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

