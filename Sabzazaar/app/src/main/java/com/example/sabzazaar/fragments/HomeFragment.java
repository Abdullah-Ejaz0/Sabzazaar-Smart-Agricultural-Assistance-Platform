package com.example.sabzazaar.fragments;

import com.example.sabzazaar.R;
import com.example.sabzazaar.utils.TTSManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.sabzazaar.activities.main.ChatbotActivity;
import com.example.sabzazaar.activities.main.MainActivity;
import com.example.sabzazaar.activities.main.SoilHealthActivity;
import com.example.sabzazaar.activities.main.WeatherDetailsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.sabzazaar.models.WeatherResponse;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Date;
import java.text.SimpleDateFormat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sabzazaar.adapters.RecentScanAdapter;
import com.example.sabzazaar.models.ScanHistoryItem;
import com.example.sabzazaar.activities.scan.ScanAnalysisActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private TextView tvWeatherAdvice, tvHeaderTitle;
    private ImageView ivWeatherIcon;
    private TTSManager ttsManager;
    private CardView cvBroadcastAlert;
    private TextView tvBroadcastMessage, tvBroadcastTime;
    
    private RecyclerView rvRecentScans;
    private RecentScanAdapter recentScanAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvWeatherAdvice = view.findViewById(R.id.tvWeatherAdvice);
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);

        setupUserProfile(view);
        
        cvBroadcastAlert = view.findViewById(R.id.cvBroadcastAlert);
        tvBroadcastMessage = view.findViewById(R.id.tvBroadcastMessage);
        tvBroadcastTime = view.findViewById(R.id.tvBroadcastTime);

        // Setup Recent Scans RecyclerView
        rvRecentScans = view.findViewById(R.id.rvRecentScans);
        recentScanAdapter = new RecentScanAdapter(new ArrayList<>(), item -> {
            // Since we construct the ScanResponse from the item itself for now, 
            // the transition is synchronous.
            com.example.sabzazaar.models.ScanResponse response = new com.example.sabzazaar.models.ScanResponse();
            response.diseaseName = item.diseaseName;
            response.riskLevel = item.riskLevel;
            response.confidence = item.confidence;
            
            Intent intent = new Intent(getActivity(), com.example.sabzazaar.activities.scan.ScanResultsActivity.class);
            intent.putExtra("scan_result", response);
            intent.putExtra("image_uri", item.getDisplayUrl());
            
            startActivity(intent);
        });
        rvRecentScans.setAdapter(recentScanAdapter);

        fetchWeather();
        fetchBroadcasts();
        fetchRecentScans();

        ttsManager = new TTSManager(getContext());

        view.findViewById(R.id.btnListen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakHomeStatus();
            }
        });

        updateListenButtonUI(view);

        view.findViewById(R.id.cvWeather).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), WeatherDetailsActivity.class));
            }
        });

        view.findViewById(R.id.btnScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                    bottomNav.setSelectedItemId(R.id.navigation_scan);
                }
            }
        });

        view.findViewById(R.id.btnSoilHealth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SoilHealthActivity.class));
            }
        });

        view.findViewById(R.id.btnChatbot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ChatbotActivity.class));
            }
        });

        return view;
    }

    private void fetchRecentScans() {
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        apiService.getRecentScans().enqueue(new Callback<List<ScanHistoryItem>>() {
            @Override
            public void onResponse(Call<List<ScanHistoryItem>> call, Response<List<ScanHistoryItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recentScanAdapter.updateData(response.body());
                } else {
                    android.util.Log.e("HomeFragment", "Failed to load recent scans");
                }
            }

            @Override
            public void onFailure(Call<List<ScanHistoryItem>> call, Throwable t) {
                android.util.Log.e("HomeFragment", "Error fetching recent scans: " + t.getMessage());
            }
        });
    }

    private void setupUserProfile(View view) {
        if (tvHeaderTitle == null || getContext() == null) return;

        android.content.SharedPreferences sPref = getContext().getSharedPreferences("user", android.content.Context.MODE_PRIVATE);
        String fullName = sPref.getString("full_name", "");

        if (fullName == null || fullName.trim().isEmpty()) {
            tvHeaderTitle.setText(R.string.default_farmer_name);
        } else {
            tvHeaderTitle.setText(fullName);
        }
    }

    private void fetchWeather() {
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        // Default to Lahore, Pakistan coordinates
        apiService.getWeatherData(31.5204, 74.3587, true, "weathercode,temperature_2m_max,temperature_2m_min", "auto")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateWeatherUI(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        // Handle failure
                    }
                });
    }

    private void updateWeatherUI(WeatherResponse weather) {
        if (weather.currentWeather == null) return;

        int code = weather.currentWeather.weathercode;
        double temp = weather.currentWeather.temperature;

        String advice = "Clear skies today";
        int iconRes = R.drawable.ic_sunny;

        if (code >= 51 && code <= 67) {
            advice = "Rain expected – avoid spraying";
            iconRes = R.drawable.ic_rain;
        } else if (code >= 71 && code <= 77) {
            advice = "Snow expected";
            iconRes = R.drawable.ic_cold;
        } else if (code >= 95) {
            advice = "Storm warning";
            iconRes = R.drawable.ic_cloud_thunder;
        } else if (temp > 35) {
            advice = "High heat – water crops more";
            iconRes = R.drawable.ic_temperature_high;
        }

        if (tvWeatherAdvice != null) {
            tvWeatherAdvice.setText(advice + " (" + (int)temp + "°C)");
        }
        if (ivWeatherIcon != null) {
            ivWeatherIcon.setImageResource(iconRes);
        }
    }

    private void updateListenButtonUI(View view) {
        if (getContext() == null) return;
        android.content.SharedPreferences sPref = getContext().getSharedPreferences("user", android.content.Context.MODE_PRIVATE);
        boolean isEnabled = sPref.getBoolean("voice_assistant_enabled", true);
        android.widget.ImageButton btnListen = view.findViewById(R.id.btnListen);
        if (btnListen != null) {
            btnListen.setBackgroundResource(isEnabled ? R.drawable.bg_circle_light : R.drawable.bg_circle_outline);
            btnListen.setImageResource(isEnabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
    }

    private void speakHomeStatus() {
        if (getContext() == null) return;
        android.content.SharedPreferences sPref = getContext().getSharedPreferences("user", android.content.Context.MODE_PRIVATE);

        String name = sPref.getString("full_name", "Farmer");
        String weather = tvWeatherAdvice != null ? tvWeatherAdvice.getText().toString() : "";
        String lang = sPref.getString("preferred_language", "en");

        String text;
        if (lang.equals("ur")) {
            text = "خوش آمدید " + name + "۔ آج کا موسم: " + weather;
        } else if (lang.equals("pa")) {
            text = "خوش آمدید " + name + "۔ اج دا موسم: " + weather;
        } else {
            text = "Welcome " + name + ". Today's weather: " + weather;
        }

        // If there's a broadcast showing, append it to the speech
        if (cvBroadcastAlert != null && cvBroadcastAlert.getVisibility() == View.VISIBLE) {
             text += ". Important alert: " + tvBroadcastMessage.getText().toString();
        }

        ttsManager.speak(text);
    }

    private void fetchBroadcasts() {
        android.util.Log.d("HomeFragment", "Fetching broadcasts...");
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        apiService.getBroadcasts().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    android.util.Log.d("HomeFragment", "Broadcast received: " + response.body().get(0).toString());
                    Map<String, Object> latest = response.body().get(0);
                    String message = (String) latest.get("message");
                    String createdAt = (String) latest.get("created_at");

                    try {
                        // Handle standard ISO format from Supabase (e.g. 2024-05-09T18:00:00+00:00)
                        String cleanDate = createdAt.split("\\+")[0];
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date date = sdf.parse(cleanDate);
                        
                        long diff = System.currentTimeMillis() - date.getTime();
                        long hours = diff / (1000 * 60 * 60);
                        android.util.Log.d("HomeFragment", "Broadcast is " + hours + " hours old");

                        // Show if less than 24 hours old
                        if (hours < 24) {
                            cvBroadcastAlert.setVisibility(View.VISIBLE);
                            tvBroadcastMessage.setText(message);
                            if (hours == 0) {
                                tvBroadcastTime.setText("Sent just now");
                            } else {
                                tvBroadcastTime.setText("Sent " + hours + " hours ago");
                            }
                        } else {
                            android.util.Log.d("HomeFragment", "Broadcast too old, hiding.");
                            cvBroadcastAlert.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("HomeFragment", "Error parsing date: " + e.getMessage());
                    }
                } else {
                    android.util.Log.d("HomeFragment", "No broadcasts found or response failed.");
                    cvBroadcastAlert.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                android.util.Log.e("HomeFragment", "Broadcast fetch failed: " + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}
