package com.example.sabzazaar.network;

import com.example.sabzazaar.models.ApiResponse;
import com.example.sabzazaar.models.CheckPhoneResponse;
import com.example.sabzazaar.models.CompleteSignupResponse;
import com.example.sabzazaar.models.FarmerSignupRequest;
import com.example.sabzazaar.models.PhoneRequest;
import com.example.sabzazaar.models.SignupRequest;
import com.example.sabzazaar.models.WeatherResponse;

import com.example.sabzazaar.models.CommunityPost;
import com.example.sabzazaar.models.CommunityReply;
import com.example.sabzazaar.models.ScanHistoryItem;
import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
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
    Call<List<CommunityReply>> getPostReplies(@Path("post_id") String postId);

    @GET("api/community/my-history/")
    Call<List<CommunityPost>> getUserHistory();

    @POST("api/community/{post_id}/replies/")
    Call<Map<String, String>> submitReply(@Path("post_id") String postId, @Body Map<String, Object> body);

    // Expert Portal
    @POST("api/auth/expert/login/")
    Call<Map<String, Object>> expertLogin(@Body Map<String, String> credentials);

    @GET("api/expert/dashboard/stats/")
    Call<Map<String, Integer>> getDashboardStats();

    @GET("api/expert/pending/")
    Call<List<CommunityPost>> getPendingQuestions();

    @POST("api/expert/broadcast/")
    Call<Map<String, String>> sendBroadcast(@Body Map<String, Object> body);

    @GET("api/test/")
    Call<ApiResponse> getMessage();

    @POST("api/auth/check-phone/")
    Call<CheckPhoneResponse> checkPhoneNumber(@Body PhoneRequest phoneRequest);

    @POST("api/auth/profile/update/")
    Call<Map<String, Object>> updateProfile(@Body Map<String, Object> body);

    @POST("api/auth/profile/voice-preference/")
    Call<Map<String, Object>> updateVoicePreference(@Body Map<String, Object> body);

    @POST("api/auth/farmer/signup/")
    Call<CompleteSignupResponse> farmerSignup(@Body FarmerSignupRequest signupRequest);

    @GET("https://api.open-meteo.com/v1/forecast")
    Call<WeatherResponse> getWeatherData(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current_weather") boolean currentWeather,
            @Query("daily") String daily,
            @Query("timezone") String timezone
    );

    @retrofit2.http.Multipart
    @POST("api/scans/detect/")
    Call<com.example.sabzazaar.models.ScanResponse> detectDisease(
            @retrofit2.http.Part okhttp3.MultipartBody.Part image,
            @retrofit2.http.Part("crop_name") String cropName,
            @retrofit2.http.Part("use_llm") String useLlm
    );

    @POST("api/chatbot/ask/")
    Call<Map<String, String>> askChatbot(@Body Map<String, Object> body);

    @GET("api/chatbot/history/")
    Call<List<Map<String, Object>>> getChatbotHistory(
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("api/broadcasts/")
    Call<List<Map<String, Object>>> getBroadcasts();

    // Recent Scans
    @GET("api/scans/recent/")
    Call<List<ScanHistoryItem>> getRecentScans();

    // Community photo upload
    @Multipart
    @POST("api/community/upload-photo/")
    Call<Map<String, String>> uploadCommunityPhoto(
            @Part MultipartBody.Part photo
    );

    @Multipart
    @POST("api/soil/parse/save/")
    Call<Map<String, Object>> parseSoilReportSave(
            @Part MultipartBody.Part file
    );
}