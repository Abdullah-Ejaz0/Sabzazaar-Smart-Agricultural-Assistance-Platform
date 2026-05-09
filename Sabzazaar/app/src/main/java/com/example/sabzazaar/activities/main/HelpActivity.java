package com.example.sabzazaar.activities.main;

import com.example.sabzazaar.R;

import com.example.sabzazaar.utils.TTSManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {
    private TTSManager ttsManager;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ttsManager = new TTSManager(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        findViewById(R.id.btnListen).setOnClickListener(v -> speakHelpGuide());
        updateListenButtonUI();
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

    private void speakHelpGuide() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");

        String text = "Help and Tutorials. To use Sabzazaar, first, tap the camera to scan a crop leaf. Second, see the disease name and risk level. Third, follow the advice or ask an expert.";
        if (lang.equals("ur")) {
            text = "مدد اور سبق۔ سبزہ زار استعمال کرنے کے لیے، پہلے، فصل کے پتے کو اسکین کرنے کے لیے کیمرے پر ٹیپ کریں۔ دوسرا، بیماری کا نام اور خطرے کی سطح دیکھیں۔ تیسرا، مشورے پر عمل کریں یا کسی ماہر سے پوچھیں۔";
        } else if (lang.equals("pa")) {
            text = "مدد تے سبق۔ سبزہ زار نوں ورتن لئی، پہلاں، فصل دے پتے نوں اسکین کرن لئی کیمرے تے ٹیپ کرو۔ دوجا، بیماری دا ناں تے خطرے دی پدھر دیکھو۔ تیجا، مشورے تے عمل کرو یا کسی ماہر توں پچھو۔";
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

