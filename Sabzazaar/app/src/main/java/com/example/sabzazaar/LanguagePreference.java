package com.example.sabzazaar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class LanguagePreference extends AppCompatActivity {

    ImageButton backBtn;
    LinearLayout langEn, langUr;
    MaterialButton langNext;

    boolean isLanguageSelected = false;
    String selectedLang = null;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_preference);

        init();
        getIntentData();
        applyPreviousSelection();
        setupListeners();
        updateNextButton();
    }

    private void init() {
        backBtn = findViewById(R.id.backBtn);
        langEn = findViewById(R.id.langEn);
        langUr = findViewById(R.id.langUr);
        langNext = findViewById(R.id.langNext);

        langNext.setEnabled(false);
        langNext.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
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
            Intent i = new Intent(this, mobile_number_get.class);
            startActivity(i);
            finish();
        });

        langEn.setOnClickListener(v -> selectLanguage("en"));
        langUr.setOnClickListener(v -> selectLanguage("ur"));

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
}