package com.client.models;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Post {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty content = new SimpleStringProperty();
    private final StringProperty imageUrl = new SimpleStringProperty();
    private final IntegerProperty likeCount = new SimpleIntegerProperty();
    private final BooleanProperty liked = new SimpleBooleanProperty();
    private final BooleanProperty bookmarked = new SimpleBooleanProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    
    public Post() {}
    
    public Post(int id, int userId, String username, String content) {
        setId(id);
        setUserId(userId);
        setUsername(username);
        setContent(content);
        setCreatedAt(LocalDateTime.now());
    }
    
    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }
    
    // User ID
    public int getUserId() { return userId.get(); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public IntegerProperty userIdProperty() { return userId; }
    
    // Username
    public String getUsername() { return username.get(); }
    public void setUsername(String username) { this.username.set(username); }
    public StringProperty usernameProperty() { return username; }
    
    // Content
    public String getContent() { return content.get(); }
    public void setContent(String content) { this.content.set(content); }
    public StringProperty contentProperty() { return content; }
    
    // Image URL
    public String getImageUrl() { return imageUrl.get(); }
    public void setImageUrl(String imageUrl) { this.imageUrl.set(imageUrl); }
    public StringProperty imageUrlProperty() { return imageUrl; }
    
    // Like Count
    public int getLikeCount() { return likeCount.get(); }
    public void setLikeCount(int count) { this.likeCount.set(count); }
    public IntegerProperty likeCountProperty() { return likeCount; }
    
    // Liked
    public boolean isLiked() { return liked.get(); }
    public void setLiked(boolean liked) { this.liked.set(liked); }
    public BooleanProperty likedProperty() { return liked; }
    
    // Bookmarked
    public boolean isBookmarked() { return bookmarked.get(); }
    public void setBookmarked(boolean bookmarked) { this.bookmarked.set(bookmarked); }
    public BooleanProperty bookmarkedProperty() { return bookmarked; }
    
    // Created At
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime dateTime) { this.createdAt.set(dateTime); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    
    @Override
    public String toString() {
        return "Post{" +
                "id=" + getId() +
                ", userId=" + getUserId() +
                ", username='" + getUsername() + '\'' +
                ", content='" + getContent() + '\'' +
                ", likeCount=" + getLikeCount() +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}