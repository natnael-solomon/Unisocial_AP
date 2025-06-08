package com.client.controllers;

import com.client.events.AuthEvent;
import com.client.models.User;
import com.client.utils.ValidationUtils;
import com.client.views.LoginView;
import javafx.application.Platform;

public class LoginController extends BaseController {
    private final LoginView view;

    public LoginController(LoginView view) {
        this.view = view;

        // Initialize base controller components
        initializeBaseController();

        // Then initialize this controller
        initialize();
        subscribeToEvents();
    }

    @Override
    protected void initialize() {
        if (view != null) {
            setupEventHandlers();
            setupValidation();
        }
    }

    @Override
    protected void subscribeToEvents() {
        eventBus.subscribe(AuthEvent.LoginSuccess.class, this::handleLoginSuccess);
        eventBus.subscribe(AuthEvent.LoginFailure.class, this::handleLoginFailure);
    }

    private void setupEventHandlers() {
        if (view.getLoginButton() != null) {
            view.getLoginButton().setOnAction(e -> handleLogin());
        }

        if (view.getUsernameField() != null) {
            view.getUsernameField().setOnAction(e -> handleLogin());
        }

        if (view.getPasswordField() != null) {
            view.getPasswordField().setOnAction(e -> handleLogin());
        }
    }

    private void setupValidation() {
        // Real-time validation for username field
        if (view.getUsernameField() != null) {
            view.getUsernameField().textProperty().addListener((obs, oldText, newText) -> {
                validateUsernameField(newText);
            });
        }

        // Real-time validation for password field
        if (view.getPasswordField() != null) {
            view.getPasswordField().textProperty().addListener((obs, oldText, newText) -> {
                validatePasswordField(newText);
                updateLoginButtonState();
            });
        }
    }

    private void validateUsernameField(String username) {
        String error = ValidationUtils.getUsernameValidationError(username);
        if (error != null && !ValidationUtils.isEmpty(username)) {
            // Show validation error for non-empty invalid input
            showFieldError(error);
        } else {
            clearError();
        }
        updateLoginButtonState();
    }

    private void validatePasswordField(String password) {
        String error = ValidationUtils.getPasswordValidationError(password);
        if (error != null && !ValidationUtils.isEmpty(password)) {
            // Show validation error for non-empty invalid input
            showFieldError(error);
        } else {
            clearError();
        }
    }

    private void updateLoginButtonState() {
        if (view != null && view.getLoginButton() != null) {
            String username = view.getUsernameField().getText();
            String password = view.getPasswordField().getText();

            boolean isValid = ValidationUtils.isValidUsername(username) &&
                    ValidationUtils.isValidPassword(password);

            view.getLoginButton().setDisable(!isValid);
        }
    }

    private void handleLogin() {
        if (view == null) return;

        String username = ValidationUtils.sanitizeInput(view.getUsernameField().getText().trim());
        String password = view.getPasswordField().getText();

        // Clear previous error
        clearError();

        // Comprehensive validation
        String validationError = ValidationUtils.validateFields(
                new ValidationUtils.FieldValidation("Username", username, ValidationUtils.FieldType.USERNAME),
                new ValidationUtils.FieldValidation("Password", password, ValidationUtils.FieldType.PASSWORD)
        );

        if (validationError != null) {
            showFieldError(validationError);
            return;
        }

        // Disable form during login
        setFormEnabled(false);

        // Attempt login
        authService.login(username, password);
    }

    private void handleLoginSuccess(AuthEvent.LoginSuccess event) {
        Platform.runLater(() -> {
            setFormEnabled(true);
            clearForm();
        });
    }

    private void handleLoginFailure(AuthEvent.LoginFailure event) {
        Platform.runLater(() -> {
            setFormEnabled(true);
            showFieldError(event.getMessage());
        });
    }

    private void showFieldError(String message) {
        if (view != null && view.getErrorLabel() != null) {
            view.getErrorLabel().setText(message);
            view.getErrorLabel().setVisible(true);
        }
    }

    private void clearError() {
        if (view != null && view.getErrorLabel() != null) {
            view.getErrorLabel().setText("");
            view.getErrorLabel().setVisible(false);
        }
    }

    private void setFormEnabled(boolean enabled) {
        if (view != null) {
            if (view.getUsernameField() != null) {
                view.getUsernameField().setDisable(!enabled);
            }
            if (view.getPasswordField() != null) {
                view.getPasswordField().setDisable(!enabled);
            }
            if (view.getLoginButton() != null) {
                view.getLoginButton().setDisable(!enabled);
                view.getLoginButton().setText(enabled ? "Log In" : "Logging in...");
            }
        }
    }

    private void clearForm() {
        if (view != null) {
            if (view.getUsernameField() != null) {
                view.getUsernameField().clear();
            }
            if (view.getPasswordField() != null) {
                view.getPasswordField().clear();
            }
            clearError();
        }
    }
}