package com.example.sabzazaar.activities.expert;

import com.example.sabzazaar.R;
import com.example.sabzazaar.activities.auth.OnBoardingStartActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ExpertLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert_login);

        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        findViewById(R.id.btnBack).setOnClickListener((v) -> {
                startActivity(new Intent(this, OnBoardingStartActivity.class));
                finish();
        });

        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString();
            String pass = etPassword.getText().toString();

            if (!user.isEmpty() && !pass.isEmpty()) {
                Toast.makeText(this, "Logged in as expert", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, ExpertDashboardActivity.class));
            } else {
                Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }
}