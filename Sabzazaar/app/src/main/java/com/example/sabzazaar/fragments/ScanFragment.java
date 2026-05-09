package com.example.sabzazaar.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sabzazaar.R;
import com.example.sabzazaar.activities.scan.ScanCameraActivity;
import com.example.sabzazaar.activities.scan.ScanPreviewActivity;
import com.example.sabzazaar.activities.scan.ScanResultsActivity;
import com.example.sabzazaar.utils.TTSManager;

import android.content.Context;
import android.content.SharedPreferences;

public class ScanFragment extends Fragment {
    private TTSManager ttsManager;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            Intent intent = new Intent(getActivity(), ScanPreviewActivity.class);
                            intent.putExtra("image_uri", selectedImage.toString());
                            startActivity(intent);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        if (getContext() != null) {
            ttsManager = new TTSManager(getContext());
        }

        view.findViewById(R.id.btnTakePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the simulated scan flow
                startActivity(new Intent(getActivity(), ScanCameraActivity.class));
            }
        });

        view.findViewById(R.id.btnGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        view.findViewById(R.id.btnListen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakScanInfo();
            }
        });

        updateListenButtonUI(view);

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
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

    private void speakScanInfo() {
        if (ttsManager == null || getContext() == null) return;
        SharedPreferences sPref = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");

        String text = "Scan Page. Take a photo of your crop or choose from gallery to detect diseases.";
        if (lang.equals("ur")) {
            text = "اسکین پیج۔ بیماریوں کی تشخیص کے لیے اپنی فصل کی تصویر لیں یا گیلری سے منتخب کریں۔";
        } else if (lang.equals("pa")) {
            text = "اسکین پیج۔ بیماریاں لبھن لئی اپنی فصل دی فوٹو کھچو یا گیلری توں چونو۔";
        }
        ttsManager.speak(text);
    }

    @Override
    public void onDestroyView() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroyView();
    }
}