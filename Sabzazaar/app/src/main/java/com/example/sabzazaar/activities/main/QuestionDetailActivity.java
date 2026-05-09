package com.example.sabzazaar.activities.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sabzazaar.R;
import com.example.sabzazaar.adapters.ReplyAdapter;
import com.example.sabzazaar.models.CommunityPost;
import com.example.sabzazaar.models.CommunityReply;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import com.example.sabzazaar.utils.TTSManager;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionDetailActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    private static final String TAG = "QuestionDetailActivity";

    private TextView tvQuestionUser, tvQuestionTime, tvQuestionContent;
    private TextView tvRepliesHeader, tvFarmerDiscussionsHeader;
    private ImageView ivQuestionDetailImage;
    private EditText etReply;
    private ImageButton btnSend, btnBack;
    private ProgressBar pbReplies;
    private RecyclerView rvVerifiedReplies, rvRegularReplies;

    private CommunityPost post;
    private ReplyAdapter verifiedAdapter, regularAdapter;
    private TTSManager ttsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        ttsManager = new TTSManager(this);

        initViews();
        handleIntent();
        setupRecyclerViews();
        setupListeners();
        updateListenButtonUI();
        
        if (post != null && post.getId() != null) {
            fetchReplies();
        } else {
            Toast.makeText(this, "Error loading post details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvQuestionUser = findViewById(R.id.tvQuestionUser);
        tvQuestionTime = findViewById(R.id.tvQuestionTime);
        tvQuestionContent = findViewById(R.id.tvQuestionContent);
        ivQuestionDetailImage = findViewById(R.id.ivQuestionDetailImage);
        etReply = findViewById(R.id.etReply);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);

        tvRepliesHeader = findViewById(R.id.tvRepliesHeader);
        tvFarmerDiscussionsHeader = findViewById(R.id.tvFarmerDiscussionsHeader);
        pbReplies = findViewById(R.id.pbReplies);
        rvVerifiedReplies = findViewById(R.id.rvVerifiedReplies);
        rvRegularReplies = findViewById(R.id.rvRegularReplies);
    }

    private void handleIntent() {
        post = (CommunityPost) getIntent().getSerializableExtra("post_data");
        if (post != null) {
            tvQuestionUser.setText(post.getAuthorName());
            tvQuestionTime.setText(post.getAuthorName() + " • " + post.getCreatedAt());
            tvQuestionContent.setText(post.getBody());
        }
    }

    private void setupRecyclerViews() {
        verifiedAdapter = new ReplyAdapter(new ArrayList<>());
        rvVerifiedReplies.setLayoutManager(new LinearLayoutManager(this));
        rvVerifiedReplies.setAdapter(verifiedAdapter);

        regularAdapter = new ReplyAdapter(new ArrayList<>());
        rvRegularReplies.setLayoutManager(new LinearLayoutManager(this));
        rvRegularReplies.setAdapter(regularAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> submitReply());
        findViewById(R.id.btnListen).setOnClickListener(v -> speakQuestionDetail());
    }

    private void updateListenButtonUI() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        ImageButton btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            btnListen.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    private void speakQuestionDetail() {
        if (post == null) return;
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");

        String content = post.getBody();
        String author = post.getAuthorName();

        String text = "Question from " + author + ". " + content;
        if (lang.equals("ur")) {
            text = author + " کا سوال۔ " + content;
        } else if (lang.equals("pa")) {
            text = author + " دا سوال۔ " + content;
        }
        ttsManager.speak(text);
    }

    private void fetchReplies() {
        pbReplies.setVisibility(View.VISIBLE);
        tvRepliesHeader.setVisibility(View.GONE);
        tvFarmerDiscussionsHeader.setVisibility(View.GONE);
        rvVerifiedReplies.setVisibility(View.GONE);
        rvRegularReplies.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getPostReplies(post.getId()).enqueue(new Callback<List<CommunityReply>>() {
            @Override
            public void onResponse(Call<List<CommunityReply>> call, Response<List<CommunityReply>> response) {
                pbReplies.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<CommunityReply> allReplies = response.body();
                    List<CommunityReply> verified = new ArrayList<>();
                    List<CommunityReply> regular = new ArrayList<>();

                    for (CommunityReply r : allReplies) {
                        if (r.isVerified() || r.isExpert()) {
                            verified.add(r);
                        } else {
                            regular.add(r);
                        }
                    }

                    verifiedAdapter.updateData(verified);
                    regularAdapter.updateData(regular);

                    if (!verified.isEmpty()) {
                        tvRepliesHeader.setText("Expert Discussion");
                        tvRepliesHeader.setVisibility(View.VISIBLE);
                        rvVerifiedReplies.setVisibility(View.VISIBLE);
                    }
                    if (!regular.isEmpty()) {
                        tvFarmerDiscussionsHeader.setText("Farmer Discussions");
                        tvFarmerDiscussionsHeader.setVisibility(View.VISIBLE);
                        rvRegularReplies.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(QuestionDetailActivity.this, "Failed to load replies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CommunityReply>> call, Throwable t) {
                pbReplies.setVisibility(View.GONE);
                Toast.makeText(QuestionDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitReply() {
        if (com.example.sabzazaar.utils.ProfileUtils.isGuest(this)) {
            Toast.makeText(this, "Please log in to use this feature", Toast.LENGTH_SHORT).show();
            return;
        }

        String replyText = etReply.getText().toString().trim();
        if (replyText.isEmpty()) {
            Toast.makeText(this, "Please write a reply", Toast.LENGTH_SHORT).show();
            return;
        }

        com.example.sabzazaar.utils.ProfileUtils.ensureProfileName(this, () -> {
            btnSend.setEnabled(false);

            ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
            Map<String, Object> body = new HashMap<>();
            body.put("body", replyText);

            apiService.submitReply(post.getId(), body).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    btnSend.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(QuestionDetailActivity.this, "Reply submitted", Toast.LENGTH_SHORT).show();
                        etReply.setText("");
                        fetchReplies();
                    } else {
                        Toast.makeText(QuestionDetailActivity.this, "Failed to submit reply", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    btnSend.setEnabled(true);
                    Toast.makeText(QuestionDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}
