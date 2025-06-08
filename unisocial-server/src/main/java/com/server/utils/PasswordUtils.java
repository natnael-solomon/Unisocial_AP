package com.server.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using jBCrypt
 */
public class PasswordUtils {
    private static final int BCRYPT_ROUNDS = 12; // Higher rounds = more secure but slower

    /**
     * Hash a plain text password
     *
     * @param password The plain text password
     * @return The hashed password
     */
    public static String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verify a password against a hash
     *
     * @param password The plain text password
     * @param hash The stored hash
     * @return true if password matches hash, false otherwise
     */
    public static boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(password, hash);
        } catch (Exception e) {
            Logger.error("Error verifying password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a password meets minimum requirements
     *
     * @param password The password to check
     * @return true if password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }

        // Minimum 6 characters
        if (password.length() < 6) {
            return false;
        }

        // Maximum 72 characters (BCrypt limitation)
        if (password.length() > 72) {
            return false;
        }

        // Add more validation rules as needed
        // e.g., require uppercase, lowercase, numbers, special characters

        return true;
    }

    /**
     * Check if a password is strong (meets enhanced requirements)
     *
     * @param password The password to check
     * @return true if password is strong, false otherwise
     */
    public static boolean isStrongPassword(String password) {
        if (!isValidPassword(password)) {
            return false;
        }

        // Minimum 8 characters for strong password
        if (password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecial = true;
            }
        }

        // Require at least 3 of the 4 character types
        int typeCount = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) +
                (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);

        return typeCount >= 3;
    }

    /**
     * Generate a random password
     *
     * @param length The length of the password
     * @return A random password
     */
    public static String generateRandomPassword(int length) {
        if (length < 6) {
            length = 6;
        }
        if (length > 72) {
            length = 72;
        }

        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String allChars = upperCase + lowerCase + digits + special;

        StringBuilder password = new StringBuilder();

        // Ensure at least one character from each type
        password.append(upperCase.charAt((int) (Math.random() * upperCase.length())));
        password.append(lowerCase.charAt((int) (Math.random() * lowerCase.length())));
        password.append(digits.charAt((int) (Math.random() * digits.length())));
        password.append(special.charAt((int) (Math.random() * special.length())));

        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt((int) (Math.random() * allChars.length())));
        }

        // Shuffle the password
        return shuffleString(password.toString());
    }

    /**
     * Generate a secure random password with specified requirements
     *
     * @param length The length of the password
     * @param includeUpper Include uppercase letters
     * @param includeLower Include lowercase letters
     * @param includeDigits Include digits
     * @param includeSpecial Include special characters
     * @return A random password meeting the requirements
     */
    public static String generateRandomPassword(int length, boolean includeUpper,
                                                boolean includeLower, boolean includeDigits,
                                                boolean includeSpecial) {
        if (length < 6) {
            length = 6;
        }
        if (length > 72) {
            length = 72;
        }

        StringBuilder charSet = new StringBuilder();
        StringBuilder password = new StringBuilder();

        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";

        // Build character set and ensure at least one from each required type
        if (includeUpper) {
            charSet.append(upperCase);
            password.append(upperCase.charAt((int) (Math.random() * upperCase.length())));
        }
        if (includeLower) {
            charSet.append(lowerCase);
            password.append(lowerCase.charAt((int) (Math.random() * lowerCase.length())));
        }
        if (includeDigits) {
            charSet.append(digits);
            password.append(digits.charAt((int) (Math.random() * digits.length())));
        }
        if (includeSpecial) {
            charSet.append(special);
            password.append(special.charAt((int) (Math.random() * special.length())));
        }

        // If no character types selected, default to alphanumeric
        if (charSet.length() == 0) {
            charSet.append(upperCase).append(lowerCase).append(digits);
            password.append(upperCase.charAt((int) (Math.random() * upperCase.length())));
            password.append(lowerCase.charAt((int) (Math.random() * lowerCase.length())));
            password.append(digits.charAt((int) (Math.random() * digits.length())));
        }

        // Fill the rest randomly
        String allChars = charSet.toString();
        while (password.length() < length) {
            password.append(allChars.charAt((int) (Math.random() * allChars.length())));
        }

        // Shuffle the password
        return shuffleString(password.toString());
    }

    /**
     * Shuffle a string randomly
     *
     * @param input The input string
     * @return The shuffled string
     */
    private static String shuffleString(String input) {
        char[] chars = input.toCharArray();

        // Fisher-Yates shuffle
        for (int i = chars.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    /**
     * Get password strength score (0-100)
     *
     * @param password The password to evaluate
     * @return Strength score from 0 (very weak) to 100 (very strong)
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length scoring
        if (password.length() >= 8) score += 25;
        else if (password.length() >= 6) score += 15;
        else score += 5;

        // Character type scoring
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");

        if (hasUpper) score += 15;
        if (hasLower) score += 15;
        if (hasDigit) score += 15;
        if (hasSpecial) score += 15;

        // Bonus for length
        if (password.length() >= 12) score += 10;
        if (password.length() >= 16) score += 5;

        // Penalty for common patterns
        if (password.matches(".*123.*") || password.matches(".*abc.*") ||
                password.toLowerCase().contains("password")) {
            score -= 10;
        }

        return Math.min(100, Math.max(0, score));
    }

    /**
     * Get password strength description
     *
     * @param password The password to evaluate
     * @return Human-readable strength description
     */
    public static String getPasswordStrengthDescription(String password) {
        int strength = getPasswordStrength(password);

        if (strength >= 80) return "Very Strong";
        if (strength >= 60) return "Strong";
        if (strength >= 40) return "Moderate";
        if (strength >= 20) return "Weak";
        return "Very Weak";
    }
}
