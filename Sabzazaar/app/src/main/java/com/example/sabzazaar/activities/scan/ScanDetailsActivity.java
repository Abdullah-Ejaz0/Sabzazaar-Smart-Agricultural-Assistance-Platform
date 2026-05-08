package com.example.sabzazaar.activities.scan;

import com.example.sabzazaar.R;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class ScanDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_details);

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
                // TODO: Implement TTS
            }
        };

        findViewById(R.id.btnListen).setOnClickListener(ttsListener);
        findViewById(R.id.btnVoiceBig).setOnClickListener(ttsListener);
    }
}
