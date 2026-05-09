package com.example.sabzazaar.activities.expert;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabzazaar.R;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import com.example.sabzazaar.utils.TTSManager;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpertBroadcastActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    private TTSManager ttsManager;
    private EditText etTitle, etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert_broadcast);

        ttsManager = new TTSManager(this);
        
        etTitle = findViewById(R.id.etTitle);
        etMessage = findViewById(R.id.etMessage);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnSend).setOnClickListener(v -> {
            sendBroadcast();
        });

        findViewById(R.id.btnListen).setOnClickListener(v -> {
            ttsManager.speak("Broadcast Page. Enter the title and message to send a notification to all farmers.");
        });

        updateListenButtonUI();
    }

    private void sendBroadcast() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please enter title and message", Toast.LENGTH_SHORT).show();
            return;
        }

        findViewById(R.id.btnSend).setEnabled(false);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("message", message);

        apiService.sendBroadcast(body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ExpertBroadcastActivity.this, "Broadcast sent to all farmers", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    findViewById(R.id.btnSend).setEnabled(true);
                    Toast.makeText(ExpertBroadcastActivity.this, "Failed to send broadcast", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                findViewById(R.id.btnSend).setEnabled(true);
                Toast.makeText(ExpertBroadcastActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    protected void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}
