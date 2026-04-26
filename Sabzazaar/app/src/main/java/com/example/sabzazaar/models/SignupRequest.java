package com.example.sabzazaar.models;

public class SignupRequest {
    public String phone_number;
    public String preferred_language;
    public boolean voice_assistant_enabled;

    public SignupRequest(String phone_number, String preferred_language, boolean voice_assistant_enabled) {
        this.phone_number = phone_number;
        this.preferred_language = preferred_language;
        this.voice_assistant_enabled = voice_assistant_enabled;
    }
}
