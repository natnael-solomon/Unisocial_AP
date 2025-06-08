package com.client.models;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Post {
    private int id;
    private int userId;
    private String username;
    private String content;
    private String imageUrl;
    private int likeCount;
    private boolean liked;
    private boolean bookmarked;
    private String createdAt;
    private String updatedAt;
    
    public Post() {}
    
    public Post(int id, int userId, String username, String content) {
        setId(id);
        setUserId(userId);
        setUsername(username);
        setContent(content);
        setCreatedAt(LocalDateTime.now().toString());
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    // User ID
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    // Username
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    // Content
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    // Image URL
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    // Like Count
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    
    // Liked
    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    
    // Bookmarked
    public boolean isBookmarked() { return bookmarked; }
    public void setBookmarked(boolean bookmarked) { this.bookmarked = bookmarked; }
    
    // Created At
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods for JavaFX binding
    public StringProperty usernameProperty() {
        return new SimpleStringProperty(username != null ? username : "");
    }
    
    public StringProperty contentProperty() {
        return new SimpleStringProperty(content != null ? content : "");
    }
    
    public IntegerProperty likeCountProperty() {
        return new SimpleIntegerProperty(likeCount);
    }
    
    public BooleanProperty likedProperty() {
        return new SimpleBooleanProperty(liked);
    }
    
    public BooleanProperty bookmarkedProperty() {
        return new SimpleBooleanProperty(bookmarked);
    }
    
    // Helper methods for LocalDateTime conversion
    public LocalDateTime getCreatedAtAsLocalDateTime() {
        return getCreatedAt() != null ? LocalDateTime.parse(getCreatedAt()) : null;
    }
    
    public LocalDateTime getUpdatedAtAsLocalDateTime() {
        return getUpdatedAt() != null ? LocalDateTime.parse(getUpdatedAt()) : null;
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