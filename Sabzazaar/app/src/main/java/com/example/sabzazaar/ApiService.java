package com.example.sabzazaar;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api/test/")
    Call<ApiResponse> getMessage();

    @POST("api/users/check-phone/")
    Call<CheckPhoneResponse> checkPhoneNumber(@Body PhoneRequest phoneRequest);

    @POST("api/users/complete-signup/")
    Call<CompleteSignupResponse> completeSignup(@Body SignupRequest signupRequest);
}