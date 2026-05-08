package com.example.sabzazaar.models;

import com.google.gson.annotations.SerializedName;

public class CommunityReply {
    @SerializedName("id")
    private String id;
    
    @SerializedName("author_name")
    private String authorName;
    
    @SerializedName("body")
    private String body;
    
    @SerializedName("is_verified")
    private boolean isVerified;
    
    @SerializedName("created_at")
    private String createdAt;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}