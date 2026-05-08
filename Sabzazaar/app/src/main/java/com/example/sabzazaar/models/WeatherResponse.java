package com.example.sabzazaar.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    @SerializedName("current_weather")
    public CurrentWeather currentWeather;

    @SerializedName("daily")
    public DailyData daily;

    public static class CurrentWeather {
        public double temperature;
        public double windspeed;
        public int weathercode;
    }

    public static class DailyData {
        public List<String> time;
        @SerializedName("weathercode")
        public List<Integer> weatherCodes;
        @SerializedName("temperature_2m_max")
        public List<Double> tempMax;
        @SerializedName("temperature_2m_min")
        public List<Double> tempMin;
    }
}
