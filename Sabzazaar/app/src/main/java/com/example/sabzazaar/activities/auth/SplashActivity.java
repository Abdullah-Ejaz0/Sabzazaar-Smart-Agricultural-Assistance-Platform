package com.example.sabzazaar.activities.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sabzazaar.R;
import com.example.sabzazaar.activities.main.MainActivity;
import com.example.sabzazaar.activities.expert.ExpertDashboardActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    ImageView logo;
    TextView text;
    SharedPreferences sPref;
    Animation logoAnim, textAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        applyAnimation();

        new Handler().postDelayed(this::navigate, 1500);
    }

    private void navigate() {
        boolean isExpert = sPref.getBoolean("is_expert", false);
        boolean isLoggedIn = sPref.getBoolean("loggedIn", false);
        String token = sPref.getString("access_token", null);

        if (isExpert && token != null) {
            startActivity(new Intent(SplashActivity.this, ExpertDashboardActivity.class));
            finish();
        } else if (isLoggedIn) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        } else {
            startActivity(new Intent(SplashActivity.this, OnBoardingStartActivity.class));
            finish();
        }
    }

    private void applyAnimation(){
        logo.setAnimation(logoAnim);
        text.setAnimation(textAnim);
    }

    private void init() {
        sPref = getSharedPreferences("user", MODE_PRIVATE);
        logo = findViewById(R.id.logo);
        text = findViewById(R.id.subtitle);

        logoAnim = AnimationUtils.loadAnimation(this, R.anim.logo_anim);
        textAnim = AnimationUtils.loadAnimation(this, R.anim.text_anim);
    }
}
