package com.server.services;

import com.server.DatabaseManager;
import com.server.models.User;
import com.server.utils.Logger;
import com.server.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Authentication service for user login and registration
 */
public class AuthService {
    private final DatabaseManager databaseManager;

    public AuthService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Authenticate user with username and password
     *
     * @param username The username
     * @param password The plain text password
     * @return User object if authentication successful, null otherwise
     */
    public User authenticate(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) {
            return null;
        }

        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                SELECT id, username, password_hash, full_name, bio, avatar_url, created_at
                FROM users 
                WHERE username = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username.trim());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password_hash");

                        // Verify password
                        if (PasswordUtils.verifyPassword(password, storedHash)) {
                            User user = new User();
                            user.setId(rs.getInt("id"));
                            user.setUsername(rs.getString("username"));
                            user.setFullName(rs.getString("full_name"));
                            user.setBio(rs.getString("bio"));
                            user.setAvatarUrl(rs.getString("avatar_url"));
                            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());

                            Logger.info("User authenticated: " + username);
                            return user;
                        }
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error authenticating user: " + e.getMessage());
        }

        Logger.warn("Authentication failed for user: " + username);
        return null;
    }

    /**
     * Create a new user account
     *
     * @param username The username
     * @param password The plain text password
     * @return User object if creation successful, null otherwise
     */
    public User createUser(String username, String password) {
        if (username == null || password == null ||
                username.trim().isEmpty() || password.length() < 6) {
            return null;
        }

        username = username.trim();

        // Check if username already exists
        if (userExists(username)) {
            Logger.warn("Attempted to create user with existing username: " + username);
            return null;
        }

        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                INSERT INTO users (username, password_hash, full_name, created_at, updated_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;

            String passwordHash = PasswordUtils.hashPassword(password);

            try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, username);
                stmt.setString(2, passwordHash);
                stmt.setString(3, username); // Default full name to username

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int userId = generatedKeys.getInt(1);

                            // Create and return user object
                            User user = new User();
                            user.setId(userId);
                            user.setUsername(username);
                            user.setFullName(username);
                            user.setBio("New user on UniSocial!");
                            user.setAvatarUrl(null);

                            Logger.info("User created: " + username + " (ID: " + userId + ")");
                            return user;
                        }
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error creating user: " + e.getMessage());
        }

        return null;
    }

    /**
     * Check if a username already exists
     *
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    public boolean userExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        try (Connection conn = databaseManager.getConnection()) {
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username.trim());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error checking if user exists: " + e.getMessage());
        }

        return false;
    }

    /**
     * Change user password
     *
     * @param userId The user ID
     * @param oldPassword The current password
     * @param newPassword The new password
     * @return true if password changed successfully, false otherwise
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            return false;
        }

        try (Connection conn = databaseManager.getConnection()) {
            // First verify old password
            String selectSql = "SELECT password_hash FROM users WHERE id = ?";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, userId);

                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        String currentHash = rs.getString("password_hash");

                        if (!PasswordUtils.verifyPassword(oldPassword, currentHash)) {
                            Logger.warn("Password change failed - incorrect old password for user ID: " + userId);
                            return false;
                        }
                    } else {
                        return false; // User not found
                    }
                }
            }

            // Update password
            String updateSql = "UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            String newPasswordHash = PasswordUtils.hashPassword(newPassword);

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newPasswordHash);
                updateStmt.setInt(2, userId);

                int affectedRows = updateStmt.executeUpdate();

                if (affectedRows > 0) {
                    Logger.info("Password changed for user ID: " + userId);
                    return true;
                }
            }

        } catch (SQLException e) {
            Logger.error("Error changing password: " + e.getMessage());
        }

        return false;
    }

    /**
     * Reset password (admin function)
     *
     * @param username The username
     * @param newPassword The new password
     * @return true if password reset successfully, false otherwise
     */
    public boolean resetPassword(String username, String newPassword) {
        if (username == null || newPassword == null ||
                username.trim().isEmpty() || newPassword.length() < 6) {
            return false;
        }

        try (Connection conn = databaseManager.getConnection()) {
            String sql = "UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE username = ?";
            String passwordHash = PasswordUtils.hashPassword(newPassword);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, passwordHash);
                stmt.setString(2, username.trim());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    Logger.info("Password reset for user: " + username);
                    return true;
                }
            }

        } catch (SQLException e) {
            Logger.error("Error resetting password: " + e.getMessage());
        }

        return false;
    }

    /**
     * Delete user account
     *
     * @param userId The user ID
     * @param password The user's password for confirmation
     * @return true if account deleted successfully, false otherwise
     */
    public boolean deleteAccount(int userId, String password) {
        try (Connection conn = databaseManager.getConnection()) {
            // First verify password
            String selectSql = "SELECT password_hash FROM users WHERE id = ?";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, userId);

                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password_hash");

                        if (!PasswordUtils.verifyPassword(password, storedHash)) {
                            Logger.warn("Account deletion failed - incorrect password for user ID: " + userId);
                            return false;
                        }
                    } else {
                        return false; // User not found
                    }
                }
            }

            // Delete user (cascade will handle related records)
            String deleteSql = "DELETE FROM users WHERE id = ?";

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, userId);

                int affectedRows = deleteStmt.executeUpdate();

                if (affectedRows > 0) {
                    Logger.info("Account deleted for user ID: " + userId);
                    return true;
                }
            }

        } catch (SQLException e) {
            Logger.error("Error deleting account: " + e.getMessage());
        }

        return false;
    }
}
