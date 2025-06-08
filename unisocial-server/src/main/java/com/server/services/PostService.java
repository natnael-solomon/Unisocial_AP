package com.server.services;

import com.server.DatabaseManager;
import com.server.models.Post;
import com.server.utils.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing posts
 */
public class PostService {
    private final DatabaseManager databaseManager;

    public PostService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Create a new post
     *
     * @param userId The user ID creating the post
     * @param content The post content
     * @return true if post created successfully, false otherwise
     */
    public boolean createPost(int userId, String content) {
        if (content == null || content.trim().isEmpty() || content.length() > 500) {
            return false;
        }

        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                INSERT INTO posts (user_id, content, created_at, updated_at)
                VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, content.trim());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    Logger.info("Post created by user ID: " + userId);
                    return true;
                }
            }

        } catch (SQLException e) {
            Logger.error("Error creating post: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get feed for a user (posts from followed users + own posts)
     *
     * @param userId The user ID requesting the feed
     * @return List of posts in the feed
     */
    public List<Post> getFeed(int userId) {
        List<Post> posts = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                SELECT DISTINCT p.id, p.user_id, u.username, p.content, p.image_url, 
                       p.created_at, p.updated_at,
                       (SELECT COUNT(*) FROM likes l WHERE l.post_id = p.id) as like_count,
                       (SELECT COUNT(*) FROM likes l WHERE l.post_id = p.id AND l.user_id = ?) as user_liked,
                       (SELECT COUNT(*) FROM bookmarks b WHERE b.post_id = p.id AND b.user_id = ?) as user_bookmarked
                FROM posts p
                JOIN users u ON p.user_id = u.id
                LEFT JOIN follows f ON p.user_id = f.followee_id
                WHERE p.user_id = ? OR f.follower_id = ?
                ORDER BY p.created_at DESC
                LIMIT 50
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, userId);
                stmt.setInt(3, userId);
                stmt.setInt(4, userId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Post post = new Post();
                        post.setId(rs.getInt("id"));
                        post.setUserId(rs.getInt("user_id"));
                        post.setUsername(rs.getString("username"));
                        post.setContent(rs.getString("content"));
                        post.setImageUrl(rs.getString("image_url"));
                        post.setLikeCount(rs.getInt("like_count"));
                        post.setLiked(rs.getInt("user_liked") > 0);
                        post.setBookmarked(rs.getInt("user_bookmarked") > 0);
                        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        post.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

                        posts.add(post);
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error getting feed: " + e.getMessage());
        }

        Logger.info("Feed loaded for user ID " + userId + " with " + posts.size() + " posts");
        return posts;
    }

    /**
     * Get posts by a specific user
     *
     * @param userId The user ID whose posts to retrieve
     * @param requestingUserId The user ID making the request (for like/bookmark status)
     * @return List of user's posts
     */
    public List<Post> getUserPosts(int userId, int requestingUserId) {
        List<Post> posts = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                SELECT p.id, p.user_id, u.username, p.content, p.image_url, 
                       p.created_at, p.updated_at,
                       (SELECT COUNT(*) FROM likes l WHERE l.post_id = p.id) as like_count,
                       (SELECT COUNT(*) FROM likes l WHERE l.post_id = p.id AND l.user_id = ?) as user_liked,
                       (SELECT COUNT(*) FROM bookmarks b WHERE b.post_id = p.id AND b.user_id = ?) as user_bookmarked
                FROM posts p
                JOIN users u ON p.user_id = u.id
                WHERE p.user_id = ?
                ORDER BY p.created_at DESC
                LIMIT 50
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, requestingUserId);
                stmt.setInt(2, requestingUserId);
                stmt.setInt(3, userId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Post post = new Post();
                        post.setId(rs.getInt("id"));
                        post.setUserId(rs.getInt("user_id"));
                        post.setUsername(rs.getString("username"));
                        post.setContent(rs.getString("content"));
                        post.setImageUrl(rs.getString("image_url"));
                        post.setLikeCount(rs.getInt("like_count"));
                        post.setLiked(rs.getInt("user_liked") > 0);
                        post.setBookmarked(rs.getInt("user_bookmarked") > 0);
                        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        post.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

                        posts.add(post);
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error getting user posts: " + e.getMessage());
        }

        return posts;
    }

    /**
     * Toggle like on a post
     *
     * @param userId The user ID toggling the like
     * @param postId The post ID to like/unlike
     * @return true if operation successful, false otherwise
     */
    public boolean toggleLike(int userId, int postId) {
        try (Connection conn = databaseManager.getConnection()) {
            // Check if user already liked the post
            String checkSql = "SELECT COUNT(*) FROM likes WHERE user_id = ? AND post_id = ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, postId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // Unlike the post
                        String deleteSql = "DELETE FROM likes WHERE user_id = ? AND post_id = ?";

                        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                            deleteStmt.setInt(1, userId);
                            deleteStmt.setInt(2, postId);

                            int affectedRows = deleteStmt.executeUpdate();

                            if (affectedRows > 0) {
                                Logger.info("Post " + postId + " unliked by user " + userId);
                                return true;
                            }
                        }
                    } else {
                        // Like the post
                        String insertSql = "INSERT INTO likes (user_id, post_id, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)";

                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, userId);
                            insertStmt.setInt(2, postId);

                            int affectedRows = insertStmt.executeUpdate();

                            if (affectedRows > 0) {
                                Logger.info("Post " + postId + " liked by user " + userId);
                                return true;
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error toggling like: " + e.getMessage());
        }

        return false;
    }

    /**
     * Toggle bookmark on a post
     *
     * @param userId The user ID toggling the bookmark
     * @param postId The post ID to bookmark/unbookmark
     * @return true if operation successful, false otherwise
     */
    public boolean toggleBookmark(int userId, int postId) {
        try (Connection conn = databaseManager.getConnection()) {
            // Check if user already bookmarked the post
            String checkSql = "SELECT COUNT(*) FROM bookmarks WHERE user_id = ? AND post_id = ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, postId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // Remove bookmark
                        String deleteSql = "DELETE FROM bookmarks WHERE user_id = ? AND post_id = ?";

                        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                            deleteStmt.setInt(1, userId);
                            deleteStmt.setInt(2, postId);

                            int affectedRows = deleteStmt.executeUpdate();

                            if (affectedRows > 0) {
                                Logger.info("Post " + postId + " unbookmarked by user " + userId);
                                return true;
                            }
                        }
                    } else {
                        // Add bookmark
                        String insertSql = "INSERT INTO bookmarks (user_id, post_id, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)";

                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, userId);
                            insertStmt.setInt(2, postId);

                            int affectedRows = insertStmt.executeUpdate();

                            if (affectedRows > 0) {
                                Logger.info("Post " + postId + " bookmarked by user " + userId);
                                return true;
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error toggling bookmark: " + e.getMessage());
        }

        return false;
    }

    /**
     * Delete a post
     *
     * @param userId The user ID requesting deletion
     * @param postId The post ID to delete
     * @return true if post deleted successfully, false otherwise
     */
    public boolean deletePost(int userId, int postId) {
        try (Connection conn = databaseManager.getConnection()) {
            // Check if user owns the post
            String checkSql = "SELECT user_id FROM posts WHERE id = ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, postId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        int postOwnerId = rs.getInt("user_id");

                        if (postOwnerId != userId) {
                            Logger.warn("User " + userId + " attempted to delete post " + postId + " owned by user " + postOwnerId);
                            return false;
                        }
                    } else {
                        Logger.warn("Post " + postId + " not found for deletion");
                        return false;
                    }
                }
            }

            // Delete the post (cascade will handle likes and bookmarks)
            String deleteSql = "DELETE FROM posts WHERE id = ?";

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, postId);

                int affectedRows = deleteStmt.executeUpdate();

                if (affectedRows > 0) {
                    Logger.info("Post " + postId + " deleted by user " + userId);
                    return true;
                }
            }

        } catch (SQLException e) {
            Logger.error("Error deleting post: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get a specific post by ID
     *
     * @param postId The post ID
     * @param requestingUserId The user ID making the request (for like/bookmark status)
     * @return Post object if found, null otherwise
     */
    public Post getPostById(int postId, int requestingUserId) {
        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                SELECT p.id, p.user_id, u.username, p.content, p.image_url, 
                       p.created_at, p.updated_at,
                       (SELECT COUNT(*) FROM likes l WHERE l.post_id = p.id) as like_count,
                       (SELECT COUNT(*) FROM likes l WHERE l.post_id = p.id AND l.user_id = ?) as user_liked,
                       (SELECT COUNT(*) FROM bookmarks b WHERE b.post_id = p.id AND b.user_id = ?) as user_bookmarked
                FROM posts p
                JOIN users u ON p.user_id = u.id
                WHERE p.id = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, requestingUserId);
                stmt.setInt(2, requestingUserId);
                stmt.setInt(3, postId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Post post = new Post();
                        post.setId(rs.getInt("id"));
                        post.setUserId(rs.getInt("user_id"));
                        post.setUsername(rs.getString("username"));
                        post.setContent(rs.getString("content"));
                        post.setImageUrl(rs.getString("image_url"));
                        post.setLikeCount(rs.getInt("like_count"));
                        post.setLiked(rs.getInt("user_liked") > 0);
                        post.setBookmarked(rs.getInt("user_bookmarked") > 0);
                        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        post.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

                        return post;
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error getting post by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get bookmarked posts for a user
     *
     * @param userId The user ID
     * @return List of bookmarked posts
     */
    public List<Post> getBookmarkedPosts(int userId) {
        List<Post> posts = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                SELECT p.id, p.user_id, u.username, p.content, p.image_url, 
                       p.created_at, p.updated_at,
                       (SELECT COUNT(*) FROM likes l WHERE l.post_id = p.id) as like_count,
                       (SELECT COUNT(*) FROM likes l WHERE l.post_id = p.id AND l.user_id = ?) as user_liked,
                       1 as user_bookmarked
                FROM posts p
                JOIN users u ON p.user_id = u.id
                JOIN bookmarks b ON p.id = b.post_id
                WHERE b.user_id = ?
                ORDER BY b.created_at DESC
                LIMIT 50
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, userId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Post post = new Post();
                        post.setId(rs.getInt("id"));
                        post.setUserId(rs.getInt("user_id"));
                        post.setUsername(rs.getString("username"));
                        post.setContent(rs.getString("content"));
                        post.setImageUrl(rs.getString("image_url"));
                        post.setLikeCount(rs.getInt("like_count"));
                        post.setLiked(rs.getInt("user_liked") > 0);
                        post.setBookmarked(true);
                        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        post.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

                        posts.add(post);
                    }
                }
            }

        } catch (SQLException e) {
            Logger.error("Error getting bookmarked posts: " + e.getMessage());
        }

        return posts;
    }

    /**
     * Update post content
     *
     * @param userId The user ID requesting the update
     * @param postId The post ID to update
     * @param newContent The new content
     * @return true if post updated successfully, false otherwise
     */
    public boolean updatePost(int userId, int postId, String newContent) {
        if (newContent == null || newContent.trim().isEmpty() || newContent.length() > 500) {
            return false;
        }

        try (Connection conn = databaseManager.getConnection()) {
            // Check if user owns the post
            String checkSql = "SELECT user_id FROM posts WHERE id = ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, postId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        int postOwnerId = rs.getInt("user_id");

                        if (postOwnerId != userId) {
                            Logger.warn("User " + userId + " attempted to update post " + postId + " owned by user " + postOwnerId);
                            return false;
                        }
                    } else {
                        Logger.warn("Post " + postId + " not found for update");
                        return false;
                    }
                }
            }

            // Update the post
            String updateSql = "UPDATE posts SET content = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newContent.trim());
                updateStmt.setInt(2, postId);

                int affectedRows = updateStmt.executeUpdate();

                if (affectedRows > 0) {
                    Logger.info("Post " + postId + " updated by user " + userId);
                    return true;
                }
            }

        } catch (SQLException e) {
            Logger.error("Error updating post: " + e.getMessage());
        }

        return false;
    }
}
