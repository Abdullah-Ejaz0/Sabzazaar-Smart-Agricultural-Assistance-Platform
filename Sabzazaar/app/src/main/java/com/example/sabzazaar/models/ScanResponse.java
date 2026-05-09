package com.example.sabzazaar.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ScanResponse implements Serializable {
    @SerializedName("scan_id")
    public String scanId;
    
    @SerializedName("image_path")
    public String imagePath;
    
    @SerializedName("crop_name")
    public String cropName;
    
    @SerializedName("disease_key")
    public String diseaseKey;
    
    @SerializedName("disease_name")
    public String diseaseName;
    
    public double confidence;
    
    @SerializedName("risk_level")
    public String riskLevel;
    
    @SerializedName("class_probabilities")
    public Map<String, Double> classProbabilities;
    
    public Recommendations recommendations;
    
    @SerializedName("llm_report")
    public String llmReport;

    public static class Recommendations implements Serializable {
        @SerializedName("display_name")
        public String displayName;
        
        public String pathogen;
        public FertilizerInfo fertilizer;
        public PesticideInfo pesticide;
        public String disclaimer;
    }

    public static class FertilizerInfo implements Serializable {
        public String recommendation;
        public List<Chemical> chemicals;
        public String note;
    }

    public static class PesticideInfo implements Serializable {
        public String recommendation;
        public List<Chemical> chemicals;
    }

    public static class Chemical implements Serializable {
        public String name;
        public String dosage;
        public String frequency;
    }
}
