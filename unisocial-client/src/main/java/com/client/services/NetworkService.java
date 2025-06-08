package com.client.services;

import com.client.core.AppState;
import com.client.models.Post;
import com.client.models.User;
import com.client.utils.ValidationUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class NetworkService {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final int CONNECTION_TIMEOUT = 5000; // 5 seconds
    private static final int READ_TIMEOUT = 10000; // 10 seconds
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson;
    private boolean isConnecting = false;

    public NetworkService() {
        this.gson = new Gson();
    }

    // ==================== CONNECTION MANAGEMENT ====================

    /**
     * Check if the network connection is active
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed() &&
                out != null && in != null;
    }

    /**
     * Establish connection to the server
     *
     * @return true if connection successful, false otherwise
     */
    public boolean connect() {
        if (isConnecting) {
            return false; // Prevent multiple simultaneous connection attempts
        }

        if (isConnected()) {
            return true; // Already connected
        }

        isConnecting = true;

        try {
            // Close any existing connection
            disconnect();

            // Create new socket connection
            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(SERVER_HOST, SERVER_PORT), CONNECTION_TIMEOUT);
            socket.setSoTimeout(READ_TIMEOUT);

            // Initialize streams
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send handshake
            JsonObject handshake = new JsonObject();
            handshake.addProperty("type", "HANDSHAKE");
            handshake.addProperty("version", "1.0");
            handshake.addProperty("clientId", generateClientId());

            sendRequest("HANDSHAKE", handshake);
            JsonObject response = readResponse();

            boolean success = response != null && response.get("success").getAsBoolean();

            if (!success) {
                disconnect();
                return false;
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            disconnect();
            return false;
        } finally {
            isConnecting = false;
        }
    }

    /**
     * Disconnect from the server and clean up resources
     *
     * @return true if disconnection was successful, false otherwise
     */
    public boolean disconnect() {
        try {
            // Send disconnect message if connected
            if (isConnected()) {
                try {
                    JsonObject disconnectMsg = new JsonObject();
                    disconnectMsg.addProperty("userId", getCurrentUserId());
                    sendRequest("DISCONNECT", disconnectMsg);
                } catch (Exception e) {
                    // Ignore errors when sending disconnect message
                    System.err.println("Warning: Could not send disconnect message: " + e.getMessage());
                }
            }

            // Close streams
            if (out != null) {
                out.close();
                out = null;
            }

            if (in != null) {
                in.close();
                in = null;
            }

            // Close socket
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error disconnecting from server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reconnect to the server
     *
     * @return true if reconnection successful, false otherwise
     */
    public boolean reconnect() {
        disconnect();
        return connect();
    }

    // ==================== AUTHENTICATION ====================

    /**
     * Login user
     *
     * @param username The username
     * @param password The password
     * @return User object if successful, null otherwise
     */
    public User login(String username, String password) {
        try {
            if (!ensureConnection()) {
                return null;
            }

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("username", username);
            requestBody.addProperty("password", password);

            sendRequest("LOGIN", requestBody);
            JsonObject response = readResponse();

            if (response != null && response.get("success").getAsBoolean()) {
                JsonElement userElement = response.get("user");
                if (userElement != null) {
                    return gson.fromJson(userElement, User.class);
                }
            }

            return null;

        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            return null;
        }
    }

    /**
     * Signup new user
     *
     * @param username The username
     * @param password The password
     * @return User object if successful, null otherwise
     */
    public User signup(String username, String password) {
        try {
            if (!ensureConnection()) {
                return null;
            }

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("username", username);
            requestBody.addProperty("password", password);

            sendRequest("SIGNUP", requestBody);
            JsonObject response = readResponse();

            if (response != null && response.get("success").getAsBoolean()) {
                JsonElement userElement = response.get("user");
                if (userElement != null) {
                    return gson.fromJson(userElement, User.class);
                }
            }

            return null;

        } catch (Exception e) {
            System.err.println("Error during signup: " + e.getMessage());
            return null;
        }
    }

    /**
     * Logout current user
     *
     * @return true if successful, false otherwise
     */
    public boolean logout() {
        try {
            if (!isConnected()) {
                return true; // Already disconnected
            }

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("userId", getCurrentUserId());

            sendRequest("LOGOUT", requestBody);
            JsonObject response = readResponse();

            // Disconnect after logout
            disconnect();

            return response != null && response.get("success").getAsBoolean();

        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            disconnect(); // Force disconnect on error
            return false;
        }
    }

    // ==================== POST OPERATIONS ====================

    /**
     * Create a new post
     *
     * @param content The post content
     * @return true if successful, false otherwise
     */
    public boolean createPost(String content) {
        try {
            if (!ensureConnection()) {
                return false;
            }

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("content", content);
            requestBody.addProperty("userId", getCurrentUserId());

            sendRequest("CREATE_POST", requestBody);
            JsonObject response = readResponse();

            return response != null && response.get("success").getAsBoolean();

        } catch (Exception e) {
            System.err.println("Error creating post: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the user's feed
     *
     * @return List of posts in the feed
     */
    public List<Post> getFeed() {
        try {
            if (!ensureConnection()) {
                return new ArrayList<>();
            }

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("userId", getCurrentUserId());

            sendRequest("GET_FEED", requestBody);
            JsonObject response = readResponse();

            if (response != null && response.get("success").getAsBoolean()) {
                JsonArray postsArray = response.getAsJsonArray("posts");
                List<Post> posts = new ArrayList<>();

                for (JsonElement postElement : postsArray) {
                    Post post = gson.fromJson(postElement, Post.class);
                    posts.add(post);
                }

                return posts;
            }

            return new ArrayList<>();

        } catch (Exception e) {
            System.err.println("Error getting feed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Like or unlike a post
     *
     * @param postId The post ID
     * @return true if successful, false otherwise
     */
    public boolean likePost(int postId) {
        try {
            if (!ensureConnection()) {
                return false;
            }

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("postId", postId);
            requestBody.addProperty("userId", getCurrentUserId());

            sendRequest("LIKE_POST", requestBody);
            JsonObject response = readResponse();

            return response != null && response.get("success").getAsBoolean();

        } catch (Exception e) {
            System.err.println("Error liking post: " + e.getMessage());
            return false;
        }
    }

    /**
     * Bookmark or unbookmark a post
     *
     * @param postId The post ID to bookmark/unbookmark
     * @return true if successful, false otherwise
     */
    public boolean bookmarkPost(int postId) {
        try {
            if (!ensureConnection()) {
                return false;
            }

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("postId", postId);
            requestBody.addProperty("userId", getCurrentUserId());

            sendRequest("BOOKMARK_POST", requestBody);
            JsonObject response = readResponse();

            return response != null && response.get("success").getAsBoolean();

        } catch (Exception e) {
            System.err.println("Error bookmarking post: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a post
     *
     * @param postId The post ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deletePost(int postId) {
        try {
            if (!ensureConnection()) {
                return false;
            }

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("postId", postId);
            requestBody.addProperty("userId", getCurrentUserId());

            sendRequest("DELETE_POST", requestBody);
            JsonObject response = readResponse();

            return response != null && response.get("success").getAsBoolean();

        } catch (Exception e) {
            System.err.println("Error deleting post: " + e.getMessage());
            return false;
        }
    }

    // ==================== USER OPERATIONS ====================

    /**
     * Get user by ID
     *
     * @param userId The user ID
     * @return User object if found, null otherwise
     */
    public User getUserById(int userId) {
        try {
            if (!ensureConnection()) {
                return null;
            }

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("userId", userId);

            sendRequest("GET_USER", requestBody);
            JsonObject response = readResponse();

            if (response != null && response.get("success").getAsBoolean()) {
                JsonElement userElement = response.get("user");
                if (userElement != null) {
                    return gson.fromJson(userElement, User.class);
                }
            }

            return null;

        } catch (Exception e) {
            System.err.println("Error getting user: " + e.getMessage());
            return null;
        }
    }

    /**
     * Update user profile
     *
     * @param user The user with updated profile information
     * @return true if update was successful, false otherwise
     */
    public boolean updateProfile(User user) {
        try {
            if (!ensureConnection()) {
                return false;
            }

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("userId", user.getId());
            requestBody.addProperty("fullName", user.getFullName());
            requestBody.addProperty("bio", user.getBio());
            if (user.getAvatarUrl() != null) {
                requestBody.addProperty("avatarUrl", user.getAvatarUrl());
            }

            sendRequest("UPDATE_PROFILE", requestBody);
            JsonObject response = readResponse();

            return response != null && response.get("success").getAsBoolean();

        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            return false;
        }
    }

    /**
     * Search users by username
     *
     * @param query The search query
     * @return List of matching users
     */
    public List<User> searchUsers(String query) {
        try {
            if (!ensureConnection()) {
                return new ArrayList<>();
            }

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("query", query);
            requestBody.addProperty("limit", 20); // Limit results

            sendRequest("SEARCH_USERS", requestBody);
            JsonObject response = readResponse();

            if (response != null && response.get("success").getAsBoolean()) {
                JsonArray usersArray = response.getAsJsonArray("users");
                List<User> users = new ArrayList<>();

                for (JsonElement userElement : usersArray) {
                    User user = gson.fromJson(userElement, User.class);
                    users.add(user);
                }

                return users;
            }

            return new ArrayList<>();

        } catch (Exception e) {
            System.err.println("Error searching users: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Follow or unfollow a user
     *
     * @param targetUserId The user ID to follow/unfollow
     * @return true if successful, false otherwise
     */
    public boolean followUser(int targetUserId) {
        try {
            if (!ensureConnection()) {
                return false;
            }

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("targetUserId", targetUserId);
            requestBody.addProperty("userId", getCurrentUserId());

            sendRequest("FOLLOW_USER", requestBody);
            JsonObject response = readResponse();

            return response != null && response.get("success").getAsBoolean();

        } catch (Exception e) {
            System.err.println("Error following user: " + e.getMessage());
            return false;
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Ensure connection is established, attempt to reconnect if needed
     *
     * @return true if connected, false otherwise
     */
    private boolean ensureConnection() {
        if (isConnected()) {
            return true;
        }

        // Attempt to reconnect
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            if (connect()) {
                return true;
            }

            if (attempt < MAX_RETRY_ATTEMPTS) {
                try {
                    // Exponential backoff
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return false;
    }

    /**
     * Send a request to the server
     *
     * @param command The command to send
     * @param data The data to send
     */
    private void sendRequest(String command, JsonObject data) throws IOException {
        if (out == null) {
            throw new IOException("Output stream is not available");
        }

        JsonObject request = new JsonObject();
        request.addProperty("command", command);
        request.addProperty("timestamp", System.currentTimeMillis());
        request.add("data", data);

        String requestJson = gson.toJson(request);
        out.println(requestJson);

        if (out.checkError()) {
            throw new IOException("Error sending request to server");
        }
    }

    /**
     * Read response from the server
     *
     * @return JsonObject response or null if error
     */
    private JsonObject readResponse() throws IOException {
        if (in == null) {
            throw new IOException("Input stream is not available");
        }

        try {
            String response = in.readLine();
            if (response == null) {
                throw new IOException("Server closed connection");
            }

            return JsonParser.parseString(response).getAsJsonObject();

        } catch (SocketTimeoutException e) {
            throw new IOException("Server response timeout", e);
        }
    }

    /**
     * Get current user ID (helper method)
     *
     * @return current user ID or -1 if no user logged in
     */
    private int getCurrentUserId() {
        if (AppState.getInstance().getCurrentUser() != null) {
            return AppState.getInstance().getCurrentUser().getId();
        }
        return -1;
    }

    /**
     * Generate a unique client ID
     *
     * @return unique client ID
     */
    private String generateClientId() {
        return "client_" + System.currentTimeMillis() + "_" +
                (int)(Math.random() * 10000);
    }

    // ==================== CONNECTION STATUS ====================

    /**
     * Get connection status information
     *
     * @return JsonObject with connection details
     */
    public JsonObject getConnectionStatus() {
        JsonObject status = new JsonObject();
        status.addProperty("connected", isConnected());
        status.addProperty("host", SERVER_HOST);
        status.addProperty("port", SERVER_PORT);

        if (socket != null) {
            status.addProperty("localPort", socket.getLocalPort());
            status.addProperty("remoteAddress", socket.getRemoteSocketAddress().toString());
        }

        return status;
    }

    /**
     * Test connection to server
     *
     * @return true if server is reachable, false otherwise
     */
    public boolean testConnection() {
        try {
            if (!ensureConnection()) {
                return false;
            }

            JsonObject pingData = new JsonObject();
            pingData.addProperty("timestamp", System.currentTimeMillis());

            sendRequest("PING", pingData);
            JsonObject response = readResponse();

            return response != null && response.get("success").getAsBoolean();

        } catch (Exception e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cleanup resources when service is destroyed
     */
    public void cleanup() {
        disconnect();
    }
}
