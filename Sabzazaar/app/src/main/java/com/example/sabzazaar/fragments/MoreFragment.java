package com.example.sabzazaar.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.sabzazaar.AboutActivity;
import com.example.sabzazaar.HelpActivity;
import com.example.sabzazaar.MoreLanguageActivity;
import com.example.sabzazaar.ProfileActivity;
import com.example.sabzazaar.R;
import com.example.sabzazaar.SettingsActivity;

public class MoreFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        LinearLayout llProfile = view.findViewById(R.id.llProfile);
        LinearLayout llSettings = view.findViewById(R.id.llSettings);
        LinearLayout llLanguage = view.findViewById(R.id.llLanguage);
        LinearLayout llHelp = view.findViewById(R.id.llHelp);
        LinearLayout llAbout = view.findViewById(R.id.llAbout);

        llProfile.setOnClickListener(v -> startActivity(new Intent(getActivity(), ProfileActivity.class)));
        llSettings.setOnClickListener(v -> startActivity(new Intent(getActivity(), SettingsActivity.class)));
        llLanguage.setOnClickListener(v -> startActivity(new Intent(getActivity(), MoreLanguageActivity.class)));
        llHelp.setOnClickListener(v -> startActivity(new Intent(getActivity(), HelpActivity.class)));
        llAbout.setOnClickListener(v -> startActivity(new Intent(getActivity(), AboutActivity.class)));

        return view;
    }
}