package com.example.sabzazaar.fragments;

import com.example.sabzazaar.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.sabzazaar.activities.main.ChatbotActivity;
import com.example.sabzazaar.activities.main.MainActivity;
import com.example.sabzazaar.activities.main.SoilHealthActivity;
import com.example.sabzazaar.activities.main.WeatherDetailsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.widget.ImageView;
import android.widget.TextView;
import com.example.sabzazaar.models.WeatherResponse;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private TextView tvWeatherAdvice;
    private ImageView ivWeatherIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvWeatherAdvice = view.findViewById(R.id.tvWeatherAdvice);
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);

        fetchWeather();

        view.findViewById(R.id.btnListen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement TTS for Home Screen
            }
        });

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
            iconRes = R.id.ivWeatherIcon; // Using a hot icon if available
            iconRes = R.drawable.ic_temperature_high;
        }

        if (tvWeatherAdvice != null) {
            tvWeatherAdvice.setText(advice + " (" + (int)temp + "°C)");
        }
        if (ivWeatherIcon != null) {
            ivWeatherIcon.setImageResource(iconRes);
        }
    }
}