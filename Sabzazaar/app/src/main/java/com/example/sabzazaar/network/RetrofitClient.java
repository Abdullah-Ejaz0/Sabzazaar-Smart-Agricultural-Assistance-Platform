package com.example.sabzazaar.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.example.sabzazaar.BuildConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    public static final String BASE_URL = BuildConfig.BASE_URL;
    private static final String SUPABASE_URL = "https://evlnoytrwyjpnpiagjnn.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImV2bG5veXRyd3lqcG5waWFnam5uIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY0MzU2MTAsImV4cCI6MjA5MjAxMTYxMH0.oXx_g-1k2WDrusUZMfpesFrmLjHW5BG7rUrKgUQl-rs";

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            final Context appContext = context.getApplicationContext();
            
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            
                            // Only add auth to our backend requests
                            if (!original.url().toString().startsWith(BASE_URL)) {
                                return chain.proceed(original);
                            }

                            SharedPreferences sharedPref = appContext.getSharedPreferences("user", Context.MODE_PRIVATE);
                            String token = sharedPref.getString("access_token", null);
                            String refreshToken = sharedPref.getString("refresh_token", null);

                            if (token != null && isTokenExpired(token)) {
                                Log.d("RetrofitClient", "Token expired, refreshing...");
                                String newToken = refreshAccessToken(refreshToken, appContext);
                                if (newToken != null) {
                                    token = newToken;
                                }
                            }

                            Request.Builder requestBuilder = original.newBuilder()
                                    .header("Accept", "application/json");

                            if (token != null && !token.isEmpty()) {
                                requestBuilder.header("Authorization", "Bearer " + token);
                            }

                            return chain.proceed(requestBuilder.build());
                        }
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return true;
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            JSONObject json = new JSONObject(payload);
            long exp = json.getLong("exp");
            // Check if it expires in the next 30 seconds
            return (exp * 1000) < (System.currentTimeMillis() + 30000);
        } catch (Exception e) {
            return true;
        }
    }

    private static String refreshAccessToken(String refreshToken, Context context) {
        if (refreshToken == null || refreshToken.isEmpty()) return null;

        OkHttpClient client = new OkHttpClient();
        String jsonBody = "{\"refresh_token\":\"" + refreshToken + "\"}";
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=refresh_token")
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JsonObject json = new JsonParser().parse(responseBody).getAsJsonObject();
                String newAccess = json.get("access_token").getAsString();
                String newRefresh = json.get("refresh_token").getAsString();

                context.getSharedPreferences("user", Context.MODE_PRIVATE).edit()
                        .putString("access_token", newAccess)
                        .putString("refresh_token", newRefresh)
                        .apply();

                Log.d("RetrofitClient", "Token refreshed successfully");
                return newAccess;
            } else {
                Log.e("RetrofitClient", "Failed to refresh token: " + response.code());
            }
        } catch (IOException e) {
            Log.e("RetrofitClient", "Error refreshing token", e);
        }
        return null;
    }
}
