package com.server.models;

import java.time.LocalDateTime;

/**
 * Server-side User model
 */
public class User {
    private int id;
    private String username;
    private String fullName;
    private String bio;
    private String avatarUrl;
    private int followingCount;
    private int followersCount;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public User() {}

    public User(int id, String username, String fullName) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.createdAt = LocalDateTime.now().toString();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public int getFollowingCount() { return followingCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }

    public int getFollowersCount() { return followersCount; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }

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
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", followersCount=" + followersCount +
                ", followingCount=" + followingCount +
                '}';
    }
}
