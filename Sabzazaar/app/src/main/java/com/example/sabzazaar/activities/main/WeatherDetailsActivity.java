package com.example.sabzazaar.activities.main;

import com.example.sabzazaar.R;
import com.example.sabzazaar.models.WeatherResponse;
import com.example.sabzazaar.network.ApiService;
import com.example.sabzazaar.network.RetrofitClient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class WeatherDetailsActivity extends AppCompatActivity {

    private LinearLayout llAdviceContainer;
    private LinearLayout llForecastContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_details);

        llAdviceContainer = findViewById(R.id.llAdviceContainer);
        llForecastContainer = findViewById(R.id.llForecastContainer);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        fetchWeatherData();
    }

    private void fetchWeatherData() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getWeatherData(31.5204, 74.3587, true, "weathercode,temperature_2m_max,temperature_2m_min", "auto")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateUI(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {}
                });
    }

    private void updateUI(WeatherResponse weather) {
        updateAdvice(weather);
        updateForecast(weather);
    }

    private void updateAdvice(WeatherResponse weather) {
        llAdviceContainer.removeAllViews();
        int code = weather.currentWeather.weathercode;
        double temp = weather.currentWeather.temperature;

        List<AdviceItem> advices = new ArrayList<>();
        
        if (code >= 51 && code <= 67) {
            advices.add(new AdviceItem("Rain tomorrow – don't spray", R.drawable.ic_rain));
        } else {
            advices.add(new AdviceItem("Clear skies – good for spraying", R.drawable.ic_sunny));
        }

        if (temp > 30) {
            advices.add(new AdviceItem("High temperature – irrigate crops", R.drawable.ic_temperature_high));
        }

        if (weather.currentWeather.windspeed > 15) {
            advices.add(new AdviceItem("Strong wind – avoid spraying", R.drawable.ic_wind));
        } else {
            advices.add(new AdviceItem("Low wind – good for sowing", R.drawable.ic_wind));
        }

        for (AdviceItem item : advices) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_weather_advice, llAdviceContainer, false);
            ((ImageView) view.findViewById(R.id.ivIcon)).setImageResource(item.icon);
            ((TextView) view.findViewById(R.id.tvText)).setText(item.text);
            llAdviceContainer.addView(view);
        }
    }

    private void updateForecast(WeatherResponse weather) {
        llForecastContainer.removeAllViews();
        if (weather.daily == null || weather.daily.time == null) return;

        for (int i = 0; i < Math.min(3, weather.daily.time.size()); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_forecast, llForecastContainer, false);
            String day = (i == 0) ? "Today" : (i == 1) ? "Tomorrow" : "Day " + (i + 1);
            
            ((TextView) view.findViewById(R.id.tvDay)).setText(day);
            ((TextView) view.findViewById(R.id.tvTemp)).setText((int)Math.round(weather.daily.tempMax.get(i)) + "°C");
            ((ImageView) view.findViewById(R.id.ivIcon)).setImageResource(getWeatherIcon(weather.daily.weatherCodes.get(i)));
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            view.setLayoutParams(params);
            llForecastContainer.addView(view);
        }
    }

    private int getWeatherIcon(int code) {
        if (code <= 3) return R.drawable.ic_sunny;
        if (code >= 51 && code <= 67) return R.drawable.ic_rain;
        if (code >= 71 && code <= 77) return R.drawable.ic_cold;
        if (code >= 95) return R.drawable.ic_cloud_thunder;
        return R.drawable.ic_cloud_sun;
    }

    private static class AdviceItem {
        String text;
        int icon;
        AdviceItem(String text, int icon) { this.text = text; this.icon = icon; }
    }
}
