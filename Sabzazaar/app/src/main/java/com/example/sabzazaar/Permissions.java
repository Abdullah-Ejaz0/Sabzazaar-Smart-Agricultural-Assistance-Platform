package com.example.sabzazaar;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class Permissions extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private String flowType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_permisssions);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        flowType = getIntent().getStringExtra("flow_type");

        MaterialButton btnLater = findViewById(R.id.btnLater);
        MaterialButton btnAllow = findViewById(R.id.btnAllow);
        ImageView backBtn = findViewById(R.id.backBtn);
        ImageView speakerBtn = findViewById(R.id.speakerBtn);

        // LATER → skip permissions → go home
        btnLater.setOnClickListener(v -> goToHome());

        // ALLOW → request runtime permissions
        btnAllow.setOnClickListener(v -> requestPermissions());

        // BACK → dynamic navigation
        backBtn.setOnClickListener(v -> handleBack());

        // speaker (optional demo)
        speakerBtn.setOnClickListener(v ->
                Toast.makeText(this, "Voice demo", Toast.LENGTH_SHORT).show()
        );
    }

    private void requestPermissions() {
        String[] permissions = new String[]{
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        };

        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {

            boolean allGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                goToHome();
            } else {
                Toast.makeText(this, "Permissions required for full features", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void goToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleBack() {
        if ("login".equals(flowType)) {
            startActivity(new Intent(this, otp_page.class));
        } else {
            startActivity(new Intent(this, LanguagePreference.class));
        }
        finish();
    }
}