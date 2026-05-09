package com.example.sabzazaar.models;

import com.google.gson.annotations.SerializedName;

/**
 * Maps to a row from the Supabase 'profiles' table as returned by
 * check_phone and farmer_signup endpoints.
 *
 * Supabase field names → Java field names used throughout the app.
 */
public class UserData {
    public String id;

    /** Supabase profiles.phone → phone_number */
    @SerializedName("phone")
    public String phone_number;

    /** Supabase profiles.language → preferred_language */
    @SerializedName("language")
    public String preferred_language;

    /** Supabase profiles.voice_assistance → voice_assistant_enabled */
    @SerializedName("voice_assistance")
    public boolean voice_assistant_enabled;

    public String full_name;
    public String username;
    public String role;
    /** Supabase profiles.avatar_url → profile_photo */
    @SerializedName("avatar_url")
    public String profile_photo;

    public String region;

    /** Supabase profiles.region → location (for backwards compat) */
    public String location;

    public String created_at;
    public String updated_at;
}
