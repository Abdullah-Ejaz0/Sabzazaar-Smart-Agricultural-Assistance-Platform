package com.example.sabzazaar.activities.main;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabzazaar.R;
import com.example.sabzazaar.models.CommunityPost;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionDetailActivity extends AppCompatActivity {
    private TextView tvQuestionUser, tvQuestionTime, tvQuestionContent;
    private ImageView ivQuestionDetailImage;
    private EditText etReply;
    private ImageButton btnSend, btnBack;
    private CommunityPost post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        initViews();
        handleIntent();
        setupListeners();
    }

    private void initViews() {
        tvQuestionUser = findViewById(R.id.tvQuestionUser);
        tvQuestionTime = findViewById(R.id.tvQuestionTime);
        tvQuestionContent = findViewById(R.id.tvQuestionContent);
        ivQuestionDetailImage = findViewById(R.id.ivQuestionDetailImage);
        etReply = findViewById(R.id.etReply);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
    }

    private void handleIntent() {
        post = (CommunityPost) getIntent().getSerializableExtra("post_data");
        if (post != null) {
            tvQuestionUser.setText(post.getAuthorName());
            tvQuestionTime.setText(post.getCreatedAt());
            tvQuestionContent.setText(post.getBody());
            // In a real app, use Glide/Picasso to load post.getPhotoUrl() into ivQuestionDetailImage
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> submitReply());
    }

    private void submitReply() {
        String replyText = etReply.getText().toString().trim();
        if (replyText.isEmpty()) {
            Toast.makeText(this, "Please write a reply", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Map<String, Object> body = new HashMap<>();
        body.put("body", replyText);

        apiService.submitReply(post.getId(), body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuestionDetailActivity.this, "Reply submitted", Toast.LENGTH_SHORT).show();
                    etReply.setText("");
                    // Ideally, refresh the reply list here
                } else {
                    Toast.makeText(QuestionDetailActivity.this, "Failed to submit reply", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(QuestionDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}