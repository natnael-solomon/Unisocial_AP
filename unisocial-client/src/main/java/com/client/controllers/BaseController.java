package com.client.controllers;

import com.client.core.AppState;
import com.client.core.EventBus;
import com.client.core.ServiceManager;
import com.client.services.AuthService;
import com.client.services.PostService;
import com.client.services.UserService;
import javafx.scene.control.Alert;

public abstract class BaseController {

    protected final ServiceManager serviceManager;
    protected final EventBus eventBus;
    protected final AppState appState;
    protected final AuthService authService;
    protected final PostService postService;
    protected final UserService userService;

    protected BaseController() {
        // Initialize services but don't call initialize() yet
        this.serviceManager = ServiceManager.getInstance();
        this.eventBus = serviceManager.getEventBus();
        this.appState = AppState.getInstance();
        this.authService = serviceManager.getAuthService();
        this.postService = serviceManager.getPostService();
        this.userService = serviceManager.getUserService();
    }

    /**
     * Call this method after the view is properly set up
     */
    protected final void initializeBaseController() {
        // This can be called manually after view setup
        // Subclasses should call initialize() and subscribeToEvents() after this
    }

    /**
     * Initialize the controller - called after view is set up
     */
    protected abstract void initialize();

    /**
     * Subscribe to events - called after view is set up
     */
    protected abstract void subscribeToEvents();

    /**
     * Cleanup resources when controller is destroyed
     */
    public void cleanup() {
        // Default implementation - subclasses can override
    }

    /**
     * Show error dialog
     */
    protected void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show info dialog
     */
    protected void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show warning dialog
     */
    protected void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}