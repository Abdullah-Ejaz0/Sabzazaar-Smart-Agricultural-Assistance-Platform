package com.example.sabzazaar.activities.scan;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sabzazaar.R;
import com.example.sabzazaar.utils.TTSManager;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanCameraActivity extends AppCompatActivity {
    private static final String TAG = "ScanCameraActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    private TTSManager ttsManager;
    private ImageCapture imageCapture;
    private PreviewView viewFinder;
    private ExecutorService cameraExecutor;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_camera);

        viewFinder = findViewById(R.id.viewFinder);
        ttsManager = new TTSManager(this);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnCapture).setOnClickListener(v -> takePhoto());

        findViewById(R.id.btnListen).setOnClickListener(v -> speakCameraGuide());

        cameraExecutor = Executors.newSingleThreadExecutor();
        updateListenButtonUI();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(getExternalFilesDir(null), 
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Uri savedUri = Uri.fromFile(photoFile);
                Intent intent = new Intent(ScanCameraActivity.this, ScanPreviewActivity.class);
                intent.putExtra("image_uri", savedUri.toString());
                startActivity(intent);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
            }
        });
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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

    private void speakCameraGuide() {
        SharedPreferences sPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");

        String text = "Camera Page. Position the leaf in the center box and tap the capture button.";
        if (lang.equals("ur")) {
            text = "کیمرہ صفحہ۔ پتے کو درمیانی خانے میں رکھیں اور کیپچر بٹن کو تھپتھپائیں۔";
        } else if (lang.equals("pa")) {
            text = "کیمرہ صفحہ۔ پتے نوں وچکارلے ڈبے وچ رکھو تے کیپچر بٹن نوں دباؤ۔";
        }
        ttsManager.speak(text);
    }

    @Override
    protected void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        super.onDestroy();
    }
}

