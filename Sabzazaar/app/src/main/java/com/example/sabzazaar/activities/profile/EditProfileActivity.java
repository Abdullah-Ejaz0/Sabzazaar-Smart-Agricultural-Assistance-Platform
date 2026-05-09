package com.example.sabzazaar.activities.profile;

import com.example.sabzazaar.R;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    private EditText etEditName, etEditVillage, etEditPhone;
    private Button btnSave;
    private TextToSpeech tts;
    private boolean isTtsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etEditName = findViewById(R.id.etEditName);
        etEditVillage = findViewById(R.id.etEditVillage);
        etEditPhone = findViewById(R.id.etEditPhone);
        
        // Disable phone editing since it's used for auth
        etEditPhone.setEnabled(false);

        populateFields();

        tts = new TextToSpeech(this, this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        ImageButton btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setOnClickListener(v -> speakProfileInfo());
        }

        updateListenButtonUI();

        Button btnCancel = findViewById(R.id.btnCancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        btnSave = findViewById(R.id.btnSave);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfile());
        }
    }

    private void updateListenButtonUI() {
        SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        ImageButton btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            btnListen.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    private void populateFields() {
        SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
        etEditName.setText(sPref.getString("full_name", ""));
        etEditVillage.setText(sPref.getString("location", ""));
        etEditPhone.setText(sPref.getString("phone_number", ""));
    }

    private void saveProfile() {
        String newName = etEditName.getText().toString().trim();
        String newLocation = etEditVillage.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Map<String, Object> body = new HashMap<>();
        body.put("full_name", newName);
        body.put("location", newLocation);

        apiService.updateProfile(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                    editor.putString("full_name", newName);
                    editor.putString("location", newLocation);
                    editor.apply();
                    
                    Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText("Save");
                Toast.makeText(EditProfileActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true;
            SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
            String lang = sPref.getString("preferred_language", "en");
            Locale locale = new Locale(lang);
            if (lang.equals("ur") || lang.equals("pa")) {
                locale = new Locale("ur", "PK");
            }
            tts.setLanguage(locale);
        }
    }

    private void speakProfileInfo() {
        SharedPreferences sPref = getSharedPreferences("user", MODE_PRIVATE);
        boolean isVoiceEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        if (!isVoiceEnabled || !isTtsInitialized || tts == null) return;

        String name = etEditName.getText().toString();
        String village = etEditVillage.getText().toString();
        String lang = sPref.getString("preferred_language", "en");

        String text;
        if (lang.equals("ur")) {
            text = "آپ کا نام " + name + " ہے۔ آپ کا گاؤں " + village + " ہے۔";
        } else if (lang.equals("pa")) {
            text = "تہاڈا ناں " + name + " اے۔ تہاڈا پنڈ " + village + " اے۔";
        } else {
            text = "Your name is " + name + ". Your village is " + village + ".";
        }

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "EditProfileTTS");
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}

