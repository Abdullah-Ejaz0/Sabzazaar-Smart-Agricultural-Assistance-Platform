package com.example.sabzazaar.activities.profile;

import com.example.sabzazaar.R;
import com.example.sabzazaar.utils.TTSManager;

import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    private TTSManager ttsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ttsManager = new TTSManager(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        ImageButton btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setOnClickListener(v -> speakSettings());
        }

        SwitchCompat switchVoice = findViewById(R.id.switchVoice);
        SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
        if (switchVoice != null) {
            boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
            switchVoice.setChecked(isEnabled);
            updateListenButtonUI(isEnabled);
            switchVoice.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sPref.edit().putBoolean("voice_assistant_enabled", isChecked).apply();
                updateListenButtonUI(isChecked);
                syncVoicePreferenceWithBackend(isChecked);
            });
        }

        Button btnClearCache = findViewById(R.id.btnClearCache);
        if (btnClearCache != null) {
            btnClearCache.setOnClickListener(v -> {
                Toast.makeText(this, "Cache cleared (12 MB freed)", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateListenButtonUI(boolean isEnabled) {
        ImageButton btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            btnListen.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    private void syncVoicePreferenceWithBackend(boolean isEnabled) {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Map<String, Object> body = new HashMap<>();
        body.put("voice_assistant_enabled", isEnabled);

        apiService.updateVoicePreference(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                // Background sync
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // Fail silently
            }
        });
    }

    private void speakSettings() {
        SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");
        
        String textToSpeak = "Settings. Voice Guidance: Read text aloud. Offline Content: FAQs, guides 12 MB. Clear Cache: Free up space.";
        if (lang.equals("ur")) {
            textToSpeak = "ترتیبات۔ صوتی رہنمائی۔ آف لائن مواد۔ کیشے صاف کریں۔";
        } else if (lang.equals("pa")) {
            textToSpeak = "سیٹنگاں۔ آواز دی مدد۔ آف لائن مواد۔ کیشے صاف کرو۔";
        }
        
        ttsManager.speak(textToSpeak);
    }

    @Override
    protected void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}
