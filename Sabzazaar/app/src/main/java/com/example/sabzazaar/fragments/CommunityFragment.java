package com.example.sabzazaar.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sabzazaar.R;
import com.example.sabzazaar.activities.main.AskQuestionActivity;
import com.example.sabzazaar.activities.main.QuestionDetailActivity;
import com.example.sabzazaar.activities.profile.MyQuestionsActivity;
import com.example.sabzazaar.adapters.CommunityAdapter;
import com.example.sabzazaar.models.CommunityPost;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import com.example.sabzazaar.utils.TTSManager;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityFragment extends Fragment {

    private static final String TAG = "CommunityFragment";
    private static final int SEARCH_DEBOUNCE_MS = 400;

    private RecyclerView rvCommunityQuestions;
    private CommunityAdapter adapter;
    private List<CommunityPost> postList = new ArrayList<>();

    private EditText etSearch;
    private TTSManager ttsManager;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Track current active retrofit call so we can cancel on new search
    private Call<List<CommunityPost>> currentFeedCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        if (getContext() != null) {
            ttsManager = new TTSManager(getContext());
        }

        LinearLayout btnAsk         = view.findViewById(R.id.btnAsk);
        LinearLayout btnMyQuestions = view.findViewById(R.id.btnMyQuestions);
        rvCommunityQuestions        = view.findViewById(R.id.rvCommunityQuestions);
        etSearch                    = view.findViewById(R.id.etSearch);

        view.findViewById(R.id.btnListen).setOnClickListener(v -> speakCommunityInfo());
        updateListenButtonUI(view);

        setupRecyclerView();
        fetchCommunityPosts(null);   // initial load — no search query

        // ── Ask button ─────────────────────────────────────────────────────────
        if (btnAsk != null) {
            btnAsk.setOnClickListener(v -> {
                if (getContext() == null) return;
                
                if (com.example.sabzazaar.utils.ProfileUtils.isGuest(getContext())) {
                    Toast.makeText(getContext(), "Please log in to use this feature", Toast.LENGTH_SHORT).show();
                    return;
                }

                com.example.sabzazaar.utils.ProfileUtils.ensureProfileName(getContext(), () -> {
                    startActivityForResult(
                            new Intent(getActivity(), AskQuestionActivity.class), 1001);
                });
            });
        }

        // ── My Questions button ────────────────────────────────────────────────
        if (btnMyQuestions != null) {
            btnMyQuestions.setOnClickListener(v ->
                    startActivity(new Intent(getActivity(), MyQuestionsActivity.class)));
        }

        // ── Search bar — debounced live search ─────────────────────────────────
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                    final String query = s.toString().trim();
                    searchRunnable = () -> fetchCommunityPosts(query.isEmpty() ? null : query);
                    searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
                }
                @Override public void afterTextChanged(Editable s) { }
            });
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Refresh feed when a new question is posted
        if (requestCode == 1001 && resultCode == requireActivity().RESULT_OK) {
            String currentQuery = etSearch != null ? etSearch.getText().toString().trim() : null;
            fetchCommunityPosts(currentQuery == null || currentQuery.isEmpty() ? null : currentQuery);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        if (currentFeedCall != null) currentFeedCall.cancel();
    }

    private void updateListenButtonUI(View view) {
        if (getContext() == null) return;
        SharedPreferences sPref = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        android.widget.ImageButton btnListen = view.findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            btnListen.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    private void speakCommunityInfo() {
        if (ttsManager == null || getContext() == null) return;
        SharedPreferences sPref = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");

        String text = "Community Page. Ask questions, see what other farmers are discussing, and check your own questions.";
        if (lang.equals("ur")) {
            text = "کمیونٹی پیج۔ سوالات پوچھیں، دیکھیں کہ دوسرے کسان کیا بحث کر رہے ہیں، اور اپنے سوالات چیک کریں۔";
        } else if (lang.equals("pa")) {
            text = "کمیونٹی پیج۔ سوال پچھو، ویکھو دوجے کسان کی گلاں کر رہے نیں، تے اپنے سوال ویکھو۔";
        }
        ttsManager.speak(text);
    }

    // ── RecyclerView ───────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new CommunityAdapter(postList, post -> {
            Intent intent = new Intent(getActivity(), QuestionDetailActivity.class);
            intent.putExtra("post_data", post);
            startActivity(intent);
        });
        rvCommunityQuestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCommunityQuestions.setAdapter(adapter);
    }

    // ── API call ───────────────────────────────────────────────────────────────

    /**
     * Fetches the community feed from the backend.
     *
     * @param searchQuery null for full feed, non-null string for filtered results.
     *                    Maps to GET /api/community/?search=<query>
     */
    private void fetchCommunityPosts(@Nullable String searchQuery) {
        if (!isAdded() || getContext() == null) return;

        // Cancel any in-flight request before starting a new one
        if (currentFeedCall != null && !currentFeedCall.isCanceled()) {
            currentFeedCall.cancel();
        }

        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        currentFeedCall = apiService.getCommunityFeed(null, searchQuery, 20, 0);
        currentFeedCall.enqueue(new Callback<List<CommunityPost>>() {
            @Override
            public void onResponse(Call<List<CommunityPost>> call,
                                   Response<List<CommunityPost>> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    postList = response.body();
                    adapter.updateData(postList);

                    if (postList.isEmpty() && searchQuery != null) {
                        Toast.makeText(getContext(),
                                "No results for '" + searchQuery + "'",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Feed error: " + response.code());
                    Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CommunityPost>> call, Throwable t) {
                if (call.isCanceled()) return;   // expected; don't show error
                if (!isAdded() || getContext() == null) return;
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}