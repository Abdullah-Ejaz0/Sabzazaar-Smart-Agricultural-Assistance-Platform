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

public class SplashActivity extends AppCompatActivity {
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

        new Handler().postDelayed(() -> {
            navigate();
        }, 1000);
    }

    private void navigate() {
        if (!sPref.getBoolean("loggedIn", false)){
            startActivity(new Intent(SplashActivity.this, OnBoardingStartActivity.class));
        }else{
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
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