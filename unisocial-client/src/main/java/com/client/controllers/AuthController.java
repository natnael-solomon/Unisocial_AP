package com.client.controllers;

import com.client.UniSocialApplication;
import com.client.events.AuthEvent;
import com.client.views.AuthView;
import com.client.views.LoginView;
import com.client.views.SignupView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class AuthController extends BaseController {
    private final AuthView view;
    private LoginController loginController;
    private SignupController signupController;
    
    private double xOffset = 0;
    private double yOffset = 0;
    
    public AuthController(AuthView view) {
        // DON'T call super() here - it will call initialize() before view is set
        this.view = view;

        // Now manually initialize the base controller components
        initializeBaseController();

        // Then initialize this controller
        initialize();
        subscribeToEvents();
    }
    
    @Override
    protected void initialize() {
        setupWindowDragging();
        setupCloseButton();
        setupToggleSwitch();
        showLoginView(); // Default view
    }
    
    @Override
    protected void subscribeToEvents() {
        eventBus.subscribe(AuthEvent.LoginSuccess.class, this::handleLoginSuccess);
        eventBus.subscribe(AuthEvent.SignupSuccess.class, this::handleSignupSuccess);
    }
    
    private void setupWindowDragging() {
        if (view != null) {
            view.setOnMousePressed(this::handleMousePressed);
            view.setOnMouseDragged(this::handleMouseDragged);
        }
    }
    
    private void setupCloseButton() {
        if (view != null && view.getCloseButton() != null) {
            view.getCloseButton().setOnMouseClicked(this::handleClose);
        }
    }
    
    private void setupToggleSwitch() {
        if (view != null && view.getViewToggle() != null) {
            view.getViewToggle().setOnSwitchAction(event -> {
                String selectedView = view.getViewToggle().getSelectedView();
                if ("Sign In".equals(selectedView)) {
                    showLoginView();
                } else if ("Sign Up".equals(selectedView)) {
                    showSignupView();
                }
            });
        }
    }
    
    private void showLoginView() {
        if (loginController != null) {
            loginController.cleanup();
        }
        
        LoginView loginView = new LoginView();
        loginController = new LoginController(loginView);
        
        if (view != null && view.getContentArea() != null) {
            view.getContentArea().getChildren().setAll(loginView);
        }
    }
    
    private void showSignupView() {
        if (signupController != null) {
            signupController.cleanup();
        }
        
        SignupView signupView = new SignupView();
        signupController = new SignupController(signupView);
        
        if (view != null && view.getContentArea() != null) {
            view.getContentArea().getChildren().setAll(signupView);
        }
    }
    
    private void handleMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }
    
    private void handleMouseDragged(MouseEvent event) {
        if (view != null && view.getScene() != null && view.getScene().getWindow() != null) {
            Stage stage = (Stage) view.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }
    
    private void handleClose(MouseEvent event) {
        if (view != null && view.getScene() != null && view.getScene().getWindow() != null) {
            Stage stage = (Stage) view.getScene().getWindow();
            stage.close();
        }
    }
    
    private void handleLoginSuccess(AuthEvent.LoginSuccess event) {
        UniSocialApplication.showFeedScreen(); // Use App instead of UniSocialApplication
    }
    
    private void handleSignupSuccess(AuthEvent.SignupSuccess event) {
        UniSocialApplication.showFeedScreen(); // Use App instead of UniSocialApplication
    }
    
    @Override
    public void cleanup() {
        if (loginController != null) {
            loginController.cleanup();
        }
        if (signupController != null) {
            signupController.cleanup();
        }
    }
}