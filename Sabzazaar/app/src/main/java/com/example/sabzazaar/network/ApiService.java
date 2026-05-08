package com.example.sabzazaar.network;

import com.example.sabzazaar.models.ApiResponse;
import com.example.sabzazaar.models.CheckPhoneResponse;
import com.example.sabzazaar.models.CompleteSignupResponse;
import com.example.sabzazaar.models.PhoneRequest;
import com.example.sabzazaar.models.SignupRequest;
import com.example.sabzazaar.models.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @GET("api/test/")
    Call<ApiResponse> getMessage();

    @POST("api/users/check-phone/")
    Call<CheckPhoneResponse> checkPhoneNumber(@Body PhoneRequest phoneRequest);

    @POST("api/users/complete-signup/")
    Call<CompleteSignupResponse> completeSignup(@Body SignupRequest signupRequest);

    @GET("https://api.open-meteo.com/v1/forecast")
    Call<WeatherResponse> getWeatherData(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current_weather") boolean currentWeather,
            @Query("daily") String daily,
            @Query("timezone") String timezone
    );
}