package com.example.sabzazaar.activities.scan;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.ViewTreeObserver;
import com.example.sabzazaar.R;
import com.example.sabzazaar.utils.TTSManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ScanPreviewActivity extends AppCompatActivity {
    private TTSManager ttsManager;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_preview);

        ImageView ivPreview = findViewById(R.id.ivPreview);
        String imageUriStr = getIntent().getStringExtra("image_uri");
        if (imageUriStr != null) {
            ivPreview.setImageURI(Uri.parse(imageUriStr));
            ivPreview.setColorFilter(null); // Remove the tint if it was set in XML
        }

        ttsManager = new TTSManager(this);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btnRetake).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to camera
                finish();
            }
        });

        findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Proceed to analysis
                Intent intent = new Intent(ScanPreviewActivity.this, ScanAnalysisActivity.class);
                intent.putExtra("image_uri", imageUriStr);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.btnListen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakPreviewGuide();
            }
        });

        updateListenButtonUI();
        startScanningAnimation();
    }

    private void startScanningAnimation() {
        final View scanningLine = findViewById(R.id.scanningLine);
        final View frameLayout = (View) scanningLine.getParent();

        frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                
                scanningLine.setVisibility(View.VISIBLE);
                float height = frameLayout.getHeight();
                
                ObjectAnimator animator = ObjectAnimator.ofFloat(scanningLine, "translationY", 0f, height);
                animator.setDuration(2000);
                animator.setRepeatMode(ValueAnimator.REVERSE);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.start();
            }
        });
    }

    private void updateListenButtonUI() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        android.widget.ImageButton btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            btnListen.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    private void speakPreviewGuide() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");

        String text = "Preview Page. Check if the photo is clear. Tap Confirm to start analysis or Retake to try again.";
        if (lang.equals("ur")) {
            text = "پریویو صفحہ۔ چیک کریں کہ تصویر صاف ہے۔ تجزیہ شروع کرنے کے لیے 'Confirm' دبائیں یا دوبارہ کوشش کرنے کے لیے 'Retake' دبائیں۔";
        } else if (lang.equals("pa")) {
            text = "پریویو صفحہ۔ ویکھو کہ فوٹو صاف اے۔ تجزیہ شروع کرن لئی 'Confirm' دباؤ یا دوبارہ کوشش کرن لئی 'Retake' دباؤ۔";
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

