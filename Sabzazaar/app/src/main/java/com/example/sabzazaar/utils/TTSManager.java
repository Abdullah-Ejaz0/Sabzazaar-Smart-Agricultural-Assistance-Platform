package com.example.sabzazaar.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class TTSManager implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;
    private boolean isInitialized = false;
    private Context context;

    public TTSManager(Context context) {
        if (context == null) return;
        this.context = context;
        this.tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS && context != null) {
            SharedPreferences sPref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
            String lang = sPref.getString("preferred_language", "en");
            Locale locale = new Locale(lang);
            if (lang.equals("ur") || lang.equals("pa")) {
                locale = new Locale("ur", "PK");
            }
            tts.setLanguage(locale);
            isInitialized = true;
        }
    }

    public void speak(String text) {
        if (context == null) return;
        SharedPreferences sPref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean isVoiceEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        if (isVoiceEnabled && isInitialized && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID");
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
