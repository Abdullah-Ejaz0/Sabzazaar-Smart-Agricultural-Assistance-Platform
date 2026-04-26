package com.example.sabzazaar.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.sabzazaar.R;
import com.example.sabzazaar.ScanResultsActivity;

public class ScanFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        view.findViewById(R.id.btnTakePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the simulated scan flow
                startActivity(new Intent(getActivity(), com.example.sabzazaar.ScanCameraActivity.class));
            }
        });

        view.findViewById(R.id.btnGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // For demo, just start the scan flow as well or open gallery picker
                startActivity(new Intent(getActivity(), com.example.sabzazaar.ScanCameraActivity.class));
            }
        });

        view.findViewById(R.id.btnListen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement TTS for Scan screen
            }
        });

        return view;
    }
}