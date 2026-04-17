package com.example.sabzazaar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class onBoarding_start extends AppCompatActivity {
    ConstraintLayout ExpertLogin;
    MaterialButton FarmerLogin;
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
        ExpertLogin.setOnClickListener((v)->{
            ExpertLogin.setSelected(true);

            startActivity(new Intent(onBoarding_start.this, ExpertLogin.class));
        });
        FarmerLogin.setOnClickListener((v) -> {
            startActivity(new Intent(onBoarding_start.this, mobile_number_get.class));
        });
    }

    private void init(){
        ExpertLogin = findViewById(R.id.expert_Container);
        FarmerLogin = findViewById(R.id.btnStart);
    }
}