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
import com.example.sabzazaar.AskQuestionActivity;
import com.example.sabzazaar.MyQuestionsActivity;
import com.example.sabzazaar.R;

public class CommunityFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        LinearLayout btnAsk = view.findViewById(R.id.btnAsk);
        LinearLayout btnMyQuestions = view.findViewById(R.id.btnMyQuestions);

        if (btnAsk != null) {
            btnAsk.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), AskQuestionActivity.class));
            });
        }

        if (btnMyQuestions != null) {
            btnMyQuestions.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), MyQuestionsActivity.class));
            });
        }

        return view;
    }
}