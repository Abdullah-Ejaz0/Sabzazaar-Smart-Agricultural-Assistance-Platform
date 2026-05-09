package com.example.sabzazaar.activities.main;

import com.example.sabzazaar.R;
import com.example.sabzazaar.utils.TTSManager;

import android.content.Intent;
import android.speech.RecognizerIntent;
import com.example.sabzazaar.adapters.ChatAdapter;
import com.example.sabzazaar.models.ChatMessage;
import com.example.sabzazaar.models.ScanResponse;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.SharedPreferences;

public class ChatbotActivity extends AppCompatActivity {
    private ScanResponse scanResult;
    private TextView tvDiseaseContext;
    private RecyclerView rvChatHistory;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private EditText etQuestion;
    private View btnSend;
    private View btnMic;
    private View layoutQuickActions;
    private static final int SPEECH_REQUEST_CODE = 123;
    private List<Map<String, String>> chatHistoryForBackend = new ArrayList<>();
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    private TTSManager ttsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        ttsManager = new TTSManager(this);

        scanResult = (ScanResponse) getIntent().getSerializableExtra("scan_result");
        tvDiseaseContext = findViewById(R.id.tvDiseaseContext);
        rvChatHistory = findViewById(R.id.rvChatHistory);
        etQuestion = findViewById(R.id.etQuestion);
        btnSend = findViewById(R.id.btnSend);
        btnMic = findViewById(R.id.btnMic);
        layoutQuickActions = findViewById(R.id.layoutQuickActions);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        rvChatHistory.setLayoutManager(new LinearLayoutManager(this));
        rvChatHistory.setAdapter(chatAdapter);

        if (scanResult != null) {
            tvDiseaseContext.setVisibility(View.VISIBLE);
            tvDiseaseContext.setText(getString(R.string.disease_context_format, scanResult.cropName, scanResult.diseaseName));
        } else {
            if (tvDiseaseContext != null) {
                tvDiseaseContext.setVisibility(View.GONE);
            }
        }
        
        // Setup Common Questions (Example data)
        RecyclerView rvCommonQuestions = findViewById(R.id.rvCommonQuestions);
        if (rvCommonQuestions != null) {
            rvCommonQuestions.setLayoutManager(new LinearLayoutManager(this));
            List<String> commonQuestions = new ArrayList<>();
            commonQuestions.add("How to manage water in rice fields?");
            commonQuestions.add("What are the signs of Stem Borer?");
            commonQuestions.add("How to apply Urea correctly?");
            
            // Reusing a simple adapter or just making them clickable if already implemented
            // For now, I'll assume they were just placeholders or I'll implement a quick one
            setupCommonQuestions(rvCommonQuestions, commonQuestions);
        }

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rvChatHistory.getVisibility() == View.VISIBLE) {
                    showQuickActions();
                } else {
                    finish();
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question = etQuestion.getText().toString().trim();
                if (!question.isEmpty()) {
                    showChat();
                    askQuestion(question);
                    etQuestion.setText("");
                }
            }
        });

        btnMic.setOnClickListener(v -> startSpeechToText());

        findViewById(R.id.btnHowToScan).setOnClickListener(v -> {
            showChat();
            askQuestion("How to scan a leaf correctly?");
        });
        findViewById(R.id.btnDiseaseInfo).setOnClickListener(v -> {
            showChat();
            if (scanResult != null) {
                askQuestion("Tell me more about " + scanResult.diseaseName);
            } else {
                askQuestion("Tell me about common rice diseases in Pakistan.");
            }
        });
        findViewById(R.id.btnWeatherAdvice).setOnClickListener(v -> {
            showChat();
            askQuestion("What is the best weather for rice planting?");
        });
        findViewById(R.id.btnPesticideAdvice).setOnClickListener(v -> {
            showChat();
            askQuestion("Which pesticides are best for rice?");
        });

        findViewById(R.id.btnListen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakChatbotIntro();
            }
        });

        updateListenButtonUI();
    }

    private void showChat() {
        if (layoutQuickActions != null) layoutQuickActions.setVisibility(View.GONE);
        if (rvChatHistory != null) rvChatHistory.setVisibility(View.VISIBLE);
    }

    private void showQuickActions() {
        if (layoutQuickActions != null) layoutQuickActions.setVisibility(View.VISIBLE);
        if (rvChatHistory != null) rvChatHistory.setVisibility(View.GONE);
        // Clear history if needed, or keep it. User said "disappear", implying a transition.
    }

    private void setupCommonQuestions(RecyclerView rv, List<String> questions) {
        // Simple adapter for common questions
        rv.setAdapter(new RecyclerView.Adapter<CommonQuestionViewHolder>() {
            @NonNull
            @Override
            public CommonQuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = android.view.LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                TextView tv = v.findViewById(android.R.id.text1);
                tv.setTextColor(android.graphics.Color.parseColor("#424242"));
                return new CommonQuestionViewHolder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull CommonQuestionViewHolder holder, int position) {
                String q = questions.get(position);
                ((TextView) holder.itemView.findViewById(android.R.id.text1)).setText(q);
                holder.itemView.setOnClickListener(v -> {
                    showChat();
                    askQuestion(q);
                });
            }

            @Override
            public int getItemCount() {
                return questions.size();
            }
        });
    }

    static class CommonQuestionViewHolder extends RecyclerView.ViewHolder {
        CommonQuestionViewHolder(View v) { super(v); }
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        
        SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
        
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                etQuestion.setText(spokenText);
                // Optionally send immediately
                // showChat();
                // askQuestion(spokenText);
            }
        }
    }

    private void addBotMessage(String text) {
        messageList.add(new ChatMessage(text, ChatMessage.TYPE_BOT));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChatHistory.smoothScrollToPosition(messageList.size() - 1);
        
        // Add to history for backend
        Map<String, String> entry = new HashMap<>();
        entry.put("role", "assistant");
        entry.put("content", text);
        chatHistoryForBackend.add(entry);
    }

    private void askQuestion(String question) {
        messageList.add(new ChatMessage(question, ChatMessage.TYPE_USER));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChatHistory.smoothScrollToPosition(messageList.size() - 1);

        // Prepare request
        Map<String, Object> body = new HashMap<>();
        body.put("question", question);
        body.put("history", chatHistoryForBackend);

        // Add to history for backend
        Map<String, String> entry = new HashMap<>();
        entry.put("role", "user");
        entry.put("content", question);
        chatHistoryForBackend.add(entry);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Log.d("ChatbotActivity", "Asking chatbot at: " + RetrofitClient.BASE_URL + "api/chatbot/ask/");
        apiService.askChatbot(body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String answer = response.body().get("answer");
                    addBotMessage(answer);
                    
                    SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
                    if (sPref.getBoolean("voice_assistant_enabled", true)) {
                        ttsManager.speak(answer);
                    }
                } else {
                    String errorMsg = "Error " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Log.e("ChatbotActivity", errorMsg);
                    Toast.makeText(ChatbotActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    addBotMessage("Sorry, I encountered an error. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e("ChatbotActivity", "Network failure", t);
                Toast.makeText(ChatbotActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                addBotMessage("Network error. Please check your connection.");
            }
        });
    }

    private void updateListenButtonUI() {
        SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        android.widget.ImageButton btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            btnListen.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    private void speakChatbotIntro() {
        SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");
        String text;
        if (lang.equals("ur")) {
            text = "میں آپ کا سبزہ زار اسسٹنٹ ہوں۔ آپ مجھ سے فصلوں کی بیماریوں، موسم اور کھاد کے بارے میں پوچھ سکتے ہیں۔";
        } else if (lang.equals("pa")) {
            text = "میں تواڈا سبزہ زار اسسٹنٹ واں۔ تسیں میرے توں فصلاں دیاں بیماریاں، موسم تے کھاد بارے پچھ سکدے او۔";
        } else {
            text = "I am your Sabzazaar assistant. You can ask me about crop diseases, weather, and fertilizers.";
        }

        ttsManager.speak(text);
    }

    @Override
    protected void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}

