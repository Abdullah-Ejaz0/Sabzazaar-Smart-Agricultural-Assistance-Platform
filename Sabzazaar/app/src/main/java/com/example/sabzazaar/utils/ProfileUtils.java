package com.example.sabzazaar.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sabzazaar.R;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileUtils {

    public interface OnNameReadyListener {
        void onReady();
    }

    public static boolean isGuest(Context context) {
        return context.getSharedPreferences("user", Context.MODE_PRIVATE).getBoolean("is_guest", false);
    }

    public static void ensureProfileName(Context context, OnNameReadyListener listener) {
        SharedPreferences sPref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String fullName = sPref.getString("full_name", "");

        if (fullName != null && !fullName.trim().isEmpty()) {
            // Name is already set, proceed
            listener.onReady();
            return;
        }

        // Show AlertDialog to ask for name
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        // You can create a custom layout for the dialog to match the UI, or just use a standard one.
        // We'll use a simple layout programmatically for now.
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_name_prompt, null);
        EditText etName = dialogView.findViewById(R.id.etNamePrompt);
        Button btnSave = dialogView.findViewById(R.id.btnSaveName);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelName);
        
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSave.setEnabled(false);
            btnSave.setText("Saving...");

            ApiService apiService = RetrofitClient.getClient(context).create(ApiService.class);
            Map<String, Object> body = new HashMap<>();
            body.put("full_name", newName);

            apiService.updateProfile(body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        sPref.edit().putString("full_name", newName).apply();
                        dialog.dismiss();
                        listener.onReady();
                    } else {
                        btnSave.setEnabled(true);
                        btnSave.setText("Save");
                        Toast.makeText(context, "Failed to save name", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                    Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}
