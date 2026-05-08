package com.example.sabzazaar.activities.scan;

import com.example.sabzazaar.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class ScanPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_preview);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btnRetake).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to camera
                finish();
            }
        });

        findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Proceed to analysis
                startActivity(new Intent(ScanPreviewActivity.this, ScanAnalysisActivity.class));
                finish();
            }
        });
    }
}
