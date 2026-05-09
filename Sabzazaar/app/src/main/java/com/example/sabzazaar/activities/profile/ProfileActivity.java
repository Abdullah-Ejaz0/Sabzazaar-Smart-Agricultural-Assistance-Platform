package com.example.sabzazaar.activities.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sabzazaar.R;
import com.example.sabzazaar.utils.TTSManager;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    private TTSManager ttsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ttsManager = new TTSManager(this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayUserData();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class)));
        }

        ImageButton btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setOnClickListener(v -> speakProfileSummary());
        }

        updateListenButtonUI();
        displayUserData();
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

    private void speakProfileSummary() {
        TextView tvName = findViewById(R.id.tvProfileName);
        TextView tvPhone = findViewById(R.id.tvProfilePhone);
        TextView tvLocation = findViewById(R.id.tvProfileLocation);

        StringBuilder sb = new StringBuilder();
        sb.append("Profile. ");
        if (tvName != null) sb.append("Name: ").append(tvName.getText()).append(". ");
        if (tvPhone != null) sb.append("Phone: ").append(tvPhone.getText()).append(". ");
        if (tvLocation != null) sb.append("Location: ").append(tvLocation.getText()).append(". ");

        ttsManager.speak(sb.toString());
    }

    private void displayUserData() {
        SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
        TextView tvName = findViewById(R.id.tvProfileName);
        TextView tvPhone = findViewById(R.id.tvProfilePhone);
        TextView tvLang = findViewById(R.id.tvProfileLang);
        TextView tvLocation = findViewById(R.id.tvProfileLocation);

        String name = sPref.getString("full_name", "");
        String phone = sPref.getString("phone_number", "");
        String lang = sPref.getString("preferred_language", "en");
        String location = sPref.getString("location", "");

        String tapName = "Tap Edit Profile to add name";
        String tapLoc = "Tap Edit Profile to add location";
        String notProvided = "Not provided";
        String displayLang = "English";

        if (lang.equals("ur")) {
             tapName = "نام شامل کرنے کے لیے پروفائل میں ترمیم کریں کو تھپتھپائیں";
             tapLoc = "مقام شامل کرنے کے لیے پروفائل میں ترمیم کریں کو تھپتھپائیں";
             notProvided = "فراہم نہیں کیا گیا";
             displayLang = "اردو";
        } else if (lang.equals("pa")) {
             tapName = "نام شامل کرن لئی ایڈٹ پروفائل نوں دباؤ";
             tapLoc = "مقام شامل کرن لئی ایڈٹ پروفائل نوں دباؤ";
             notProvided = "نہیں دتا گیا";
             displayLang = "پنجابی";
        }

        if (tvName != null) {
            tvName.setText(name.isEmpty() ? tapName : name);
        }
        if (tvPhone != null) {
            tvPhone.setText(phone.isEmpty() ? notProvided : phone);
        }
        if (tvLang != null) {
            tvLang.setText(displayLang);
        }
        if (tvLocation != null) {
            tvLocation.setText(location.isEmpty() ? tapLoc : location);
        }
    }

    @Override
    protected void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}
