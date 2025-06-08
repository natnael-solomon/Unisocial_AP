package com.client.services;

import com.client.core.AppState;
import com.client.core.EventBus;
import com.client.events.UIEvent;
import com.client.models.User;
import java.util.concurrent.CompletableFuture;

public class UserService {
    private final NetworkService networkService;
    private final EventBus eventBus;
    private final AppState appState;
    
    public UserService(NetworkService networkService, EventBus eventBus) {
        this.networkService = networkService;
        this.eventBus = eventBus;
        this.appState = AppState.getInstance();
    }
    
    public CompletableFuture<User> getCurrentUser() {
        return CompletableFuture.supplyAsync(() -> {
            User currentUser = appState.getCurrentUser();
            if (currentUser != null) {
                return currentUser;
            }
            
            // If no current user, return null
            return null;
        });
    }
    
    public CompletableFuture<Boolean> updateProfile(String fullName, String bio) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Publish event for UI update
                eventBus.publish(new UIEvent.Loading("Updating profile..."));
                
                // TODO: Implement actual network call to update profile
                // For now, just update the local user
                User currentUser = appState.getCurrentUser();
                if (currentUser != null) {
                    currentUser.setFullName(fullName);
                    currentUser.setBio(bio);
                    appState.setCurrentUser(currentUser);
                    
                    eventBus.publish(new UIEvent.Success("Profile updated successfully"));
                    return true;
                }
                
                eventBus.publish(new UIEvent.Error("Failed to update profile: No user logged in"));
                return false;
                
            } catch (Exception e) {
                eventBus.publish(new UIEvent.Error("Failed to update profile: " + e.getMessage()));
                return false;
            }
        });
    }
    
    public CompletableFuture<Boolean> updateAvatar(byte[] imageData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Publish event for UI update
                eventBus.publish(new UIEvent.Loading("Updating avatar..."));
                
                // TODO: Implement actual network call to update avatar
                // For now, just return success
                eventBus.publish(new UIEvent.Success("Avatar updated successfully"));
                return true;
                
            } catch (Exception e) {
                eventBus.publish(new UIEvent.Error("Failed to update avatar: " + e.getMessage()));
                return false;
            }
        });
    }
    
    public CompletableFuture<User> getUserById(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: Implement actual network call to get user by ID
                // For now, just return a mock user
                return new User(userId, "user" + userId, "User " + userId, "Bio for user " + userId, 100, 200, null);
                
            } catch (Exception e) {
                System.err.println("Failed to get user: " + e.getMessage());
                return null;
            }
        });
    }
}