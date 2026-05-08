package com.example.sabzazaar.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CommunityPost implements Serializable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("author_name")
    private String authorName;
    
    @SerializedName("body")
    private String body;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("photo_url")
    private String photoUrl;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("reply_count")
    private int replyCount;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }
}