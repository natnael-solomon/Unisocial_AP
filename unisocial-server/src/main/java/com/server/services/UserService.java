package com.server.services;

import com.server.DatabaseManager;
import com.server.models.User;
import com.server.utils.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing users and user relationships
 */
public class UserService {
    private final DatabaseManager databaseManager;
    private final String uploadDirectory;

    public UserService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.uploadDirectory = "uploads/avatars/";

        // Create upload directory if it doesn't exist
        createUploadDirectory();
    }

    public UserService(DatabaseManager databaseManager, String uploadDirectory) {
        this.databaseManager = databaseManager;
        this.uploadDirectory = uploadDirectory;
        createUploadDirectory();
    }

    private void createUploadDirectory() {
        File dir = new File(uploadDirectory);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                Logger.info("Created upload directory: " + uploadDirectory);
            } else {
                Logger.error("Failed to create upload directory: " + uploadDirectory);
            }
        }
    }

    /**
     * Get user by ID
     *
     * @param userId The user ID
     * @return User object if found, null otherwise
     */
    public User getUserById(int userId) {
        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                SELECT u.id, u.username, u.full_name, u.bio, u.avatar_url, u.created_at, u.updated_at,
                       (SELECT COUNT(*) FROM follows f WHERE f.follower_id = u.id) as following_count,
                       (SELECT COUNT(*) FROM follows f WHERE f.followee_id = u.id) as followers_count
                FROM users u
                WHERE u.id = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setFullName(rs.getString("full_name"));
                        user.setBio(rs.getString("bio"));
                        user.setAvatarUrl(rs.getString("avatar_url"));
                        user.setFollowingCount(rs.getInt("following_count"));
                        user.setFollowersCount(rs.getInt("followers_count"));
                        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
                        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime().toString());

                        return user;
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error getting user by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get user by username
     *
     * @param username The username
     * @return User object if found, null otherwise
     */
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                SELECT u.id, u.username, u.full_name, u.bio, u.avatar_url, u.created_at, u.updated_at,
                       (SELECT COUNT(*) FROM follows f WHERE f.follower_id = u.id) as following_count,
                       (SELECT COUNT(*) FROM follows f WHERE f.followee_id = u.id) as followers_count
                FROM users u
                WHERE u.username = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username.trim());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setFullName(rs.getString("full_name"));
                        user.setBio(rs.getString("bio"));
                        user.setAvatarUrl(rs.getString("avatar_url"));
                        user.setFollowingCount(rs.getInt("following_count"));
                        user.setFollowersCount(rs.getInt("followers_count"));
                        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
                        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime().toString());

                        return user;
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error getting user by username: " + e.getMessage());
        }

        return null;
    }

    /**
     * Update user profile
     *
     * @param userId The user ID
     * @param fullName The new full name (can be null to keep current)
     * @param bio The new bio (can be null to keep current)
     * @param avatarUrl The new avatar URL (can be null to keep current)
     * @return true if profile updated successfully, false otherwise
     */
    public boolean updateProfile(int userId, String fullName, String bio, String avatarUrl) {
        try (Connection conn = databaseManager.getConnection()) {
            StringBuilder sqlBuilder = new StringBuilder("UPDATE users SET updated_at = CURRENT_TIMESTAMP");
            List<Object> parameters = new ArrayList<>();

            if (fullName != null) {
                sqlBuilder.append(", full_name = ?");
                parameters.add(fullName.trim());
            }

            if (bio != null) {
                sqlBuilder.append(", bio = ?");
                parameters.add(bio.trim());
            }

            if (avatarUrl != null) {
                sqlBuilder.append(", avatar_url = ?");
                parameters.add(avatarUrl);
            }

            sqlBuilder.append(" WHERE id = ?");
            parameters.add(userId);

            try (PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    stmt.setObject(i + 1, parameters.get(i));
                }

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    Logger.info("Profile updated for user ID: " + userId);
                    return true;
                }
            }

        } catch (SQLException e) {
            Logger.error("Error updating profile: " + e.getMessage());
        }

        return false;
    }

    /**
     * Search users by username or full name
     *
     * @param query The search query
     * @param limit Maximum number of results
     * @return List of matching users
     */
    public List<User> searchUsers(String query, int limit) {
        List<User> users = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return users;
        }

        String searchTerm = "%" + query.trim().toLowerCase() + "%";

        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                SELECT u.id, u.username, u.full_name, u.bio, u.avatar_url, u.created_at, u.updated_at,
                       (SELECT COUNT(*) FROM follows f WHERE f.follower_id = u.id) as following_count,
                       (SELECT COUNT(*) FROM follows f WHERE f.followee_id = u.id) as followers_count
                FROM users u
                WHERE LOWER(u.username) LIKE ? OR LOWER(u.full_name) LIKE ?
                ORDER BY u.username
                LIMIT ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, searchTerm);
                stmt.setString(2, searchTerm);
                stmt.setInt(3, Math.min(limit, 50)); // Cap at 50 results

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setFullName(rs.getString("full_name"));
                        user.setBio(rs.getString("bio"));
                        user.setAvatarUrl(rs.getString("avatar_url"));
                        user.setFollowingCount(rs.getInt("following_count"));
                        user.setFollowersCount(rs.getInt("followers_count"));
                        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
                        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime().toString());

                        users.add(user);
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error searching users: " + e.getMessage());
        }

        Logger.info("User search for '" + query + "' returned " + users.size() + " results");
        return users;
    }

    /**
     * Toggle follow relationship between users
     *
     * @param followerId The user ID doing the following
     * @param followeeId The user ID being followed
     * @return true if operation successful, false otherwise
     */
    public boolean toggleFollow(int followerId, int followeeId) {
        if (followerId == followeeId) {
            return false; // Can't follow yourself
        }

        try (Connection conn = databaseManager.getConnection()) {
            // Check if already following
            String checkSql = "SELECT COUNT(*) FROM follows WHERE follower_id = ? AND followee_id = ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, followerId);
                checkStmt.setInt(2, followeeId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // Unfollow
                        String deleteSql = "DELETE FROM follows WHERE follower_id = ? AND followee_id = ?";

                        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                            deleteStmt.setInt(1, followerId);
                            deleteStmt.setInt(2, followeeId);

                            int affectedRows = deleteStmt.executeUpdate();

                            if (affectedRows > 0) {
                                Logger.info("User " + followerId + " unfollowed user " + followeeId);
                                return true;
                            }
                        }
                    } else {
                        // Follow
                        String insertSql = "INSERT INTO follows (follower_id, followee_id, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)";

                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, followerId);
                            insertStmt.setInt(2, followeeId);

                            int affectedRows = insertStmt.executeUpdate();

                            if (affectedRows > 0) {
                                Logger.info("User " + followerId + " followed user " + followeeId);
                                return true;
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error toggling follow: " + e.getMessage());
        }

        return false;
    }

    /**
     * Check if one user is following another
     *
     * @param followerId The follower user ID
     * @param followeeId The followee user ID
     * @return true if following, false otherwise
     */
    public boolean isFollowing(int followerId, int followeeId) {
        try (Connection conn = databaseManager.getConnection()) {
            String sql = "SELECT COUNT(*) FROM follows WHERE follower_id = ? AND followee_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, followerId);
                stmt.setInt(2, followeeId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error checking follow status: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get followers of a user
     *
     * @param userId The user ID
     * @param limit Maximum number of results
     * @return List of followers
     */
    public List<User> getFollowers(int userId, int limit) {
        List<User> followers = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                SELECT u.id, u.username, u.full_name, u.bio, u.avatar_url, u.created_at, u.updated_at,
                       (SELECT COUNT(*) FROM follows f WHERE f.follower_id = u.id) as following_count,
                       (SELECT COUNT(*) FROM follows f WHERE f.followee_id = u.id) as followers_count
                FROM users u
                JOIN follows f ON u.id = f.follower_id
                WHERE f.followee_id = ?
                ORDER BY f.created_at DESC
                LIMIT ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, Math.min(limit, 100));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setFullName(rs.getString("full_name"));
                        user.setBio(rs.getString("bio"));
                        user.setAvatarUrl(rs.getString("avatar_url"));
                        user.setFollowingCount(rs.getInt("following_count"));
                        user.setFollowersCount(rs.getInt("followers_count"));
                        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
                        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime().toString());

                        followers.add(user);
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error getting followers: " + e.getMessage());
        }

        return followers;
    }

    /**
     * Get users that a user is following
     *
     * @param userId The user ID
     * @param limit Maximum number of results
     * @return List of users being followed
     */
    public List<User> getFollowing(int userId, int limit) {
        List<User> following = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                SELECT u.id, u.username, u.full_name, u.bio, u.avatar_url, u.created_at, u.updated_at,
                       (SELECT COUNT(*) FROM follows f WHERE f.follower_id = u.id) as following_count,
                       (SELECT COUNT(*) FROM follows f WHERE f.followee_id = u.id) as followers_count
                FROM users u
                JOIN follows f ON u.id = f.followee_id
                WHERE f.follower_id = ?
                ORDER BY f.created_at DESC
                LIMIT ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, Math.min(limit, 100));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setFullName(rs.getString("full_name"));
                        user.setBio(rs.getString("bio"));
                        user.setAvatarUrl(rs.getString("avatar_url"));
                        user.setFollowingCount(rs.getInt("following_count"));
                        user.setFollowersCount(rs.getInt("followers_count"));
                        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
                        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime().toString());

                        following.add(user);
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error getting following: " + e.getMessage());
        }

        return following;
    }

    /**
     * Get avatar URL for a user
     *
     * @param userId The user ID
     * @return Avatar URL if found, null otherwise
     */
    public String getAvatarUrl(int userId) {
        try (Connection conn = databaseManager.getConnection()) {
            String sql = "SELECT avatar_url FROM users WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("avatar_url");
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error getting avatar URL: " + e.getMessage());
        }

        return null;
    }

    /**
     * Update user avatar
     *
     * @param userId The user ID
     * @param avatarData Base64 encoded avatar data
     * @param contentType The content type of the image
     * @return The URL of the uploaded avatar, or null if failed
     */
    public String updateAvatar(int userId, String avatarData, String contentType) {
        try {
            // Decode base64 data
            byte[] imageBytes = Base64.getDecoder().decode(avatarData);

            // Validate file size (max 5MB)
            if (imageBytes.length > 5 * 1024 * 1024) {
                Logger.warn("Avatar file too large for user " + userId + ": " + imageBytes.length + " bytes");
                return null;
            }

            // Generate unique filename
            String fileExtension = getFileExtension(contentType);
            String filename = "avatar_" + userId + "_" + UUID.randomUUID().toString() + fileExtension;
            String filePath = uploadDirectory + filename;

            // Save file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(imageBytes);
            }

            // Update database
            String avatarUrl = "/avatars/" + filename;
            if (updateProfile(userId, null, null, avatarUrl)) {
                Logger.info("Avatar updated for user " + userId + ": " + avatarUrl);
                return avatarUrl;
            } else {
                // Delete file if database update failed
                new File(filePath).delete();
            }

        } catch (Exception e) {
            Logger.error("Error updating avatar: " + e.getMessage());
        }

        return null;
    }

    /**
     * Delete user avatar
     *
     * @param userId The user ID
     * @return true if avatar deleted successfully, false otherwise
     */
    public boolean deleteAvatar(int userId) {
        try {
            // Get current avatar URL
            String currentAvatarUrl = getAvatarUrl(userId);

            // Update database to remove avatar URL
            if (updateProfile(userId, null, null, null)) {
                // Delete file if it exists
                if (currentAvatarUrl != null && currentAvatarUrl.startsWith("/avatars/")) {
                    String filename = currentAvatarUrl.substring("/avatars/".length());
                    File avatarFile = new File(uploadDirectory + filename);
                    if (avatarFile.exists()) {
                        boolean deleted = avatarFile.delete();
                        if (deleted) {
                            Logger.info("Avatar file deleted for user " + userId);
                        } else {
                            Logger.warn("Failed to delete avatar file for user " + userId);
                        }
                    }
                }

                Logger.info("Avatar deleted for user " + userId);
                return true;
            }

        } catch (Exception e) {
            Logger.error("Error deleting avatar: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get file extension from content type
     */
    private String getFileExtension(String contentType) {
        if (contentType == null) return ".jpg";

        switch (contentType.toLowerCase()) {
            case "image/jpeg":
            case "image/jpg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/gif":
                return ".gif";
            case "image/webp":
                return ".webp";
            default:
                return ".jpg";
        }
    }

    /**
     * Get user statistics
     *
     * @param userId The user ID
     * @return Array with [postCount, followersCount, followingCount]
     */
    public int[] getUserStats(int userId) {
        int[] stats = new int[3]; // [posts, followers, following]

        try (Connection conn = databaseManager.getConnection()) {
            // Get post count
            String postSql = "SELECT COUNT(*) FROM posts WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(postSql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats[0] = rs.getInt(1);
                    }
                }
            }

            // Get followers count
            String followersSql = "SELECT COUNT(*) FROM follows WHERE followee_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(followersSql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats[1] = rs.getInt(1);
                    }
                }
            }

            // Get following count
            String followingSql = "SELECT COUNT(*) FROM follows WHERE follower_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(followingSql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats[2] = rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error getting user stats: " + e.getMessage());
        }

        return stats;
    }
}
