package com.example.sabzazaar.activities.scan;

import com.example.sabzazaar.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ScanAnalysisActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvSubtext;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_analysis);

        progressBar = findViewById(R.id.pbAnalysis);
        tvSubtext = findViewById(R.id.tvAnalysisSubtext);

        // Start simulated analysis
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 10;
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                            if (progressStatus == 30) tvSubtext.setText(R.string.checking_leaf_color);
                            if (progressStatus == 60) tvSubtext.setText(R.string.comparing_diseases);
                            if (progressStatus == 90) tvSubtext.setText(R.string.almost_done);
                        }
                    });
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // When done, go to results
                startActivity(new Intent(ScanAnalysisActivity.this, ScanResultsActivity.class));
                finish();
            }
        }).start();
    }
}
