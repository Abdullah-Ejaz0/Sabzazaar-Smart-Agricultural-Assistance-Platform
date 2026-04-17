package com.example.sabzazaar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class VoicePreference extends AppCompatActivity {

    private LinearLayout voiceYes, voiceNo;
    private MaterialButton voiceNext;
    private ImageView yesIcon, noIcon;

    private boolean isSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_preference);

        voiceYes = findViewById(R.id.voiceYes);
        voiceNo = findViewById(R.id.voiceNo);
        voiceNext = findViewById(R.id.voiceNext);

        yesIcon = findViewById(R.id.yesIcon);
        noIcon = findViewById(R.id.noIcon);

        setButtonDisabled();

        voiceYes.setOnClickListener(v -> selectYes());
        voiceNo.setOnClickListener(v -> selectNo());

        voiceNext.setOnClickListener(v -> {
            if (!isSelected) return;
            startActivity(new Intent(this, mobile_number_get.class));
        });
    }

    private void selectYes() {

        isSelected = true;

        voiceYes.setSelected(true);
        voiceNo.setSelected(false);

        yesIcon.setSelected(true);
        noIcon.setSelected(false);

        setButtonEnabled();
    }

    private void selectNo() {

        isSelected = true;

        voiceYes.setSelected(false);
        voiceNo.setSelected(true);

        yesIcon.setSelected(false);
        noIcon.setSelected(true);

        setButtonEnabled();
    }

    private void setButtonDisabled() {
        voiceNext.setEnabled(false);
        voiceNext.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
    }

    private void setButtonEnabled() {
        voiceNext.setEnabled(true);
        voiceNext.setBackgroundTintList(getColorStateList(R.color.green));
    }
}