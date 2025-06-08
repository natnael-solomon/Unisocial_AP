package com.client.core;

import com.client.models.User;
import javafx.beans.property.*;

public class AppState {
    private static AppState instance;
    
    private final ObjectProperty<User> currentUser = new SimpleObjectProperty<>();
    private final BooleanProperty isAuthenticated = new SimpleBooleanProperty(false);
    private final BooleanProperty isConnected = new SimpleBooleanProperty(false);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    
    private AppState() {}
    
    public static AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }
    
    // Current User
    public User getCurrentUser() { return currentUser.get(); }
    public void setCurrentUser(User user) { 
        currentUser.set(user);
        isAuthenticated.set(user != null);
    }
    public ObjectProperty<User> currentUserProperty() { return currentUser; }
    
    // Authentication Status
    public boolean isAuthenticated() { return isAuthenticated.get(); }
    public BooleanProperty isAuthenticatedProperty() { return isAuthenticated; }
    
    // Connection Status
    public boolean isConnected() { return isConnected.get(); }
    public void setConnected(boolean connected) { isConnected.set(connected); }
    public BooleanProperty isConnectedProperty() { return isConnected; }
    
    // Status Message
    public String getStatusMessage() { return statusMessage.get(); }
    public void setStatusMessage(String message) { statusMessage.set(message); }
    public StringProperty statusMessageProperty() { return statusMessage; }
}