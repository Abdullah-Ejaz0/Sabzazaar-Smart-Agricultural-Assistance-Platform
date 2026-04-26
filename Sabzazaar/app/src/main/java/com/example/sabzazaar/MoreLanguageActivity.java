package com.example.sabzazaar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MoreLanguageActivity extends AppCompatActivity {

    private String selectedLanguage = "en";
    private ImageView ivCheckEnglish, ivCheckUrdu, ivCheckPunjabi;

    private LinearLayout llEnglish, llUrdu, llPunjabi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_language);

        ivCheckEnglish = findViewById(R.id.ivCheckEnglish);
        ivCheckUrdu = findViewById(R.id.ivCheckUrdu);
        ivCheckPunjabi = findViewById(R.id.ivCheckPunjabi);

        llEnglish = findViewById(R.id.llEnglish);
        llUrdu = findViewById(R.id.llUrdu);
        llPunjabi = findViewById(R.id.llPunjabi);
        Button btnSave = findViewById(R.id.btnSave);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        llEnglish.setOnClickListener(v -> selectLanguage("en"));
        llUrdu.setOnClickListener(v -> selectLanguage("ur"));
        llPunjabi.setOnClickListener(v -> selectLanguage("pa"));

        // Initialize with English selected
        selectLanguage("en");

        btnSave.setOnClickListener(v -> {
            Toast.makeText(this, "Language updated to " + selectedLanguage, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void selectLanguage(String lang) {
        selectedLanguage = lang;
        ivCheckEnglish.setVisibility(lang.equals("en") ? View.VISIBLE : View.GONE);
        ivCheckUrdu.setVisibility(lang.equals("ur") ? View.VISIBLE : View.GONE);
        ivCheckPunjabi.setVisibility(lang.equals("pa") ? View.VISIBLE : View.GONE);

        llEnglish.setSelected(lang.equals("en"));
        llUrdu.setSelected(lang.equals("ur"));
        llPunjabi.setSelected(lang.equals("pa"));
    }
}