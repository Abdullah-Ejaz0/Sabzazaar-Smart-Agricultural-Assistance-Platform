package com.example.sabzazaar.activities.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sabzazaar.R;
import com.example.sabzazaar.activities.main.MainActivity;
import com.example.sabzazaar.utils.TTSManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class MobileNumberGetActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }


    TextInputLayout phoneLayout;
    TextInputEditText phoneInput;

    CardView sendOtpBtn, guestBtn;
    ImageView backBtn;
    ImageButton speakerBtn;

    String pendingNumber;
    String pendingOtp;

    private TTSManager ttsManager;

    private static final int SMS_PERMISSION_CODE = 101;
    private static final String SUPABASE_URL = "https://evlnoytrwyjpnpiagjnn.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImV2bG5veXRyd3lqcG5waWFnam5uIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY0MzU2MTAsImV4cCI6MjA5MjAxMTYxMH0.oXx_g-1k2WDrusUZMfpesFrmLjHW5BG7rUrKgUQl-rs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_number_get);

        init();
        setupListeners();
        updateListenButtonUI();
    }

    private void init() {
        phoneLayout = findViewById(R.id.phoneLayout);
        phoneInput = findViewById(R.id.phoneInput);
        sendOtpBtn = findViewById(R.id.sendOtpBtn);
        guestBtn = findViewById(R.id.guestBtn);
        backBtn = findViewById(R.id.backBtn);
        speakerBtn = findViewById(R.id.speakerBtn);
        ttsManager = new TTSManager(this);

        String existingNumber = getIntent().getStringExtra("phone");
        if (existingNumber != null) {
            phoneInput.setText(existingNumber);
        }
    }

    private void updateListenButtonUI() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        if (speakerBtn != null) {
            speakerBtn.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            speakerBtn.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    private void setupListeners() {
        sendOtpBtn.setOnClickListener(v -> validateAndSendOtp());

        speakerBtn.setOnClickListener(v -> speakInstructions());

        guestBtn.setOnClickListener(v -> {
            guestBtn.setSelected(true);
            getSharedPreferences("user", MODE_PRIVATE).edit()
                    .putBoolean("is_guest", true)
                    .putBoolean("loggedIn", true)
                    .putString("access_token", "")
                    .apply();
            Intent intent = new Intent(MobileNumberGetActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MobileNumberGetActivity.this, OnBoardingStartActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void validateAndSendOtp() {
        String number = phoneInput.getText() != null
                ? phoneInput.getText().toString().trim()
                : "";

        phoneLayout.setError(null);
        phoneLayout.setBoxStrokeColor(Color.parseColor("#c8e0c8"));

        if (!isValidPakistaniNumber(number)) {
            showError("Enter valid number (03XXXXXXXXX)");
            return;
        }

        // Convert to international format before sending to Supabase
        String internationalPhone = "+92" + number.substring(1);

        sendSupabaseOtp(number, internationalPhone);
    }

    private void sendSupabaseOtp(String localNumber, String internationalPhone) {
        String jsonBody = "{\"phone\":\"" + internationalPhone + "\"}";

        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
        );

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/otp")
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        new OkHttpClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> showError("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response)
                    throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        // Navigate to OTP screen, pass the LOCAL format for display
                        Intent intent = new Intent(MobileNumberGetActivity.this, OtpPageActivity.class);
                        intent.putExtra("phone", localNumber);
                        startActivity(intent);
                    });
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    runOnUiThread(() -> showError("Failed to send OTP: " + errorBody));
                }
            }
        });
    }

    private boolean isValidPakistaniNumber(String number) {
        return number.matches("03[0-9]{9}");
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private void showError(String message) {
        phoneLayout.setError(message);
        phoneLayout.setBoxStrokeColor(Color.RED);
    }

    private void speakInstructions() {
        SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");
        String text = "Please enter your 11 digit mobile number starting with 03. Then tap Send OTP.";
        if ("ur".equals(lang)) {
            text = "براہ کرم اپنا گیارہ ہندسوں والا موبائل نمبر درج کریں جو صفر تین سے شروع ہوتا ہو۔ پھر او ٹی پی بھیجیں پر ٹیپ کریں۔";
        } else if ("pa".equals(lang)) {
            text = "مہربانی کر کے اپنا گیارہ ہندسیاں والا موبائل نمبر لکھو جیہڑا صفر تِن توں شروع ہوندا اے۔ فیر او ٹی پی بھیجو تے کلک کرو۔";
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
