package com.client.core;

public class Constants {
    // Network Constants
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 12345;
    public static final int CONNECTION_TIMEOUT = 5000; // 5 seconds
    
    // UI Constants
    public static final int AUTH_WINDOW_WIDTH = 712;
    public static final int AUTH_WINDOW_HEIGHT = 500;
    public static final int FEED_WINDOW_WIDTH = 1200;
    public static final int FEED_WINDOW_HEIGHT = 800;
    
    // Post Constants
    public static final int MAX_POST_LENGTH = 280;
    
    // File Paths
    public static final String DEFAULT_AVATAR_PATH = "/images/default-avatar.png";
    public static final String DEFAULT_POST_IMAGE_PATH = "/images/default-post-welcome_image.png";
    
    // CSS Classes
    public static final String LIKED_CLASS = "liked";
    public static final String BOOKMARKED_CLASS = "bookmarked";
    
    // Event Types
    public static final String EVENT_LOGIN_SUCCESS = "login_success";
    public static final String EVENT_LOGIN_FAILURE = "login_failure";
    public static final String EVENT_SIGNUP_SUCCESS = "signup_success";
    public static final String EVENT_SIGNUP_FAILURE = "signup_failure";
    public static final String EVENT_POST_CREATED = "post_created";
    public static final String EVENT_FEED_UPDATED = "feed_updated";
    
    private Constants() {
        // Private constructor to prevent instantiation
    }
}