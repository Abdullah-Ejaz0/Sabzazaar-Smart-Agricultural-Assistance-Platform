package com.example.sabzazaar.activities.expert;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabzazaar.R;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import com.example.sabzazaar.utils.TTSManager;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpertAnswerActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    private TTSManager ttsManager;
    private String postId;
    private EditText etAnswer;
    private android.widget.CheckBox cbVerified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert_answer);

        ttsManager = new TTSManager(this);

        postId = getIntent().getStringExtra("post_id");
        String name = getIntent().getStringExtra("farmer_name");
        String question = getIntent().getStringExtra("question_text");

        TextView tvFarmerName = findViewById(R.id.tvFarmerName);
        TextView tvQuestion = findViewById(R.id.tvQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        cbVerified = findViewById(R.id.cbVerified);
        
        tvFarmerName.setText(name);
        tvQuestion.setText(question);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        findViewById(R.id.btnListen).setOnClickListener(v -> {
            String text = "Question from " + tvFarmerName.getText().toString() + ". " + tvQuestion.getText().toString();
            ttsManager.speak(text);
        });

        updateListenButtonUI();

        findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            submitExpertAnswer();
        });
    }

    private void submitExpertAnswer() {
        String answerText = etAnswer.getText().toString().trim();
        if (answerText.isEmpty()) {
            Toast.makeText(this, "Please enter an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        findViewById(R.id.btnSubmit).setEnabled(false);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Map<String, Object> body = new HashMap<>();
        body.put("body", answerText);
        body.put("is_verified", cbVerified.isChecked()); // Expert chooses if this solves it

        apiService.submitReply(postId, body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ExpertAnswerActivity.this, "Answer submitted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    findViewById(R.id.btnSubmit).setEnabled(true);
                    Toast.makeText(ExpertAnswerActivity.this, "Failed to submit answer", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                findViewById(R.id.btnSubmit).setEnabled(true);
                Toast.makeText(ExpertAnswerActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateListenButtonUI() {
        android.content.SharedPreferences sPref = getSharedPreferences("user", android.content.Context.MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        android.widget.ImageButton btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            btnListen.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    @Override
    protected void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}
