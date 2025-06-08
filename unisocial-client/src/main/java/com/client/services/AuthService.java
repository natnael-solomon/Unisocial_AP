package com.client.services;

import com.client.core.AppState;
import com.client.core.EventBus;
import com.client.events.AuthEvent;
import com.client.exceptions.AuthException;
import com.client.models.AuthResult;
import com.client.models.User;
import com.client.utils.ValidationUtils;
import java.util.concurrent.CompletableFuture;

public class AuthService {
    private final NetworkService networkService;
    private final EventBus eventBus;
    private final AppState appState;

    public AuthService(NetworkService networkService, EventBus eventBus) {
        this.networkService = networkService;
        this.eventBus = eventBus;
        this.appState = AppState.getInstance();
    }

    public void login(String username, String password) {
        CompletableFuture.supplyAsync(() -> {
            try {
                // Validate input using ValidationUtils
                String validationError = ValidationUtils.validateFields(
                        new ValidationUtils.FieldValidation("Username", username, ValidationUtils.FieldType.USERNAME),
                        new ValidationUtils.FieldValidation("Password", password, ValidationUtils.FieldType.PASSWORD)
                );

                if (validationError != null) {
                    throw new AuthException(validationError);
                }

                // Sanitize input
                String sanitizedUsername = ValidationUtils.sanitizeInput(username.trim());

                // Additional password validation
                if (!ValidationUtils.isValidPassword(password)) {
                    throw new AuthException("Password must be at least " + ValidationUtils.MIN_PASSWORD_LENGTH + " characters long");
                }

                // Publish login attempt event
                eventBus.publish(new AuthEvent.LoginAttempt(sanitizedUsername));

                // Perform network login
                User user = networkService.login(sanitizedUsername, password);

                if (user != null) {
                    // Validate and sanitize user data from server
                    user = validateAndSanitizeUser(user);

                    appState.setCurrentUser(user);
                    eventBus.publish(new AuthEvent.LoginSuccess(user));
                    return new AuthResult(true, "Login successful", user);
                } else {
                    eventBus.publish(new AuthEvent.LoginFailure("Invalid credentials"));
                    return new AuthResult(false, "Invalid username or password", null);
                }

            } catch (AuthException e) {
                String sanitizedError = ValidationUtils.sanitizeInput(e.getMessage());
                eventBus.publish(new AuthEvent.LoginFailure(sanitizedError));
                return new AuthResult(false, sanitizedError, null);
            } catch (Exception e) {
                String errorMsg = "Login failed: " + ValidationUtils.sanitizeInput(e.getMessage());
                eventBus.publish(new AuthEvent.LoginFailure(errorMsg));
                return new AuthResult(false, errorMsg, null);
            }
        });
    }

    public void signup(String username, String password, String confirmPassword) {
        CompletableFuture.supplyAsync(() -> {
            try {
                // Comprehensive validation using ValidationUtils
                String validationError = validateSignupInput(username, password, confirmPassword);
                if (validationError != null) {
                    throw new AuthException(validationError);
                }

                // Sanitize input
                String sanitizedUsername = ValidationUtils.sanitizeInput(username.trim());

                // Publish signup attempt event
                eventBus.publish(new AuthEvent.SignupAttempt(sanitizedUsername));

                // Perform network signup
                User user = networkService.signup(sanitizedUsername, password);

                if (user != null) {
                    // Validate and sanitize user data from server
                    user = validateAndSanitizeUser(user);

                    appState.setCurrentUser(user);
                    eventBus.publish(new AuthEvent.SignupSuccess(user));
                    return new AuthResult(true, "Account created successfully", user);
                } else {
                    eventBus.publish(new AuthEvent.SignupFailure("Failed to create account"));
                    return new AuthResult(false, "Failed to create account", null);
                }

            } catch (AuthException e) {
                String sanitizedError = ValidationUtils.sanitizeInput(e.getMessage());
                eventBus.publish(new AuthEvent.SignupFailure(sanitizedError));
                return new AuthResult(false, sanitizedError, null);
            } catch (Exception e) {
                String errorMsg = "Signup failed: " + ValidationUtils.sanitizeInput(e.getMessage());
                eventBus.publish(new AuthEvent.SignupFailure(errorMsg));
                return new AuthResult(false, errorMsg, null);
            }
        });
    }

    public void logout() {
        User currentUser = appState.getCurrentUser();
        if (currentUser != null) {
            networkService.logout();
            appState.setCurrentUser(null);
            eventBus.publish(new AuthEvent.Logout(currentUser));
        }
    }

    /**
     * Validate signup input using ValidationUtils
     */
    private String validateSignupInput(String username, String password, String confirmPassword) {
        // Validate individual fields
        String validationError = ValidationUtils.validateFields(
                new ValidationUtils.FieldValidation("Username", username, ValidationUtils.FieldType.USERNAME),
                new ValidationUtils.FieldValidation("Password", password, ValidationUtils.FieldType.PASSWORD)
        );

        if (validationError != null) {
            return validationError;
        }

        // Check password confirmation
        if (!ValidationUtils.passwordsMatch(password, confirmPassword)) {
            return "Passwords do not match";
        }

        // Additional username validation
        String usernameError = ValidationUtils.getUsernameValidationError(username);
        if (usernameError != null) {
            return usernameError;
        }

        // Additional password validation
        String passwordError = ValidationUtils.getPasswordValidationError(password);
        if (passwordError != null) {
            return passwordError;
        }

        return null; // All validations passed
    }

    /**
     * Validate and sanitize user data received from server
     */
    private User validateAndSanitizeUser(User user) throws AuthException {
        if (user == null) {
            throw new AuthException("Invalid user data received from server");
        }

        // Validate required fields
        if (ValidationUtils.isEmpty(user.getUsername())) {
            throw new AuthException("Invalid username received from server");
        }

        // Sanitize user data
        user.setUsername(ValidationUtils.sanitizeInput(user.getUsername()));

        if (user.getFullName() != null) {
            user.setFullName(ValidationUtils.sanitizeInput(user.getFullName()));

            // Validate full name if provided
            if (ValidationUtils.isNotEmpty(user.getFullName()) &&
                    !ValidationUtils.isValidName(user.getFullName())) {
                user.setFullName("User"); // Set default if invalid
            }
        }

        if (user.getBio() != null) {
            user.setBio(ValidationUtils.sanitizeInput(user.getBio()));

            // Validate bio length
            if (!ValidationUtils.isValidBio(user.getBio())) {
                user.setBio(user.getBio().substring(0, Math.min(user.getBio().length(), ValidationUtils.MAX_BIO_LENGTH)));
            }
        }

        // Ensure non-negative follower counts
        user.setFollowingCount(Math.max(0, user.getFollowingCount()));
        user.setFollowersCount(Math.max(0, user.getFollowersCount()));

        return user;
    }

    /**
     * Check if current user is logged in
     */
    public boolean isLoggedIn() {
        return appState.getCurrentUser() != null;
    }

    /**
     * Get current logged in user
     */
    public User getCurrentUser() {
        return appState.getCurrentUser();
    }

    /**
     * Validate user session
     */
    public boolean isValidSession() {
        User currentUser = appState.getCurrentUser();
        return currentUser != null &&
                ValidationUtils.isNotEmpty(currentUser.getUsername()) &&
                ValidationUtils.isValidUsername(currentUser.getUsername());
    }

//    /**
//     * Update current user profile with validation
//     */
//    public void updateProfile(String fullName, String bio) {
//        User currentUser = appState.getCurrentUser();
//        if (currentUser == null) {
//            eventBus.publish(new AuthEvent.ProfileUpdateFailure("No user logged in"));
//            return;
//        }
//
//        CompletableFuture.supplyAsync(() -> {
//            try {
//                // Validate input
//                if (ValidationUtils.isNotEmpty(fullName) && !ValidationUtils.isValidName(fullName)) {
//                    throw new AuthException("Invalid full name format");
//                }
//
//                if (!ValidationUtils.isValidBio(bio)) {
//                    throw new AuthException("Bio is too long (max " + ValidationUtils.MAX_BIO_LENGTH + " characters)");
//                }
//
//                // Sanitize input
//                String sanitizedFullName = ValidationUtils.sanitizeInput(fullName);
//                String sanitizedBio = ValidationUtils.sanitizeInput(bio);
//
//                // Update user object
//                currentUser.setFullName(sanitizedFullName);
//                currentUser.setBio(sanitizedBio);
//
//                // Update in network service (if implemented)
//                boolean success = networkService.updateProfile(currentUser);
//
//                if (success) {
//                    appState.setCurrentUser(currentUser);
//                    eventBus.publish(new AuthEvent.ProfileUpdateSuccess(currentUser));
//                    return new AuthResult(true, "Profile updated successfully", currentUser);
//                } else {
//                    throw new AuthException("Failed to update profile on server");
//                }
//
//            } catch (AuthException e) {
//                String sanitizedError = ValidationUtils.sanitizeInput(e.getMessage());
//                eventBus.publish(new AuthEvent.ProfileUpdateFailure(sanitizedError));
//                return new AuthResult(false, sanitizedError, null);
//            } catch (Exception e) {
//                String errorMsg = "Profile update failed: " + ValidationUtils.sanitizeInput(e.getMessage());
//                eventBus.publish(new AuthEvent.ProfileUpdateFailure(errorMsg));
//                return new AuthResult(false, errorMsg, null);
//            }
//        });
//    }
}