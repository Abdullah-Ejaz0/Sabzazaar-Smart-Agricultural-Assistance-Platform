package com.example.sabzazaar.activities.main;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.sabzazaar.R;
import com.example.sabzazaar.fragments.CommunityFragment;
import com.example.sabzazaar.fragments.HomeFragment;
import com.example.sabzazaar.fragments.MoreFragment;
import com.example.sabzazaar.fragments.ScanFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.navigation_scan) {
                selectedFragment = new ScanFragment();
            } else if (itemId == R.id.navigation_community) {
                selectedFragment = new CommunityFragment();
            } else if (itemId == R.id.navigation_more) {
                selectedFragment = new MoreFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

//        btnCallApi = findViewById(R.id.btnCallApi);
//        tvResult = findViewById(R.id.tvResult);

//        btnCallApi.setOnClickListener(v -> {
//            ApiService api = RetrofitClient.getClient().create(ApiService.class);
//
//            api.getMessage().enqueue(new Callback<ApiResponse>() {
//                @Override
//                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
//                    tvResult.setText("RAW: " + response.toString());
//                    Log.d("URL", call.request().url().toString());
//                    if (response.isSuccessful() && response.body() != null) {
//                        tvResult.setText(response.body().message);
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<ApiResponse> call, Throwable t) {
//                    tvResult.setText("FAILED: " + t.toString());
//                    Log.e("API_ERROR", "FULL ERROR", t);
//                }
//            });
//        });
    }
}