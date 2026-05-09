package com.example.sabzazaar.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sabzazaar.R;
import com.example.sabzazaar.activities.auth.MobileNumberGetActivity;
import com.example.sabzazaar.utils.TTSManager;
import com.google.android.material.button.MaterialButton;

public class LanguagePreference extends AppCompatActivity {

    ImageButton backBtn, speakerBtn;
    LinearLayout langEn, langUr;
    MaterialButton langNext;

    boolean isLanguageSelected = false;
    String selectedLang = null;
    String phone;
    private TTSManager ttsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_preference);

        init();
        getIntentData();
        applyPreviousSelection();
        setupListeners();
        updateNextButton();
        updateListenButtonUI();
    }

    private void init() {
        backBtn = findViewById(R.id.backBtn);
        speakerBtn = findViewById(R.id.speakerBtn);
        langEn = findViewById(R.id.langEn);
        langUr = findViewById(R.id.langUr);
        langNext = findViewById(R.id.langNext);
        ttsManager = new TTSManager(this);

        langNext.setEnabled(false);
        langNext.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
    }

    private void updateListenButtonUI() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        if (speakerBtn != null) {
            speakerBtn.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            speakerBtn.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    private void getIntentData() {
        phone = getIntent().getStringExtra("phone");
        selectedLang = getIntent().getStringExtra("language");
    }

    private void applyPreviousSelection() {
        if (selectedLang != null) {
            isLanguageSelected = true;
            if (selectedLang.equals("en")) {
                langEn.setSelected(true);
                langUr.setSelected(false);
            } else if (selectedLang.equals("ur")) {
                langUr.setSelected(true);
                langEn.setSelected(false);
            }
        }
    }

    private void setupListeners() {

        backBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, MobileNumberGetActivity.class);
            startActivity(i);
            finish();
        });

        langEn.setOnClickListener(v -> selectLanguage("en"));
        langUr.setOnClickListener(v -> selectLanguage("ur"));
        speakerBtn.setOnClickListener(v -> speakInstructions());

        langNext.setOnClickListener(v -> {
            if (isLanguageSelected) {
                Intent intent = new Intent(LanguagePreference.this, VoicePreference.class);
                intent.putExtra("phone", phone);
                intent.putExtra("language", selectedLang);
                startActivity(intent);
            }
        });
    }

    private void selectLanguage(String lang) {
        selectedLang = lang;
        isLanguageSelected = true;

        if (lang.equals("en")) {
            langEn.setSelected(true);
            langUr.setSelected(false);
        } else {
            langUr.setSelected(true);
            langEn.setSelected(false);
        }

        updateNextButton();
    }

    private void updateNextButton() {
        if (isLanguageSelected) {
            langNext.setEnabled(true);
            langNext.setBackgroundTintList(getColorStateList(R.color.green));
        } else {
            langNext.setEnabled(false);
            langNext.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        }
    }

    private void speakInstructions() {
        String text = "Please select your preferred language. English or Urdu. Then tap Next.";
        // We don't have a language selected yet, so default to English and then maybe Urdu translation
        ttsManager.speak(text + ". براہ کرم اپنی پسندیدہ زبان منتخب کریں۔ انگریزی یا اردو۔ پھر آگے پر ٹیپ کریں۔");
    }

    @Override
    protected void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}