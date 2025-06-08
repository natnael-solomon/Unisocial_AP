package com.server;

import com.server.utils.Logger;

import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database connection and management
 */
public class DatabaseManager {
    private final String databaseUrl;
    private final ConcurrentHashMap<Thread, Connection> connections = new ConcurrentHashMap<>();

    public DatabaseManager(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    /**
     * Initialize database and create tables
     */
    public boolean initialize() {
        try {
            // Try to load SQLite driver explicitly
            try {
                Class.forName("org.sqlite.JDBC");
                Logger.info("SQLite JDBC driver loaded successfully");
            } catch (ClassNotFoundException e) {
                Logger.error("SQLite JDBC driver not found in classpath");
                Logger.error("Please ensure sqlite-jdbc dependency is included");
                return false;
            }

            // Test database connection
            try (Connection testConn = DriverManager.getConnection(databaseUrl)) {
                Logger.info("Database connection test successful");
                Logger.info("Database URL: " + databaseUrl);
            } catch (SQLException e) {
                Logger.error("Failed to connect to database: " + e.getMessage());
                return false;
            }

            // Create tables
            createTables();

            Logger.info("Database initialized successfully");
            return true;

        } catch (Exception e) {
            Logger.error("Failed to initialize database: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
            return false;
        }
    }

    /**
     * Get database connection for current thread
     */
    public Connection getConnection() throws SQLException {
        Thread currentThread = Thread.currentThread();
        Connection connection = connections.get(currentThread);

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(databaseUrl);
            connection.setAutoCommit(true);
            connections.put(currentThread, connection);
        }

        return connection;
    }

    /**
     * Create database tables
     */
    private void createTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            Logger.info("Creating database tables...");

            // Users table
            String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                full_name TEXT,
                bio TEXT,
                avatar_url TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
            stmt.execute(createUsersTable);
            Logger.info("Users table created/verified");

            // Posts table
            String createPostsTable = """
            CREATE TABLE IF NOT EXISTS posts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                content TEXT NOT NULL,
                image_url TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
            )
        """;
            stmt.execute(createPostsTable);
            Logger.info("Posts table created/verified");

            // Likes table
            String createLikesTable = """
            CREATE TABLE IF NOT EXISTS likes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                post_id INTEGER NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(user_id, post_id),
                FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
            )
        """;
            stmt.execute(createLikesTable);
            Logger.info("Likes table created/verified");

            // Bookmarks table
            String createBookmarksTable = """
            CREATE TABLE IF NOT EXISTS bookmarks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                post_id INTEGER NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(user_id, post_id),
                FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
            )
        """;
            stmt.execute(createBookmarksTable);
            Logger.info("Bookmarks table created/verified");

            // Follows table
            String createFollowsTable = """
            CREATE TABLE IF NOT EXISTS follows (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                follower_id INTEGER NOT NULL,
                followee_id INTEGER NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(follower_id, followee_id),
                FOREIGN KEY (follower_id) REFERENCES users (id) ON DELETE CASCADE,
                FOREIGN KEY (followee_id) REFERENCES users (id) ON DELETE CASCADE
            )
        """;
            stmt.execute(createFollowsTable);
            Logger.info("Follows table created/verified");

            // Create indexes for better performance
            Logger.info("Creating database indexes...");

            try {
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts (user_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts (created_at)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_likes_user_id ON likes (user_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_likes_post_id ON likes (post_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_bookmarks_user_id ON bookmarks (user_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_bookmarks_post_id ON bookmarks (post_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_follows_follower_id ON follows (follower_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_follows_followee_id ON follows (followee_id)");

                Logger.info("Database indexes created/verified successfully");
            } catch (SQLException e) {
                Logger.warn("Some indexes may not have been created: " + e.getMessage());
            }

            Logger.info("Database schema setup completed successfully");
        }
    }

    /**
     * Close all database connections
     */
    public void close() {
        for (Connection connection : connections.values()) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                Logger.error("Error closing database connection: " + e.getMessage());
            }
        }
        connections.clear();
        Logger.info("Database connections closed");
    }

    /**
     * Execute a transaction
     */
    public boolean executeTransaction(DatabaseTransaction transaction) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                boolean result = transaction.execute(conn);
                if (result) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            Logger.error("Transaction failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Interface for database transactions
     */
    @FunctionalInterface
    public interface DatabaseTransaction {
        boolean execute(Connection connection) throws SQLException;
    }
}
