package com.client.controllers;

import com.client.events.AuthEvent;
import com.client.utils.ValidationUtils;
import com.client.views.SignupView;

import javafx.application.Platform;

public class SignupController extends BaseController {

    private final SignupView view;

    public SignupController(SignupView view) {
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
        eventBus.subscribe(AuthEvent.SignupSuccess.class, this::handleSignupSuccess);
        eventBus.subscribe(AuthEvent.SignupFailure.class, this::handleSignupFailure);
    }

    private void setupEventHandlers() {
        if (view.getSignupButton() != null) {
            view.getSignupButton().setOnAction(e -> handleSignup());
        }

        if (view.getConfirmPasswordField() != null) {
            view.getConfirmPasswordField().setOnAction(e -> handleSignup());
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
                validatePasswordMatch();
            });
        }

        // Real-time validation for confirm password field
        if (view.getConfirmPasswordField() != null) {
            view.getConfirmPasswordField().textProperty().addListener((obs, oldText, newText) -> {
                validatePasswordMatch();
                updateSignupButtonState();
            });
        }
    }

    private void validateUsernameField(String username) {
        String error = ValidationUtils.getUsernameValidationError(username);
        if (error != null && !ValidationUtils.isEmpty(username)) {
            showFieldError(error);
        } else {
            clearError();
        }
        updateSignupButtonState();
    }

    private void validatePasswordField(String password) {
        String error = ValidationUtils.getPasswordValidationError(password);
        if (error != null && !ValidationUtils.isEmpty(password)) {
            showFieldError(error);
        } else {
            clearError();
        }
        updateSignupButtonState();
    }

    private void validatePasswordMatch() {
        if (view == null) {
            return;
        }

        String password = view.getPasswordField().getText();
        String confirmPassword = view.getConfirmPasswordField().getText();

        if (!ValidationUtils.isEmpty(confirmPassword)
                && !ValidationUtils.passwordsMatch(password, confirmPassword)) {
            showFieldError("Passwords do not match");
        } else if (ValidationUtils.isEmpty(confirmPassword)
                || ValidationUtils.passwordsMatch(password, confirmPassword)) {
            clearError();
        }
        updateSignupButtonState();
    }

    private void updateSignupButtonState() {
        if (view != null && view.getSignupButton() != null) {
            String username = view.getUsernameField().getText();
            String password = view.getPasswordField().getText();
            String confirmPassword = view.getConfirmPasswordField().getText();

            boolean isValid = ValidationUtils.isValidUsername(username)
                    && ValidationUtils.isValidPassword(password)
                    && ValidationUtils.passwordsMatch(password, confirmPassword);

            view.getSignupButton().setDisable(!isValid);
        }
    }

    private void handleSignup() {
        if (view == null) {
            return;
        }

        String username = ValidationUtils.sanitizeInput(view.getUsernameField().getText().trim());
        String password = view.getPasswordField().getText();
        String confirmPassword = view.getConfirmPasswordField().getText();

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

        // Check password match
        if (!ValidationUtils.passwordsMatch(password, confirmPassword)) {
            showFieldError("Passwords do not match");
            return;
        }

        // Disable form during signup
        setFormEnabled(false);

        // Attempt signup
        authService.signup(username, password, confirmPassword);
    }

    private void handleSignupSuccess(AuthEvent.SignupSuccess event) {
        Platform.runLater(() -> {
            setFormEnabled(true);
            clearForm();
        });
    }

    private void handleSignupFailure(AuthEvent.SignupFailure event) {
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
            if (view.getConfirmPasswordField() != null) {
                view.getConfirmPasswordField().setDisable(!enabled);
            }
            if (view.getSignupButton() != null) {
                view.getSignupButton().setDisable(!enabled);
                view.getSignupButton().setText(enabled ? "Sign Up" : "Signing up...");
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
            if (view.getConfirmPasswordField() != null) {
                view.getConfirmPasswordField().clear();
            }
            clearError();
        }
    }
}
