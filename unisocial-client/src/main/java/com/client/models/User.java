package com.client.models;

import java.time.LocalDateTime;

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

    public User() {}

    public User(int id, String username, String fullName, String bio, int followingCount, int followersCount, String avatarUrl) {
        setId(id);
        setUsername(username);
        setFullName(fullName);
        setBio(bio);
        setFollowingCount(followingCount);
        setFollowersCount(followersCount);
        setAvatarUrl(avatarUrl);
    }

    public User(int id, String username, String avatarUrl) {
        setId(id);
        setUsername(username);
        setFullName(username); // Default full name to username
        setAvatarUrl(avatarUrl);
    }

    // ID
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    // Username
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    // Full Name
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    // Bio
    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    // Following Count
    public int getFollowingCount() {
        return followingCount;
    }
    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    // Followers Count
    public int getFollowersCount() {
        return followersCount;
    }
    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    // Created At
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Updated At
    public String getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Avatar URL
    public String getAvatarUrl() {
        return avatarUrl;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", avatarUrl='" + getAvatarUrl() + '\'' +
                '}';
    }
}
