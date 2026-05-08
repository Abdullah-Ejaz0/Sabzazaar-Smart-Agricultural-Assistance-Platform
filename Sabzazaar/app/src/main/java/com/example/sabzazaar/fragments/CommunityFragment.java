package com.example.sabzazaar.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sabzazaar.activities.main.AskQuestionActivity;
import com.example.sabzazaar.activities.profile.MyQuestionsActivity;
import com.example.sabzazaar.activities.main.QuestionDetailActivity;
import com.example.sabzazaar.R;
import com.example.sabzazaar.adapters.CommunityAdapter;
import com.example.sabzazaar.models.CommunityPost;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityFragment extends Fragment {
    private RecyclerView rvCommunityQuestions;
    private CommunityAdapter adapter;
    private List<CommunityPost> postList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        LinearLayout btnAsk = view.findViewById(R.id.btnAsk);
        LinearLayout btnMyQuestions = view.findViewById(R.id.btnMyQuestions);
        rvCommunityQuestions = view.findViewById(R.id.rvCommunityQuestions);

        setupRecyclerView();
        fetchCommunityPosts();

        if (btnAsk != null) {
            btnAsk.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), AskQuestionActivity.class));
            });
        }

        if (btnMyQuestions != null) {
            btnMyQuestions.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), MyQuestionsActivity.class));
            });
        }

        return view;
    }

    private void setupRecyclerView() {
        adapter = new CommunityAdapter(postList, post -> {
            Intent intent = new Intent(getActivity(), QuestionDetailActivity.class);
            intent.putExtra("post_data", post);
            startActivity(intent);
        });
        rvCommunityQuestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCommunityQuestions.setAdapter(adapter);
    }

    private void fetchCommunityPosts() {
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        apiService.getCommunityFeed(null, null, 20, 0).enqueue(new Callback<List<CommunityPost>>() {
            @Override
            public void onResponse(Call<List<CommunityPost>> call, Response<List<CommunityPost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    postList = response.body();
                    adapter.updateData(postList);
                } else {
                    Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CommunityPost>> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;

                Log.e("CommunityFragment", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}