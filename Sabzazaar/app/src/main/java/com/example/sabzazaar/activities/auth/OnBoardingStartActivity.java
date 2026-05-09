package com.example.sabzazaar.activities.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sabzazaar.R;
import com.example.sabzazaar.activities.expert.ExpertLoginActivity;
import com.google.android.material.button.MaterialButton;

public class OnBoardingStartActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    ConstraintLayout expertLoginContainer;
    MaterialButton farmerLoginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_boarding_start);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        expertLoginContainer.setOnClickListener((v)->{
            expertLoginContainer.setSelected(true);
            startActivity(new Intent(OnBoardingStartActivity.this, ExpertLoginActivity.class));
        });
        farmerLoginBtn.setOnClickListener((v) -> {
            startActivity(new Intent(OnBoardingStartActivity.this, MobileNumberGetActivity.class));
        });
    }

    private void init(){
        expertLoginContainer = findViewById(R.id.expert_Container);
        farmerLoginBtn = findViewById(R.id.btnStart);
    }
}
