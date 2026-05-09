package com.example.sabzazaar.activities.main;

import com.example.sabzazaar.R;
import com.example.sabzazaar.utils.TTSManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    private TTSManager ttsManager;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ttsManager = new TTSManager(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        ImageButton btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setOnClickListener(v -> speakAboutInfo());
        }

        updateListenButtonUI();
    }

    private void updateListenButtonUI() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        ImageButton btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            btnListen.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    private void speakAboutInfo() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");

        String text = "About Page. This app helps farmers detect diseases and get suggestions for better yield.";
        if (lang.equals("ur")) {
            text = "معلومات کا صفحہ۔ یہ ایپ کسانوں کو بیماریوں کی تشخیص اور بہتر پیداوار کے لیے تجاویز حاصل کرنے میں مدد دیتی ہے۔";
        } else if (lang.equals("pa")) {
            text = "معلومات دا صفحہ۔ اے ایپ کساناں نوں بیماریاں لبھن تے بہتر فصل لئی مشورے دین وچ مدد کردی اے۔";
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

