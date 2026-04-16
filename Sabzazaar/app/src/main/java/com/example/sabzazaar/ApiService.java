package com.example.sabzazaar;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/test/")
    Call<ApiResponse> getMessage();
}