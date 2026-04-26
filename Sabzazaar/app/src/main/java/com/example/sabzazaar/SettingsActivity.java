package com.example.sabzazaar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        LinearLayout btnSettingLanguage = findViewById(R.id.btnSettingLanguage);
        if (btnSettingLanguage != null) {
            btnSettingLanguage.setOnClickListener(v -> {
                startActivity(new Intent(SettingsActivity.this, LanguagePreference.class));
            });
        }
    }
}
