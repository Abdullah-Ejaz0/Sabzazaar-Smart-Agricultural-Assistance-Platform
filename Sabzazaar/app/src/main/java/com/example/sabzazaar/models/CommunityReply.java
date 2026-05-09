package com.example.sabzazaar.models;

import com.google.gson.annotations.SerializedName;

public class CommunityReply {
    @SerializedName("reply_id")
    private String id;

    @SerializedName("author_name")
    private String authorName;

    @SerializedName("author_role")
    private String authorRole;

    @SerializedName("body")
    private String body;

    @SerializedName("is_verified")
    private boolean isVerified;

    @SerializedName("is_expert")
    private boolean isExpert;

    @SerializedName("created_at")
    private String createdAt;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    /**
     * True if the RPC returned is_expert=true (new column) OR
     * if author_role == 'expert' (fallback from the profiles join).
     */
    public boolean isExpert() {
        return isExpert || "expert".equalsIgnoreCase(authorRole);
    }
    public void setExpert(boolean expert) { isExpert = expert; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}