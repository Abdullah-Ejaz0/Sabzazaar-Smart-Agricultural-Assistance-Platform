package com.example.sabzazaar.activities.main;

import com.example.sabzazaar.R;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class ChatbotActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        View.OnClickListener demoListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // For demo, just show an alert or simple action
                android.widget.Toast.makeText(ChatbotActivity.this, "Demo: Question asked!", android.widget.Toast.LENGTH_SHORT).show();
            }
        };

        findViewById(R.id.btnHowToScan).setOnClickListener(demoListener);
        findViewById(R.id.btnDiseaseInfo).setOnClickListener(demoListener);
        findViewById(R.id.btnWeatherAdvice).setOnClickListener(demoListener);
        findViewById(R.id.btnPesticideAdvice).setOnClickListener(demoListener);

        findViewById(R.id.btnListen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement TTS
            }
        });
    }
}
