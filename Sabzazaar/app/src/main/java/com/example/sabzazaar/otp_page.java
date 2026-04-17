package com.example.sabzazaar;

import android.Manifest;
import android.content.Intent;
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

import com.google.android.material.button.MaterialButton;

import java.security.Permissions;
import java.util.Random;

public class otp_page extends AppCompatActivity {

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
    }

    private void setupListeners() {

        backBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, mobile_number_get.class);
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

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (isError) resetUI();

                    if (s.length() == 1 && index < 5) {
                        boxes[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void resetUI() {

        isError = false;

        setNormal(otp1);
        setNormal(otp2);
        setNormal(otp3);
        setNormal(otp4);
        setNormal(otp5);
        setNormal(otp6);

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

        setError(otp1);
        setError(otp2);
        setError(otp3);
        setError(otp4);
        setError(otp5);
        setError(otp6);

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

            startActivity(new Intent(this, Permissions.class));
            finish();

        } else {
            showError();
        }
    }

    private void generateAndSendOtp() {

        correctOtp = generateOtp();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE
            );

        } else {
            sendSms();
        }
    }

    private void sendSms() {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null,
                    "Your OTP is: " + correctOtp,
                    null, null);

            Toast.makeText(this, "OTP sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateOtp() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSms();
            }
        }
    }
}