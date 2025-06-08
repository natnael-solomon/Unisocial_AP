package com.server.models;

import java.time.LocalDateTime;

/**
 * Server-side Post model
 */
public class Post {
    private int id;
    private int userId;
    private String username; // Denormalized for easier queries
    private String content;
    private String imageUrl;
    private int likeCount;
    private boolean liked; // For current user context
    private boolean bookmarked; // For current user context
    private String createdAt;
    private String updatedAt;

    // Constructors
    public Post() {}

    public Post(int userId, String username, String content) {
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.createdAt = LocalDateTime.now().toString();
        this.updatedAt = LocalDateTime.now().toString();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }

    public boolean isBookmarked() { return bookmarked; }
    public void setBookmarked(boolean bookmarked) { this.bookmarked = bookmarked; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    // Helper method to get LocalDateTime if needed
    public LocalDateTime getCreatedAtAsLocalDateTime() {
        return createdAt != null ? LocalDateTime.parse(createdAt) : null;
    }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper method to get LocalDateTime if needed
    public LocalDateTime getUpdatedAtAsLocalDateTime() {
        return updatedAt != null ? LocalDateTime.parse(updatedAt) : null;
    }
    
    // Helper method to set from LocalDateTime
    public void setCreatedAt(LocalDateTime dateTime) {
        this.createdAt = dateTime != null ? dateTime.toString() : null;
    }
    
    public void setUpdatedAt(LocalDateTime dateTime) {
        this.updatedAt = dateTime != null ? dateTime.toString() : null;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", content='" + content + '\'' +
                ", likeCount=" + likeCount +
                ", createdAt=" + createdAt +
                '}';
    }
}
