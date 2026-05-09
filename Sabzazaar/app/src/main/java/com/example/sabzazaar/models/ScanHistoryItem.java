package com.example.sabzazaar.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ScanHistoryItem implements Serializable {
    @SerializedName("scan_id")
    public String scanId;

    @SerializedName("image_url")
    public String imageUrl;

    @SerializedName("signed_url")
    public String signedUrl;

    @SerializedName("crop_name")
    public String cropName;

    @SerializedName("disease_name")
    public String diseaseName;

    @SerializedName("risk_level")
    public String riskLevel;

    @SerializedName("confidence")
    public double confidence;

    @SerializedName("created_at")
    public String createdAt;

    /** Returns the URL to use for image loading — signed URL preferred, falls back to imageUrl. */
    public String getDisplayUrl() {
        return (signedUrl != null && !signedUrl.isEmpty()) ? signedUrl : imageUrl;
    }
}
