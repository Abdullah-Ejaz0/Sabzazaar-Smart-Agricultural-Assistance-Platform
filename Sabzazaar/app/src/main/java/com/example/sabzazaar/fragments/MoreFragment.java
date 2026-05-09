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
import com.example.sabzazaar.activities.auth.OnBoardingStartActivity;
import com.example.sabzazaar.activities.main.AboutActivity;
import com.example.sabzazaar.activities.main.HelpActivity;
import com.example.sabzazaar.activities.profile.MoreLanguageActivity;
import com.example.sabzazaar.activities.profile.ProfileActivity;
import com.example.sabzazaar.activities.profile.SettingsActivity;
import com.example.sabzazaar.R;
import com.example.sabzazaar.utils.TTSManager;

import android.content.Context;
import android.content.SharedPreferences;

public class MoreFragment extends Fragment {
    private TTSManager ttsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        ttsManager = new TTSManager(getContext());

        LinearLayout llProfile = view.findViewById(R.id.llProfile);
        LinearLayout llSettings = view.findViewById(R.id.llSettings);
        LinearLayout llLanguage = view.findViewById(R.id.llLanguage);
        LinearLayout llHelp = view.findViewById(R.id.llHelp);
        LinearLayout llAbout = view.findViewById(R.id.llAbout);
        LinearLayout llSignOut = view.findViewById(R.id.llSignOut);

        llProfile.setOnClickListener(v -> startActivity(new Intent(getActivity(), ProfileActivity.class)));
        llSettings.setOnClickListener(v -> startActivity(new Intent(getActivity(), SettingsActivity.class)));
        llLanguage.setOnClickListener(v -> startActivity(new Intent(getActivity(), MoreLanguageActivity.class)));
        llHelp.setOnClickListener(v -> startActivity(new Intent(getActivity(), HelpActivity.class)));
        llAbout.setOnClickListener(v -> startActivity(new Intent(getActivity(), AboutActivity.class)));
        llSignOut.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OnBoardingStartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        view.findViewById(R.id.btnListen).setOnClickListener(v -> speakMoreMenuGuide());
        updateListenButtonUI(view);

        return view;
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

    private void speakMoreMenuGuide() {
        if (getContext() == null) return;
        SharedPreferences sPref = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");

        String text = "More Menu. Here you can access your profile, settings, language, help, and about information.";
        if (lang.equals("ur")) {
            text = "مزید مینو۔ یہاں آپ اپنے پروفائل، ترتیبات، زبان، مدد اور بارے میں معلومات تک رسائی حاصل کر سکتے ہیں۔";
        } else if (lang.equals("pa")) {
            text = "ہور مینو۔ ایتھے تسیں اپنے پروفائل، ترتیبات، بولی، مدد تے بارے وچ معلومات تک رسائی حاصل کر سکدے او۔";
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