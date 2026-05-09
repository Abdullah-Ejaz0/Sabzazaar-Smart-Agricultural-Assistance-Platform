package com.example.sabzazaar.activities.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.sabzazaar.R;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import com.example.sabzazaar.utils.TTSManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AskQuestionActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }


    // ── Category values sent to backend ────────────────────────────────────────
    private static final String[] CATEGORY_VALUES = {
            "general", "crop_disease", "fertilizer", "pesticide", "irrigation", "weather"
    };

    // ── Category display labels (Urdu-friendly English) ────────────────────────
    private static final String[] CATEGORY_LABELS = {
            "General", "Crop Disease", "Fertilizer", "Pesticide", "Irrigation", "Weather"
    };

    // Views
    private ImageView ivPreview;
    private LinearLayout imagePreviewContainer;
    private EditText etQuestion;
    private Spinner spinnerCategory;
    private Button btnPost;

    // State
    private String selectedCategory = CATEGORY_VALUES[0];
    private Uri selectedImageUri = null;
    private TTSManager ttsManager;

    // Camera support
    private Uri cameraImageUri = null;

    // ── Image pick launchers ───────────────────────────────────────────────────
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    displayPreview(selectedImageUri);
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && cameraImageUri != null) {
                    selectedImageUri = cameraImageUri;
                    displayPreview(selectedImageUri);
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) launchCamera();
                else Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_question);

        // ── Bind views ─────────────────────────────────────────────────────────
        ImageButton btnBack           = findViewById(R.id.btnBack);
        View btnCamera                = findViewById(R.id.btnCamera);
        View btnGallery               = findViewById(R.id.btnGallery);
        imagePreviewContainer         = findViewById(R.id.imagePreviewContainer);
        ivPreview                     = findViewById(R.id.ivPreview);
        etQuestion                    = findViewById(R.id.etQuestion);
        spinnerCategory               = findViewById(R.id.spinnerCategory);
        btnPost                       = findViewById(R.id.btnPost);

        // ── Back button ────────────────────────────────────────────────────────
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // ── Category spinner setup ─────────────────────────────────────────────
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_selected,
                CATEGORY_LABELS
        );
        categoryAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = CATEGORY_VALUES[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { /* no-op */ }
        });

        // ── Photo buttons ──────────────────────────────────────────────────────
        if (imagePreviewContainer != null) {
            imagePreviewContainer.setOnClickListener(v -> openGallery());
        }
        if (btnCamera != null)  btnCamera.setOnClickListener(v -> requestCameraAndLaunch());
        if (btnGallery != null) btnGallery.setOnClickListener(v -> openGallery());

        // ── Post button ────────────────────────────────────────────────────────
        if (btnPost != null) btnPost.setOnClickListener(v -> submitPost());

        // ── TTS Setup ──────────────────────────────────────────────────────────
        ttsManager = new TTSManager(this);
        View btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setOnClickListener(v -> speakGuide());
        }
        updateListenButtonUI();
    }

    private void updateListenButtonUI() {
        android.content.SharedPreferences sPref = getSharedPreferences("user", android.content.Context.MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        View btnListen = findViewById(R.id.btnListen);
        if (btnListen != null) {
            if (isEnabled) {
                btnListen.setBackgroundResource(R.drawable.bg_circle_light);
            } else {
                btnListen.setBackgroundResource(R.drawable.bg_circle_outline);
            }
        }
    }

    private void speakGuide() {
        android.content.SharedPreferences sPref = getSharedPreferences("user", android.content.Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");

        String text = "Ask a question to the community. Add a photo, type your question, and select a category.";
        if (lang.equals("ur")) {
            text = "کمیونٹی سے سوال پوچھیں۔ تصویر شامل کریں، اپنا سوال لکھیں، اور کیٹیگری منتخب کریں۔";
        } else if (lang.equals("pa")) {
            text = "کمیونٹی توں سوال پچھو۔ تصویر لاؤ، اپنا سوال لکھو، تے کیٹیگری چنو۔";
        }
        ttsManager.speak(text);
    }

    // ── Image helpers ──────────────────────────────────────────────────────────

    private void displayPreview(Uri uri) {
        if (ivPreview != null) {
            ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ivPreview.setImageURI(uri);
            // Remove tint so the actual photo shows
            ivPreview.setColorFilter(null);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void requestCameraAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            File photoFile = createTempImageFile();
            cameraImageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            Toast.makeText(this, "Could not open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getCacheDir();
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    // ── Submit logic ───────────────────────────────────────────────────────────

    private void submitPost() {
        String questionText = (etQuestion != null && etQuestion.getText() != null)
                ? etQuestion.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(questionText)) {
            Toast.makeText(this, "Please type your question", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPost.setEnabled(false);
        btnPost.setText("Posting…");

        if (selectedImageUri != null) {
            uploadPhotoAndPost(questionText, selectedCategory, selectedImageUri);
        } else {
            createPostOnBackend(questionText, selectedCategory, null);
        }
    }

    private void uploadPhotoAndPost(String text, String category, Uri imageUri) {
        try {
            // Create a temporary file to hold the image data
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            File tempFile = new File(getCacheDir(), "upload_temp.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("photo", tempFile.getName(), requestFile);

            ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
            apiService.uploadCommunityPhoto(body).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().containsKey("photo_url")) {
                        String photoUrl = response.body().get("photo_url");
                        createPostOnBackend(text, category, photoUrl);
                    } else {
                        btnPost.setEnabled(true);
                        btnPost.setText("Post Question");
                        Toast.makeText(AskQuestionActivity.this, "Failed to upload photo", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    btnPost.setEnabled(true);
                    btnPost.setText("Post Question");
                    Toast.makeText(AskQuestionActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            btnPost.setEnabled(true);
            btnPost.setText("Post Question");
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void createPostOnBackend(String text, String category, String photoUrl) {
        Map<String, Object> body = new HashMap<>();
        body.put("body", text);
        body.put("category", category);
        if (photoUrl != null) {
            body.put("photo_url", photoUrl);
        }

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.submitPost(body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                btnPost.setEnabled(true);
                btnPost.setText("Post Question");
                if (response.isSuccessful()) {
                    Toast.makeText(AskQuestionActivity.this,
                            "Question posted successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    try {
                        String errorMsg = response.errorBody() != null ? response.errorBody().string() : "code: " + response.code();
                        Toast.makeText(AskQuestionActivity.this,
                                "Failed: " + errorMsg,
                                Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(AskQuestionActivity.this,
                                "Failed to post question (code: " + response.code() + ")",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                btnPost.setEnabled(true);
                btnPost.setText("Post Question");
                Toast.makeText(AskQuestionActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}
