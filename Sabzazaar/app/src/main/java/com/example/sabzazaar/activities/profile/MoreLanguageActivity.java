package com.example.sabzazaar.activities.profile;

import com.example.sabzazaar.R;

import com.example.sabzazaar.utils.TTSManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MoreLanguageActivity extends AppCompatActivity {
    private TTSManager ttsManager;
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    private String selectedLanguage = "en";
    private ImageView ivCheckEnglish, ivCheckUrdu, ivCheckPunjabi;

    private LinearLayout llEnglish, llUrdu, llPunjabi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_language);

        ttsManager = new TTSManager(this);

        ivCheckEnglish = findViewById(R.id.ivCheckEnglish);
        ivCheckUrdu = findViewById(R.id.ivCheckUrdu);
        ivCheckPunjabi = findViewById(R.id.ivCheckPunjabi);

        llEnglish = findViewById(R.id.llEnglish);
        llUrdu = findViewById(R.id.llUrdu);
        llPunjabi = findViewById(R.id.llPunjabi);
        Button btnSave = findViewById(R.id.btnSave);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        llEnglish.setOnClickListener(v -> selectLanguage("en"));
        llUrdu.setOnClickListener(v -> selectLanguage("ur"));
        llPunjabi.setOnClickListener(v -> selectLanguage("pa"));

        findViewById(R.id.btnListen).setOnClickListener(v -> speakLanguageGuide());

        // Initialize with currently selected language
        String currentLang = getSharedPreferences("user", MODE_PRIVATE).getString("preferred_language", "en");
        selectLanguage(currentLang);

        btnSave.setOnClickListener(v -> saveLanguage());

        updateListenButtonUI();
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

    private void speakLanguageGuide() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");

        String text = "Language Settings. Please select your preferred language: English, Urdu, or Punjabi. Tap Save to apply changes.";
        if (lang.equals("ur")) {
            text = "زبان کی ترتیبات۔ براہ کرم اپنی پسندیدہ زبان منتخب کریں: انگریزی، اردو، یا پنجابی۔ تبدیلیوں کو لاگو کرنے کے لیے محفوظ کریں پر ٹیپ کریں۔";
        } else if (lang.equals("pa")) {
            text = "بولی دیاں ترتیبات۔ مہربانی کر کے اپنی پسند دی بولی چنو: انگریزی، اردو، یا پنجابی۔ تبدیلیاں لاگو کرن لئی محفوظ کرو تے ٹیپ کرو۔";
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

    private void saveLanguage() {
        android.widget.Button btnSave = findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        com.example.sabzazaar.network.ApiService apiService = 
                com.example.sabzazaar.network.RetrofitClient.getClient(this).create(com.example.sabzazaar.network.ApiService.class);
        
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("preferred_language", selectedLanguage);

        apiService.updateProfile(body).enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    getSharedPreferences("user", MODE_PRIVATE).edit()
                            .putString("preferred_language", selectedLanguage)
                            .commit(); // Synchronous save to prevent race condition
                    
                    com.example.sabzazaar.utils.LocaleHelper.setLocale(MoreLanguageActivity.this, selectedLanguage);

                    Toast.makeText(MoreLanguageActivity.this, "Language updated", Toast.LENGTH_SHORT).show();
                    
                    android.content.Intent intent = new android.content.Intent(MoreLanguageActivity.this, com.example.sabzazaar.activities.main.MainActivity.class);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                    Toast.makeText(MoreLanguageActivity.this, "Failed to update language", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText("Save");
                Toast.makeText(MoreLanguageActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectLanguage(String lang) {
        selectedLanguage = lang;
        ivCheckEnglish.setVisibility(lang.equals("en") ? View.VISIBLE : View.GONE);
        ivCheckUrdu.setVisibility(lang.equals("ur") ? View.VISIBLE : View.GONE);
        ivCheckPunjabi.setVisibility(lang.equals("pa") ? View.VISIBLE : View.GONE);

        llEnglish.setSelected(lang.equals("en"));
        llUrdu.setSelected(lang.equals("ur"));
        llPunjabi.setSelected(lang.equals("pa"));
    }
}
