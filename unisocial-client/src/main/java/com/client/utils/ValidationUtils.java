package com.client.utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    // Regex patterns for validation
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Constants
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int MAX_POST_LENGTH = 280;
    public static final int MAX_BIO_LENGTH = 160;
    public static final int MAX_NAME_LENGTH = 50;

    private ValidationUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Validate username format
     * @param username the username to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String trimmed = username.trim();
        return trimmed.length() >= MIN_USERNAME_LENGTH &&
                trimmed.length() <= MAX_USERNAME_LENGTH &&
                USERNAME_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validate password strength
     * @param password the password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return password != null &&
                password.length() >= MIN_PASSWORD_LENGTH &&
                password.length() <= MAX_PASSWORD_LENGTH;
    }

    /**
     * Validate strong password (with additional requirements)
     * @param password the password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isStrongPassword(String password) {
        if (!isValidPassword(password)) {
            return false;
        }

        // Check for at least one uppercase, one lowercase, one digit
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        return hasUpper && hasLower && hasDigit;
    }

    /**
     * Validate email format
     * @param email the email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return email != null &&
                !email.trim().isEmpty() &&
                EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate post content
     * @param content the post content to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPostContent(String content) {
        return content != null &&
                !content.trim().isEmpty() &&
                content.trim().length() <= MAX_POST_LENGTH;
    }

    /**
     * Validate user bio
     * @param bio the bio to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidBio(String bio) {
        if (bio == null) {
            return true; // Bio is optional
        }
        return bio.length() <= MAX_BIO_LENGTH;
    }

    /**
     * Validate full name
     * @param name the name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        String trimmed = name.trim();
        return trimmed.length() <= MAX_NAME_LENGTH &&
                trimmed.matches("^[a-zA-Z\\s'-]+$"); // Letters, spaces, apostrophes, hyphens
    }

    /**
     * Check if string is not null or empty
     * @param str the string to check
     * @return true if not null or empty, false otherwise
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Check if string is null or empty
     * @param str the string to check
     * @return true if null or empty, false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Validate URL format
     * @param url the URL to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            new java.net.URL(url);
            return true;
        } catch (java.net.MalformedURLException e) {
            return false;
        }
    }

    /**
     * Validate image file extension
     * @param filename the filename to validate
     * @return true if valid image extension, false otherwise
     */
    public static boolean isValidImageFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }

        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") ||
                lower.endsWith(".jpeg") ||
                lower.endsWith(".png") ||
                lower.endsWith(".gif") ||
                lower.endsWith(".bmp") ||
                lower.endsWith(".webp");
    }

    /**
     * Sanitize input string (remove potentially harmful characters)
     * @param input the input to sanitize
     * @return sanitized string
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        // Remove HTML tags and script content
        return input.replaceAll("<[^>]*>", "")
                .replaceAll("javascript:", "")
                .replaceAll("vbscript:", "")
                .replaceAll("onload", "")
                .replaceAll("onerror", "")
                .trim();
    }

    /**
     * Validate age (must be between 13 and 120)
     * @param age the age to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAge(int age) {
        return age >= 13 && age <= 120;
    }

    /**
     * Validate that two passwords match
     * @param password the original password
     * @param confirmPassword the confirmation password
     * @return true if they match, false otherwise
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null && confirmPassword == null) {
            return true;
        }
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Get username validation error message
     * @param username the username to validate
     * @return error message or null if valid
     */
    public static String getUsernameValidationError(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Username is required";
        }

        String trimmed = username.trim();
        if (trimmed.length() < MIN_USERNAME_LENGTH) {
            return "Username must be at least " + MIN_USERNAME_LENGTH + " characters";
        }
        if (trimmed.length() > MAX_USERNAME_LENGTH) {
            return "Username must be no more than " + MAX_USERNAME_LENGTH + " characters";
        }
        if (!USERNAME_PATTERN.matcher(trimmed).matches()) {
            return "Username can only contain letters, numbers, and underscores";
        }

        return null; // Valid
    }

    /**
     * Get password validation error message
     * @param password the password to validate
     * @return error message or null if valid
     */
    public static String getPasswordValidationError(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters";
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return "Password must be no more than " + MAX_PASSWORD_LENGTH + " characters";
        }

        return null; // Valid
    }

    /**
     * Get post content validation error message
     * @param content the content to validate
     * @return error message or null if valid
     */
    public static String getPostContentValidationError(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "Post content cannot be empty";
        }
        if (!isValidPostContent(content)) {
            return "Post content must be no more than " + MAX_POST_LENGTH + " characters";
        }

        return null; // Valid
    }

    /**
     * Validate form field and return error message
     * @param fieldName the name of the field
     * @param value the value to validate
     * @param fieldType the type of validation to perform
     * @return error message or null if valid
     */
    public static String validateField(String fieldName, String value, FieldType fieldType) {
        switch (fieldType) {
            case USERNAME:
                return getUsernameValidationError(value);
            case PASSWORD:
                return getPasswordValidationError(value);
            case EMAIL:
                if (!isValidEmail(value)) {
                    return fieldName + " must be a valid email address";
                }
                break;
            case REQUIRED:
                if (isEmpty(value)) {
                    return fieldName + " is required";
                }
                break;
            case POST_CONTENT:
                return getPostContentValidationError(value);
            case NAME:
                if (!isValidName(value)) {
                    return fieldName + " contains invalid characters";
                }
                break;
        }
        return null; // Valid
    }

    /**
     * Enum for field validation types
     */
    public enum FieldType {
        USERNAME,
        PASSWORD,
        EMAIL,
        REQUIRED,
        POST_CONTENT,
        NAME,
        PHONE,
        URL
    }

    /**
     * Validate multiple fields at once
     * @param validations array of field validations
     * @return first error message found, or null if all valid
     */
    public static String validateFields(FieldValidation... validations) {
        for (FieldValidation validation : validations) {
            String error = validateField(validation.fieldName, validation.value, validation.type);
            if (error != null) {
                return error;
            }
        }
        return null; // All valid
    }

    /**
     * Helper class for field validation
     */
    public static class FieldValidation {
        public final String fieldName;
        public final String value;
        public final FieldType type;

        public FieldValidation(String fieldName, String value, FieldType type) {
            this.fieldName = fieldName;
            this.value = value;
            this.type = type;
        }
    }
}