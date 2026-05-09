package com.example.sabzazaar.activities.main;

import com.example.sabzazaar.R;
import com.example.sabzazaar.utils.TTSManager;
import com.example.sabzazaar.network.RetrofitClient;
import com.example.sabzazaar.network.ApiService;

import android.content.Intent;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SoilHealthActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }


    private EditText phInput, nInput, pInput, kInput;
    private ProgressBar phMeter, nMeter, pMeter, kMeter;
    private TextView phValue, nValue, pValue, kValue, soilSuggestion;
    private TTSManager ttsManager;
    private ProgressBar uploadProgressBar;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_health);

        ttsManager = new TTSManager(this);

        phInput = findViewById(R.id.phInput);
        nInput = findViewById(R.id.nInput);
        pInput = findViewById(R.id.pInput);
        kInput = findViewById(R.id.kInput);

        phMeter = findViewById(R.id.phMeter);
        nMeter = findViewById(R.id.nMeter);
        pMeter = findViewById(R.id.pMeter);
        kMeter = findViewById(R.id.kMeter);

        phValue = findViewById(R.id.phValue);
        nValue = findViewById(R.id.nValue);
        pValue = findViewById(R.id.pValue);
        kValue = findViewById(R.id.kValue);
        soilSuggestion = findViewById(R.id.soilSuggestion);
        uploadProgressBar = findViewById(R.id.uploadProgressBar);

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedFileUri = result.getData().getData();
                        if (selectedFileUri != null) {
                            uploadSoilReport(selectedFileUri);
                        }
                    }
                }
        );

        findViewById(R.id.btnUploadReport).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/pdf", "image/*", "text/plain"});
            filePickerLauncher.launch(intent);
        });

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btnUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSoilMeters();
            }
        });

        findViewById(R.id.btnListen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = soilSuggestion.getText().toString();
                if (!text.isEmpty()) {
                    ttsManager.speak(text);
                }
            }
        });

        updateListenButtonUI();
        updateSoilMeters();
    }

    private void updateListenButtonUI() {
        android.content.SharedPreferences sPref = getSharedPreferences("user", android.content.Context.MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        android.widget.ImageButton btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            btnListen.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    private void uploadSoilReport(Uri uri) {
        uploadProgressBar.setVisibility(View.VISIBLE);
        findViewById(R.id.btnUploadReport).setEnabled(false);

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] fileBytes = buffer.toByteArray();

            String fileName = getFileName(uri);
            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null) mimeType = "application/octet-stream";

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), fileBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("soil_report", fileName, requestFile);

            RetrofitClient.getClient(this).create(ApiService.class).parseSoilReportSave(body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    uploadProgressBar.setVisibility(View.GONE);
                    findViewById(R.id.btnUploadReport).setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> bodyData = response.body();
                        Map<String, Object> parsed = (Map<String, Object>) bodyData.get("parsed");
                        if (parsed != null) {
                            if (parsed.get("ph") != null) phInput.setText(String.valueOf(parsed.get("ph")));
                            if (parsed.get("nitrogen") != null) {
                                double n = ((Number) parsed.get("nitrogen")).doubleValue();
                                nInput.setText(String.valueOf((int) n));
                            }
                            if (parsed.get("phosphorus") != null) {
                                double p = ((Number) parsed.get("phosphorus")).doubleValue();
                                pInput.setText(String.valueOf((int) p));
                            }
                            if (parsed.get("potassium") != null) {
                                double k = ((Number) parsed.get("potassium")).doubleValue();
                                kInput.setText(String.valueOf((int) k));
                            }
                            updateSoilMeters();
                            Toast.makeText(SoilHealthActivity.this, "Report parsed and saved!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SoilHealthActivity.this, "Failed to parse report.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    uploadProgressBar.setVisibility(View.GONE);
                    findViewById(R.id.btnUploadReport).setEnabled(true);
                    Toast.makeText(SoilHealthActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            uploadProgressBar.setVisibility(View.GONE);
            findViewById(R.id.btnUploadReport).setEnabled(true);
            Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "upload_file";
    }

    private void updateSoilMeters() {
        try {
            float ph = Float.parseFloat(phInput.getText().toString());
            int n = Integer.parseInt(nInput.getText().toString());
            int p = Integer.parseInt(pInput.getText().toString());
            int k = Integer.parseInt(kInput.getText().toString());

            phValue.setText(String.valueOf(ph));
            nValue.setText(String.valueOf(n));
            pValue.setText(String.valueOf(p));
            kValue.setText(String.valueOf(k));

            phMeter.setProgress((int) ((ph / 14.0f) * 100));
            nMeter.setProgress(Math.min(100, n));
            pMeter.setProgress(Math.min(100, p));
            kMeter.setProgress(Math.min(100, k));

            StringBuilder suggestion = new StringBuilder();
            if (ph < 6) suggestion.append("Soil too acidic. Add lime. ");
            else if (ph > 7.5) suggestion.append("Soil too alkaline. Add sulfur. ");
            else suggestion.append("pH is good. ");

            if (n < 40) suggestion.append("Low nitrogen. Add urea. ");
            else if (n > 80) suggestion.append("Nitrogen high. Avoid more. ");
            else suggestion.append("Nitrogen OK. ");

            if (p < 20) suggestion.append("Low phosphorus. Add DAP. ");
            if (k < 30) suggestion.append("Low potassium. Add potash. ");

            if (suggestion.length() == 0) suggestion.append("Your soil is balanced.");
            soilSuggestion.setText(suggestion.toString());

        } catch (NumberFormatException e) {
            // Handle error
        }
    }

    @Override
    protected void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}

