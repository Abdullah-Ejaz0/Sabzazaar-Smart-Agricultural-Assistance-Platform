package com.example.sabzazaar.utils;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.sabzazaar.R;
import com.example.sabzazaar.activities.main.MainActivity;
import com.example.sabzazaar.models.CompleteSignupResponse;
import com.example.sabzazaar.models.SignupRequest;
import com.example.sabzazaar.models.UserData;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoicePreference extends AppCompatActivity {

    private ImageButton backBtn;
    private LinearLayout voiceYes, voiceNo;
    private MaterialButton voiceNext;
    private ImageView yesIcon, noIcon;

    private boolean isSelected = false;
    private boolean voiceEnabled = false;

    private String phone;
    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_preference);

        init();
        getIntentData();
        applyPreviousSelection();
        setupListeners();
    }

    private void init() {
        backBtn = findViewById(R.id.backBtn);
        voiceYes = findViewById(R.id.voiceYes);
        voiceNo = findViewById(R.id.voiceNo);
        voiceNext = findViewById(R.id.voiceNext);

        yesIcon = findViewById(R.id.yesIcon);
        noIcon = findViewById(R.id.noIcon);

        setButtonDisabled();
    }

    private void getIntentData() {
        phone = getIntent().getStringExtra("phone");
        language = getIntent().getStringExtra("language");
        voiceEnabled = getIntent().getBooleanExtra("voice", false);
    }

    private void applyPreviousSelection() {
        if (getIntent().hasExtra("voice")) {
            if (voiceEnabled) {
                selectYes();
            } else {
                selectNo();
            }
        }
    }

    private void setupListeners() {

        backBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, LanguagePreference.class);
            i.putExtra("phone", phone);
            i.putExtra("language", language);
            startActivity(i);
            finish();
        });

        voiceYes.setOnClickListener(v -> selectYes());
        voiceNo.setOnClickListener(v -> selectNo());

        voiceNext.setOnClickListener(v -> {
            if (!isSelected) return;
            completeSignup();
        });
    }

    private void selectYes() {
        isSelected = true;
        voiceEnabled = true;

        voiceYes.setSelected(true);
        voiceNo.setSelected(false);

        yesIcon.setSelected(true);
        noIcon.setSelected(false);

        setButtonEnabled();
    }

    private void selectNo() {
        isSelected = true;
        voiceEnabled = false;

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

    private void completeSignup() {

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        SignupRequest request = new SignupRequest(phone, language, voiceEnabled);

        Call<CompleteSignupResponse> call = apiService.completeSignup(request);

        call.enqueue(new Callback<CompleteSignupResponse>() {
            @Override
            public void onResponse(Call<CompleteSignupResponse> call, Response<CompleteSignupResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    CompleteSignupResponse apiResponse = response.body();

                    if (apiResponse.status == 1 && apiResponse.user != null) {

                        saveUserPreferences(apiResponse.user);

                        if (!allPermissionsGranted()) {
                            startActivity(new Intent(VoicePreference.this, Permissions.class));
                        } else {
                            startActivity(new Intent(VoicePreference.this, MainActivity.class));
                        }

                        finish();

                    } else {
                        Toast.makeText(VoicePreference.this, "Signup failed", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(VoicePreference.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CompleteSignupResponse> call, Throwable t) {
                Toast.makeText(VoicePreference.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void saveUserPreferences(UserData user) {

        SharedPreferences sharedPref = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("phone_number", user.phone_number);
        editor.putString("preferred_language", user.preferred_language != null ? user.preferred_language : "");
        editor.putBoolean("voice_assistant_enabled", user.voice_assistant_enabled);
        editor.putString("full_name", user.full_name != null ? user.full_name : "");
        editor.putString("location", user.location != null ? user.location : "");
        editor.putString("profile_photo", user.profile_photo != null ? user.profile_photo : "");

        editor.apply();
    }
}