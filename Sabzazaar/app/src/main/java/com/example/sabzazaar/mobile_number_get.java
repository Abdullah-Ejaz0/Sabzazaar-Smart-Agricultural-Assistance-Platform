package com.example.sabzazaar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Random;

public class mobile_number_get extends AppCompatActivity {

    TextInputLayout phoneLayout;
    TextInputEditText phoneInput;

    CardView sendOtpBtn, guestBtn;
    ImageView backBtn;

    String pendingNumber;
    String pendingOtp;

    private static final int SMS_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_number_get);

        init();
        setupListeners();
    }

    private void init() {
        phoneLayout = findViewById(R.id.phoneLayout);
        phoneInput = findViewById(R.id.phoneInput);
        sendOtpBtn = findViewById(R.id.sendOtpBtn);
        guestBtn = findViewById(R.id.guestBtn);
        backBtn = findViewById(R.id.backBtn);

        String existingNumber = getIntent().getStringExtra("phone");
        if (existingNumber != null) {
            phoneInput.setText(existingNumber);
        }
    }

    private void setupListeners() {
        sendOtpBtn.setOnClickListener(v -> validateAndSendOtp());

        guestBtn.setOnClickListener(v -> {
            guestBtn.setSelected(true);
            Intent intent = new Intent(mobile_number_get.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(mobile_number_get.this, onBoarding_start.class);
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

        String otp = generateOtp();

        pendingNumber = number;
        pendingOtp = otp;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE
            );

        } else {
            sendSms(number, otp);
        }
    }

    private void sendSms(String number, String otp) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, "Your OTP is: " + otp, null, null);

        Intent intent = new Intent(this, otp_page.class);
        intent.putExtra("phone", number);
        intent.putExtra("otp", otp);
        startActivity(intent);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingNumber != null && pendingOtp != null) {
                    sendSms(pendingNumber, pendingOtp);
                }
            }
        }
    }
}