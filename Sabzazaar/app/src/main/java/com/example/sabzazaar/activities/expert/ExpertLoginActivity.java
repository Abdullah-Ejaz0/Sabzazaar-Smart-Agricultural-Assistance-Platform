package com.example.sabzazaar.activities.expert;

import com.example.sabzazaar.R;
import com.example.sabzazaar.activities.auth.OnBoardingStartActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpertLoginActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }


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
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);
            btnLogin.setText("Logging in...");

            Map<String, String> credentials = new HashMap<>();
            credentials.put("email", email);
            credentials.put("password", password);

            ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
            apiService.expertLogin(credentials).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");

                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> body = response.body();
                        
                        // Save token and mark as expert using standard keys
                        android.content.SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
                        sPref.edit()
                            .putString("access_token", (String) body.get("access_token"))
                            .putString("refresh_token", (String) body.get("refresh_token"))
                            .putBoolean("loggedIn", true)
                            .putBoolean("is_expert", true)
                            .putBoolean("is_guest", false)
                            .apply();

                        Toast.makeText(ExpertLoginActivity.this, "Expert login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ExpertLoginActivity.this, ExpertDashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(ExpertLoginActivity.this, "Invalid credentials or not an expert", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    Toast.makeText(ExpertLoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
