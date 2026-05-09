package com.example.sabzazaar.activities.scan;

import com.example.sabzazaar.BuildConfig;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sabzazaar.models.ScanResponse;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import com.example.sabzazaar.utils.TTSManager;
import com.example.sabzazaar.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanAnalysisActivity extends AppCompatActivity {
    private TTSManager ttsManager;
    private ProgressBar progressBar;
    private TextView tvSubtext;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private Uri imageUri;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_analysis);

        String uriStr = getIntent().getStringExtra("image_uri");
        if (uriStr != null) {
            imageUri = Uri.parse(uriStr);
        }

        ttsManager = new TTSManager(this);
        speakAnalysisStart();

        progressBar = findViewById(R.id.pbAnalysis);
        tvSubtext = findViewById(R.id.tvAnalysisSubtext);

        startFakeProgress();
        
        if (imageUri != null) {
            uploadAndDetect();
        } else {
            Toast.makeText(this, "No image found for analysis", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startFakeProgress() {
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 90) {
                    progressStatus += 2;
                    handler.post(new Runnable() {
                        public void run() {
                            if (progressBar != null) progressBar.setProgress(progressStatus);
                            if (tvSubtext != null) {
                                if (progressStatus == 20) tvSubtext.setText(R.string.checking_leaf_health);
                                if (progressStatus == 50) tvSubtext.setText(R.string.checking_leaf_color);
                                if (progressStatus == 80) tvSubtext.setText(R.string.comparing_diseases);
                            }
                        }
                    });
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void uploadAndDetect() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] bytes = getBytes(inputStream);
            
            if (bytes == null || bytes.length == 0) {
                Log.e("ScanAnalysis", "Image bytes are empty");
                Toast.makeText(this, "Failed to read image data", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            Log.d("ScanAnalysis", "Image size: " + bytes.length + " bytes");
            
            RequestBody requestFile = RequestBody.create(bytes, MediaType.parse("image/jpeg"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", "scan.jpg", requestFile);
            
            Log.d("ScanAnalysis", "Starting upload to: " + BuildConfig.BASE_URL + "api/scans/detect/");
            ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
            apiService.detectDisease(body, "Rice", "true").enqueue(new Callback<ScanResponse>() {
                @Override
                public void onResponse(Call<ScanResponse> call, Response<ScanResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        progressStatus = 100;
                        progressBar.setProgress(100);
                        tvSubtext.setText(R.string.almost_done);
                        
                        Intent intent = new Intent(ScanAnalysisActivity.this, ScanResultsActivity.class);
                        intent.putExtra("scan_result", response.body());
                        intent.putExtra("image_uri", imageUri.toString());
                        startActivity(intent);
                        finish();
                    } else {
                        String errorBody = "No error body";
                        try {
                            if (response.errorBody() != null) {
                                errorBody = response.errorBody().string();
                            }
                        } catch (IOException e) {
                            Log.e("ScanAnalysis", "Failed to read error body", e);
                        }
                        Log.e("ScanAnalysis", "Error Code: " + response.code());
                        Log.e("ScanAnalysis", "Error Message: " + response.message());
                        Log.e("ScanAnalysis", "Error Body: " + errorBody);
                        
                        String finalErrorBody = errorBody;
                        runOnUiThread(() -> {
                            Toast.makeText(ScanAnalysisActivity.this, "Analysis failed (" + response.code() + ")", Toast.LENGTH_LONG).show();
                            // If it's a 500 error and we have HTML (Django error page), it might be too long for a toast.
                            // But at least we logged it.
                        });
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<ScanResponse> call, Throwable t) {
                    Log.e("ScanAnalysis", "Network Failure", t);
                    Toast.makeText(ScanAnalysisActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void speakAnalysisStart() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");

        String text = "Analyzing your crop. Please wait a moment while we check leaf health and color.";
        if (lang.equals("ur")) {
            text = "آپ کی فصل کا تجزیہ کر رہا ہے۔ براہ کرم تھوڑی دیر انتظار کریں جب کہ ہم پتوں کی صحت اور رنگ چیک کر رہے ہیں۔";
        } else if (lang.equals("pa")) {
            text = "تہاڈی فصل دا تجزیہ ہو رہیا اے۔ مہربانی کر کے تھوڑی دیر انتظار کرو جدوں اسیں پتے دی صحت تے رنگ ویکھ رہے آں۔";
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
