package com.example.sabzazaar.activities.expert;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sabzazaar.R;
import com.example.sabzazaar.adapters.PendingQuestionAdapter;
import com.example.sabzazaar.models.CommunityPost;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import com.example.sabzazaar.utils.TTSManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpertDashboardActivity extends AppCompatActivity implements PendingQuestionAdapter.OnQuestionClickListener {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    private TTSManager ttsManager;
    private RecyclerView rvPendingQuestions;
    private PendingQuestionAdapter adapter;
    private List<CommunityPost> pendingList = new ArrayList<>();
    
    private TextView tvStatPending, tvStatAnswered, tvStatToday, tvStatUrgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert_dashboard);

        ttsManager = new TTSManager(this);
        
        initViews();
        setupRecyclerView();
        setupListeners();
        
        fetchDashboardData();
    }

    private void initViews() {
        tvStatPending = findViewById(R.id.tvStatPending);
        tvStatAnswered = findViewById(R.id.tvStatAnswered);
        tvStatToday = findViewById(R.id.tvStatToday);
        tvStatUrgent = findViewById(R.id.tvStatUrgent);
        rvPendingQuestions = findViewById(R.id.rvPendingQuestions);
    }

    private void setupRecyclerView() {
        adapter = new PendingQuestionAdapter(pendingList, this);
        rvPendingQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvPendingQuestions.setAdapter(adapter);
        rvPendingQuestions.setNestedScrollingEnabled(false); // Crucial for NestedScrollView
    }

    @Override
    public void onQuestionClick(CommunityPost post) {
        Intent intent = new Intent(this, ExpertAnswerActivity.class);
        intent.putExtra("post_id", post.getId());
        intent.putExtra("farmer_name", post.getAuthorName());
        intent.putExtra("question_text", post.getBody());
        startActivity(intent);
    }

    private void setupListeners() {
        findViewById(R.id.btnSignOut).setOnClickListener(v -> {
            // Clear ALL expert and user session data
            getSharedPreferences("user", MODE_PRIVATE).edit().clear().apply();
            
            Intent intent = new Intent(this, com.example.sabzazaar.activities.auth.OnBoardingStartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnBroadcast).setOnClickListener(v -> {
            startActivity(new Intent(this, ExpertBroadcastActivity.class));
        });

        findViewById(R.id.btnListen).setOnClickListener(v -> {
            ttsManager.speak("Expert Dashboard. Loading your pending questions.");
        });

        updateListenButtonUI();
    }

    private void fetchDashboardData() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        
        // Fetch Stats
        apiService.getDashboardStats().enqueue(new Callback<Map<String, Integer>>() {
            @Override
            public void onResponse(Call<Map<String, Integer>> call, Response<Map<String, Integer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Integer> stats = response.body();
                    tvStatPending.setText(String.valueOf(stats.getOrDefault("pending", 0)));
                    tvStatAnswered.setText(String.valueOf(stats.getOrDefault("answered", 0)));
                    tvStatToday.setText(String.valueOf(stats.getOrDefault("today", 0)));
                    tvStatUrgent.setText(String.valueOf(stats.getOrDefault("urgent", 0)));
                } else {
                    Log.e("ExpertDashboard", "Stats error: " + response.code());
                    Toast.makeText(ExpertDashboardActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Integer>> call, Throwable t) {
                Log.e("ExpertDashboard", "Stats failure: " + t.getMessage(), t);
                Toast.makeText(ExpertDashboardActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        
        // Fetch Pending Questions
        apiService.getPendingQuestions().enqueue(new Callback<List<CommunityPost>>() {
            @Override
            public void onResponse(Call<List<CommunityPost>> call, Response<List<CommunityPost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pendingList = response.body();
                    adapter.updateData(pendingList);
                }
            }

            @Override
            public void onFailure(Call<List<CommunityPost>> call, Throwable t) {
                Toast.makeText(ExpertDashboardActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateListenButtonUI() {
        android.content.SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        android.widget.ImageButton btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            btnListen.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDashboardData();
    }

    @Override
    protected void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}
