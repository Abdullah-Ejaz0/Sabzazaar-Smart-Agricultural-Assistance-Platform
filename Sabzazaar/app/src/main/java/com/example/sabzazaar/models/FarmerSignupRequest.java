package com.example.sabzazaar.models;

import com.google.gson.annotations.SerializedName;

public class FarmerSignupRequest {
    @SerializedName("phone_number")
    public String phoneNumber;

    @SerializedName("full_name")
    public String fullName;

    @SerializedName("preferred_language")
    public String preferredLanguage;

    @SerializedName("voice_assistant_enabled")
    public boolean voiceAssistantEnabled;

    public FarmerSignupRequest(String phoneNumber, String preferredLanguage, boolean voiceAssistantEnabled) {
        this.phoneNumber = phoneNumber;
        this.preferredLanguage = preferredLanguage;
        this.voiceAssistantEnabled = voiceAssistantEnabled;
    }

    public FarmerSignupRequest(String phoneNumber, String fullName, String preferredLanguage, boolean voiceAssistantEnabled) {
        this.phoneNumber = phoneNumber;
        this.fullName = fullName;
        this.preferredLanguage = preferredLanguage;
        this.voiceAssistantEnabled = voiceAssistantEnabled;
    }
}
