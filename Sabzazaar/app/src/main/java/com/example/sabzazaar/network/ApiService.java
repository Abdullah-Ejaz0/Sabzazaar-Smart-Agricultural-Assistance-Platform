package com.example.sabzazaar.network;

import com.example.sabzazaar.models.ApiResponse;
import com.example.sabzazaar.models.CheckPhoneResponse;
import com.example.sabzazaar.models.CompleteSignupResponse;
import com.example.sabzazaar.models.PhoneRequest;
import com.example.sabzazaar.models.SignupRequest;
import com.example.sabzazaar.models.WeatherResponse;

import com.example.sabzazaar.models.CommunityPost;
import com.example.sabzazaar.models.CommunityReply;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("api/community/")
    Call<List<CommunityPost>> getCommunityFeed(
            @Query("category") String category,
            @Query("search") String search,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @POST("api/community/post/")
    Call<Map<String, String>> submitPost(@Body Map<String, Object> body);

    @GET("api/community/{post_id}/")
    Call<List<CommunityPost>> getPostDetail(@Path("post_id") String postId);

    @POST("api/community/{post_id}/replies/")
    Call<Map<String, String>> submitReply(@Path("post_id") String postId, @Body Map<String, Object> body);
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