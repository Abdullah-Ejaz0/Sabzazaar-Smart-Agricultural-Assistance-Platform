package com.example.sabzazaar;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    Button btnCallApi;
    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCallApi = findViewById(R.id.btnCallApi);
        tvResult = findViewById(R.id.tvResult);

        btnCallApi.setOnClickListener(v -> {
            ApiService api = RetrofitClient.getClient().create(ApiService.class);

            api.getMessage().enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    tvResult.setText("RAW: " + response.toString());
                    Log.d("URL", call.request().url().toString());
                    if (response.isSuccessful() && response.body() != null) {
                        tvResult.setText(response.body().message);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    tvResult.setText("FAILED: " + t.toString());
                    Log.e("API_ERROR", "FULL ERROR", t);
                }
            });
        });
    }
}