package com.example.sabzazaar.activities.auth;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Request;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.Random;

public class OtpPageActivity extends AppCompatActivity {

    // -----------------------------------------------------------------------
    // IMPORTANT: Replace these with your actual Supabase project values
    // from Supabase Dashboard → Project Settings → API
    // -----------------------------------------------------------------------
    private static final String SUPABASE_URL = "https://your-project-id.supabase.co";
    private static final String SUPABASE_ANON_KEY = "your-anon-key";
    // -----------------------------------------------------------------------

    ImageButton backBtn, listenBtn;
    TextView subtitle, resend, errorText;
    MaterialButton verifyBtn;

    EditText otp1, otp2, otp3, otp4, otp5, otp6;

    String phone;
    String correctOtp;

    boolean isError = false;

    private static final int SMS_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_page);

        init();
        getIntentData();
        setupListeners();
        setupOtp();
        resetUI();
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
    }

    private void getIntentData() {
        phone = getIntent().getStringExtra("phone");
        correctOtp = getIntent().getStringExtra("otp");

        if (phone != null) {
            subtitle.setText("Code sent to " + phone);
        }

        // TEMPORARY - remove before production
        if (correctOtp != null) {
            Toast.makeText(this, "OTP for testing: " + correctOtp, Toast.LENGTH_LONG).show();
        }
    }

    private void setupListeners() {
        backBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, MobileNumberGetActivity.class);
            i.putExtra("phone", phone);
            startActivity(i);
            finish();
        });

        resend.setOnClickListener(v -> generateAndSendOtp());
        verifyBtn.setOnClickListener(v -> verifyOtp());
        listenBtn.setOnClickListener(v -> {});
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
        String enteredOtp =
                otp1.getText().toString().trim() +
                        otp2.getText().toString().trim() +
                        otp3.getText().toString().trim() +
                        otp4.getText().toString().trim() +
                        otp5.getText().toString().trim() +
                        otp6.getText().toString().trim();

        if (enteredOtp.equals(correctOtp)) {
            // Step 1: Local OTP verified → get Supabase token
            getSupabaseToken();
        } else {
            showError();
        }
    }

    // -----------------------------------------------------------------------
    // Step 1: Get a Supabase token by signing in with phone number
    // Supabase uses phone as identifier and OTP as the password
    // -----------------------------------------------------------------------
    private void getSupabaseToken() {
        // Format phone to international format for Supabase
        // Pakistani numbers: 03XXXXXXXXX → +923XXXXXXXXX
        String internationalPhone = phone.replaceFirst("^0", "+92");

        OkHttpClient client = new OkHttpClient();

        // Build JSON body
        String jsonBody = "{\"phone\":\"" + internationalPhone + "\","
                + "\"password\":\"" + correctOtp + "\"}";

        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        okhttp3.Request okhttpRequest = new okhttp3.Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
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
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) // okhttp3.Response, not retrofit2.Response
                    throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    // Parse access_token from response
                    JsonObject json = new JsonParser().parse(responseBody).getAsJsonObject();
                    String accessToken = json.get("access_token").getAsString();

                    // Save token to SharedPreferences
                    getSharedPreferences("user", MODE_PRIVATE)
                            .edit()
                            .putString("access_token", accessToken)
                            .apply();

                    // Step 2: Now check phone with Django backend
                    runOnUiThread(() -> checkPhoneNumberWithBackend());

                } else {
                    // Supabase auth failed - user might not exist in Supabase Auth yet
                    // This happens for new users on first signup
                    runOnUiThread(() -> checkPhoneNumberWithBackend());
                }
            }
        });
    }

    // -----------------------------------------------------------------------
    // Step 2: Check phone with Django to determine login vs signup flow
    // -----------------------------------------------------------------------
    private void checkPhoneNumberWithBackend() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        PhoneRequest phoneRequest = new PhoneRequest(phone);

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

            editor.putString("phone_number", userData.phone_number);
            editor.putString("preferred_language", userData.preferred_language != null ? userData.preferred_language : "");
            editor.putBoolean("voice_assistant_enabled", userData.voice_assistant_enabled);
            editor.putString("full_name", userData.full_name != null ? userData.full_name : "");
            editor.putString("location", userData.location != null ? userData.location : "");
            editor.putString("profile_photo", userData.profile_photo != null ? userData.profile_photo : "");
            editor.apply();

            if (!allPermissionsGranted()) {
                startActivity(new Intent(OtpPageActivity.this, Permissions.class));
            } else {
                startActivity(new Intent(OtpPageActivity.this, MainActivity.class));
            }
            finish();
        }
    }

    private void handleSignupFlow() {
        Intent intent = new Intent(OtpPageActivity.this, LanguagePreference.class);
        intent.putExtra("phone", phone);
        startActivity(intent);
        finish();
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void generateAndSendOtp() {
        correctOtp = generateOtp();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            sendSms();
        }
    }

    private void sendSms() {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, "Your OTP is: " + correctOtp, null, null);
            Toast.makeText(this, "OTP sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSms();
            }
        }
    }
}