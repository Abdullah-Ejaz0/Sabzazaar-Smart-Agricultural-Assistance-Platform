package com.example.sabzazaar.activities.expert;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabzazaar.R;

public class ExpertAnswerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert_answer);

        String name = getIntent().getStringExtra("farmer_name");
        String question = getIntent().getStringExtra("question_text");

        TextView tvFarmerName = findViewById(R.id.tvFarmerName);
        TextView tvQuestion = findViewById(R.id.tvQuestion);
        tvFarmerName.setText(name);
        tvQuestion.setText(question);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            Toast.makeText(this, "Answer submitted successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}