package com.example.sabzazaar.activities.profile;

import com.example.sabzazaar.R;
import com.example.sabzazaar.adapters.CommunityAdapter;
import com.example.sabzazaar.models.CommunityPost;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import com.example.sabzazaar.activities.main.QuestionDetailActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyQuestionsActivity extends AppCompatActivity {
    private RecyclerView rvMyQuestions;
    private CommunityAdapter adapter;
    private List<CommunityPost> postList = new ArrayList<>();

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_questions);

        initViews();
        setupRecyclerView();
        fetchMyHistory();
    }

    private void initViews() {
        rvMyQuestions = findViewById(R.id.rvMyQuestions);
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new CommunityAdapter(postList, post -> {
            Intent intent = new Intent(this, QuestionDetailActivity.class);
            intent.putExtra("post_data", post);
            startActivity(intent);
        });
        rvMyQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvMyQuestions.setAdapter(adapter);
    }

    private void fetchMyHistory() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getUserHistory().enqueue(new Callback<List<CommunityPost>>() {
            @Override
            public void onResponse(Call<List<CommunityPost>> call, Response<List<CommunityPost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    postList = response.body();
                    adapter.updateData(postList);
                    if (postList.isEmpty()) {
                        Toast.makeText(MyQuestionsActivity.this, "No history found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyQuestionsActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CommunityPost>> call, Throwable t) {
                Toast.makeText(MyQuestionsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
