package com.example.sabzazaar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class LanguagePreference extends AppCompatActivity {

    LinearLayout langEn, langUr;
    MaterialButton langNext;

    boolean isSelected = false;
    String selectedLang = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_preference);

        init();
        setupListeners();
        updateNextButton();
    }

    private void init() {

        langEn = findViewById(R.id.langEn);
        langUr = findViewById(R.id.langUr);
        langNext = findViewById(R.id.langNext);

        langNext.setEnabled(false);
        langNext.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
    }

    private void setupListeners() {

        langEn.setOnClickListener(v -> selectLanguage("en"));
        langUr.setOnClickListener(v -> selectLanguage("ur"));

        langNext.setOnClickListener(v -> {
            if (isSelected) {
                startActivity(new Intent(this, VoicePreference.class));
            }
        });
    }

    private void selectLanguage(String lang) {

        selectedLang = lang;
        isSelected = true;

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

        if (isSelected) {

            langNext.setEnabled(true);
            langNext.setBackgroundTintList(getColorStateList(R.color.green));

        } else {

            langNext.setEnabled(false);
            langNext.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        }
    }
}