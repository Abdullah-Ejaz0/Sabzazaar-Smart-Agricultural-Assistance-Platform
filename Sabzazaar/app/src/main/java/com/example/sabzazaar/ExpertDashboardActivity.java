package com.example.sabzazaar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ExpertDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert_dashboard);

        findViewById(R.id.btnSignOut).setOnClickListener(v -> {
            Toast.makeText(this, "Signed out from Expert Portal", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ExpertLogin.class));
            finish();
        });

        findViewById(R.id.llQuestion1).setOnClickListener(v -> openAnswer("Ali Raza", "How to treat yellow spots on wheat?"));
        findViewById(R.id.llQuestion2).setOnClickListener(v -> openAnswer("Sara Bibi", "Best fertilizer for rice now?"));

        findViewById(R.id.btnBroadcast).setOnClickListener(v -> {
            startActivity(new Intent(this, ExpertBroadcastActivity.class));
        });
    }

    private void openAnswer(String name, String question) {
        Intent intent = new Intent(this, ExpertAnswerActivity.class);
        intent.putExtra("farmer_name", name);
        intent.putExtra("question_text", question);
        startActivity(intent);
    }
}