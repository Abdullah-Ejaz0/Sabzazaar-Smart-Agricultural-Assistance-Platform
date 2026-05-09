package com.example.sabzazaar.activities.auth;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.sabzazaar.R;
import com.example.sabzazaar.activities.main.MainActivity;
import com.example.sabzazaar.models.CheckPhoneResponse;
import com.example.sabzazaar.models.PhoneRequest;
import com.example.sabzazaar.models.UserData;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import com.example.sabzazaar.utils.LanguagePreference;
import com.example.sabzazaar.utils.Permissions;
import com.example.sabzazaar.utils.TTSManager;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.io.IOException;

public class OtpPageActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }


    // -----------------------------------------------------------------------
    // IMPORTANT: Replace these with your actual Supabase project values
    // from Supabase Dashboard → Project Settings → API
    // -----------------------------------------------------------------------
    private static final String SUPABASE_URL = "https://evlnoytrwyjpnpiagjnn.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImV2bG5veXRyd3lqcG5waWFnam5uIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY0MzU2MTAsImV4cCI6MjA5MjAxMTYxMH0.oXx_g-1k2WDrusUZMfpesFrmLjHW5BG7rUrKgUQl-rs";
    // -----------------------------------------------------------------------

    ImageButton backBtn, listenBtn;
    TextView subtitle, resend, errorText;
    MaterialButton verifyBtn;

    EditText otp1, otp2, otp3, otp4, otp5, otp6;

    String phone;

    boolean isError = false;
    private TTSManager ttsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_page);

        init();
        getIntentData();
        setupListeners();
        setupOtp();
        resetUI();
        updateListenButtonUI();
    }

    private void init() {
        backBtn = findViewById(R.id.backBtn);
        listenBtn = findViewById(R.id.listenBtn);
        subtitle = findViewById(R.id.subtitle);
        resend = findViewById(R.id.resend);
        verifyBtn = findViewById(R.id.verifyBtn);
        errorText = findViewById(R.id.errorText);

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        ttsManager = new TTSManager(this);
    }

    private void updateListenButtonUI() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        if (listenBtn != null) {
            listenBtn.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            listenBtn.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    private void getIntentData() {
        phone = getIntent().getStringExtra("phone");

        if (phone != null) {
            subtitle.setText("Code sent to " + phone);
        }
    }

    private void setupListeners() {
        backBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, MobileNumberGetActivity.class);
            i.putExtra("phone", phone);
            startActivity(i);
            finish();
        });

        resend.setOnClickListener(v -> resendSupabaseOtp());
        verifyBtn.setOnClickListener(v -> verifyOtp());
        listenBtn.setOnClickListener(v -> speakInstructions());
    }

    private void speakInstructions() {
        SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");
        String text = "Please enter the 6 digit code sent to your mobile number. Then tap Verify.";
        if ("ur".equals(lang)) {
            text = "براہ کرم اپنے موبائل نمبر پر بھیجا گیا چھ ہندسوں کا کوڈ درج کریں۔ پھر تصدیق کریں پر ٹیپ کریں۔";
        } else if ("pa".equals(lang)) {
            text = "مہربانی کر کے اپنے موبائل نمبر تے بھیجیا گیا چھ ہندسیاں دا کوڈ لکھو۔ فیر ویریفائی تے کلک کرو۔";
        }
        ttsManager.speak(text);
    }

    private void setupOtp() {
        EditText[] boxes = {otp1, otp2, otp3, otp4, otp5, otp6};

        for (int i = 0; i < boxes.length; i++) {
            final int index = i;
            boxes[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isError) resetUI();
                    if (s.length() == 1 && index < 5) {
                        boxes[index + 1].requestFocus();
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void resetUI() {
        isError = false;
        setNormal(otp1); setNormal(otp2); setNormal(otp3);
        setNormal(otp4); setNormal(otp5); setNormal(otp6);
        errorText.setVisibility(TextView.GONE);
    }

    private void setNormal(EditText box) {
        box.setBackgroundResource(R.drawable.bg_otp_box);
        box.setTextColor(getResources().getColor(android.R.color.black));
    }

    private void setError(EditText box) {
        box.setBackgroundResource(R.drawable.bg_otp_box_error);
        box.setTextColor(getResources().getColor(android.R.color.black));
    }

    private void showError() {
        isError = true;
        setError(otp1); setError(otp2); setError(otp3);
        setError(otp4); setError(otp5); setError(otp6);
        errorText.setVisibility(TextView.VISIBLE);
        errorText.setText("Incorrect OTP. Please try again.");
    }

    private void verifyOtp() {
        String enteredOtp = getEnteredOtp();
        if (enteredOtp.length() < 6) {
            showError();
            errorText.setText("Please enter all 6 digits.");
            return;
        }
        // Delegate to Supabase — Supabase verifies the OTP and returns a session token
        getSupabaseToken();
    }

    // -----------------------------------------------------------------------
    // Step 1: Verify OTP with Supabase Phone Auth (grant_type=otp)
    // Then get a session token for use in all subsequent API calls
    // -----------------------------------------------------------------------
    private void getSupabaseToken() {
        // Convert local format to international: 03XXXXXXXXX → +923XXXXXXXXX
        String internationalPhone = phone.startsWith("0")
                ? "92" + phone.substring(1)
                : phone;

        OkHttpClient client = new OkHttpClient();

        // Supabase phone OTP verification body
        String jsonBody = "{\"phone\":\"" + internationalPhone + "\","
                + "\"token\":\"" + getEnteredOtp() + "\","
                + "\"type\":\"sms\"}";

        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
        );

        okhttp3.Request okhttpRequest = new okhttp3.Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/verify")
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        client.newCall(okhttpRequest).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(OtpPageActivity.this,
                                "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response)
                    throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    // Parse tokens from Supabase session
                    JsonObject json = new JsonParser().parse(responseBody).getAsJsonObject();
                    String accessToken = json.get("access_token").getAsString();
                    String refreshToken = json.get("refresh_token").getAsString();

                    // Save tokens — RetrofitClient will attach it as Bearer and refresh if needed
                    getSharedPreferences("user", MODE_PRIVATE)
                            .edit()
                            .putString("access_token", accessToken)
                            .putString("refresh_token", refreshToken)
                            .putString("phone_international", internationalPhone)
                            .apply();

                    // Step 2: Check phone with Django backend
                    runOnUiThread(() -> checkPhoneNumberWithBackend(internationalPhone));

                } else {
                    runOnUiThread(() -> {
                        showError();
                        errorText.setText("Otp Verification Failed");
                    });
                }
            }
        });
    }

    /** Returns the 6-digit OTP currently entered in the boxes */
    private String getEnteredOtp() {
        return otp1.getText().toString().trim()
                + otp2.getText().toString().trim()
                + otp3.getText().toString().trim()
                + otp4.getText().toString().trim()
                + otp5.getText().toString().trim()
                + otp6.getText().toString().trim();
    }

    // -----------------------------------------------------------------------
    // Step 2: Check phone with Django to determine login vs signup flow
    // The Supabase token is already in SharedPreferences and will be sent
    // automatically by RetrofitClient as a Bearer header.
    // check_phone does NOT require auth, but farmer_signup does.
    // -----------------------------------------------------------------------
    private void checkPhoneNumberWithBackend(String internationalPhone) {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        // Send international format — matches what Supabase stored
        PhoneRequest phoneRequest = new PhoneRequest(internationalPhone);

        apiService.checkPhoneNumber(phoneRequest).enqueue(new retrofit2.Callback<CheckPhoneResponse>() {
            @Override
            public void onResponse(retrofit2.Call<CheckPhoneResponse> call,
                                   retrofit2.Response<CheckPhoneResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CheckPhoneResponse apiResponse = response.body();

                    if (apiResponse.status == 1) {
                        handleLoginFlow(apiResponse);
                    } else {
                        handleSignupFlow();
                    }
                } else {
                    String errorMsg = "Error: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += "\n" + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(OtpPageActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<CheckPhoneResponse> call, Throwable t) {
                Toast.makeText(OtpPageActivity.this,
                        "Connection Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleLoginFlow(CheckPhoneResponse response) {
        UserData userData = response.user;
        if (userData != null) {
            SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();

            editor.putBoolean("loggedIn", true);
            editor.putBoolean("is_guest", false);
            editor.putString("phone_number", userData.phone_number);
            editor.putString("preferred_language", userData.preferred_language != null ? userData.preferred_language : "");
            editor.putBoolean("voice_assistant_enabled", userData.voice_assistant_enabled);
            editor.putString("full_name", userData.full_name != null ? userData.full_name : "");
            editor.putString("location", userData.location != null ? userData.location : "");
            editor.putString("profile_photo", userData.profile_photo != null ? userData.profile_photo : "");
            editor.apply();

            startActivity(new Intent(OtpPageActivity.this, MainActivity.class));
            finish();
        }
    }

    private void handleSignupFlow() {
        // Pass the international phone — Supabase stored it in E.164 format
        String intlPhone = getSharedPreferences("user", MODE_PRIVATE)
                .getString("phone_international", phone);
        Intent intent = new Intent(OtpPageActivity.this, LanguagePreference.class);
        intent.putExtra("phone", intlPhone);
        startActivity(intent);
        finish();
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Re-sends the OTP via Supabase by calling /auth/v1/otp.
     * Supabase will fire another SMS to the same phone number.
     */
    private void resendSupabaseOtp() {
        String internationalPhone = phone.startsWith("0")
                ? "92" + phone.substring(1)
                : phone;

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
                runOnUiThread(() -> Toast.makeText(OtpPageActivity.this,
                        "Failed to resend OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response)
                    throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(OtpPageActivity.this,
                                "OTP resent to " + phone, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OtpPageActivity.this,
                                "Could not resend OTP (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}
