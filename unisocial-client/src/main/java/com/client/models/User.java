package com.client.models;

import javafx.beans.property.*;

public class User {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty bio = new SimpleStringProperty();
    private final IntegerProperty followingCount = new SimpleIntegerProperty();
    private final IntegerProperty followersCount = new SimpleIntegerProperty();
    private final BooleanProperty online = new SimpleBooleanProperty();
    private final StringProperty avatarUrl = new SimpleStringProperty();

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
        return id.get();
    }
    public void setId(int id) {
        this.id.set(id);
    }
    public IntegerProperty idProperty() {
        return id;
    }

    // Username
    public String getUsername() {
        return username.get();
    }
    public void setUsername(String username) {
        this.username.set(username);
    }
    public StringProperty usernameProperty() {
        return username;
    }

    // Full Name
    public String getFullName() {
        return fullName.get();
    }
    public void setFullName(String fullName) {
        this.fullName.set(fullName);
    }
    public StringProperty fullNameProperty() {
        return fullName;
    }

    // Bio
    public String getBio() {
        return bio.get();
    }
    public void setBio(String bio) {
        this.bio.set(bio);
    }
    public StringProperty bioProperty() {
        return bio;
    }

    // Following Count
    public int getFollowingCount() {
        return followingCount.get();
    }
    public void setFollowingCount(int followingCount) {
        this.followingCount.set(followingCount);
    }
    public IntegerProperty followingCountProperty() {
        return followingCount;
    }

    // Followers Count
    public int getFollowersCount() {
        return followersCount.get();
    }
    public void setFollowersCount(int followersCount) {
        this.followersCount.set(followersCount);
    }
    public IntegerProperty followersCountProperty() {
        return followersCount;
    }

    // Online Status
    public boolean isOnline() { return online.get(); }
    public void setOnline(boolean online) { this.online.set(online); }
    public BooleanProperty onlineProperty() { return online; }

    // Avatar URL
    public String getAvatarUrl() { return avatarUrl.get(); }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl.set(avatarUrl); }
    public StringProperty avatarUrlProperty() { return avatarUrl; }

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
