package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.server.models.Post;
import com.server.models.User;
import com.server.services.AuthService;
import com.server.services.PostService;
import com.server.services.UserService;
import com.server.utils.Logger;

/**
 * Handles individual client connections
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final AuthService authService;
    private final PostService postService;
    private final UserService userService;
    private final Gson gson;

    private BufferedReader in;
    private PrintWriter out;
    private User currentUser;
    private boolean running = true;

    public ClientHandler(Socket clientSocket, AuthService authService,
            PostService postService, UserService userService) {
        this.clientSocket = clientSocket;
        this.authService = authService;
        this.postService = postService;
        this.userService = userService;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try {
            // Initialize streams
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            Logger.info("Client handler started for " + clientSocket.getRemoteSocketAddress());

            // Handle client requests
            handleClientRequests();

        } catch (IOException e) {
            Logger.error("Error in client handler: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Handle incoming client requests
     */
    private void handleClientRequests() {
        while (running && !clientSocket.isClosed()) {
            try {
                String requestLine = in.readLine();
                if (requestLine == null) {
                    // Client disconnected
                    break;
                }

                // Parse request
                JsonObject request = JsonParser.parseString(requestLine).getAsJsonObject();
                String command = request.get("command").getAsString();
                JsonObject data = request.getAsJsonObject("data");

                // Process command
                JsonObject response = processCommand(command, data);

                // Send response
                out.println(gson.toJson(response));

            } catch (SocketTimeoutException e) {
                // Client timeout - continue waiting
                continue;
            } catch (IOException e) {
                Logger.error("Error reading from client: " + e.getMessage());
                break;
            } catch (Exception e) {
                Logger.error("Error processing client request: " + e.getMessage());

                // Send error response
                JsonObject errorResponse = createErrorResponse("Internal server error");
                out.println(gson.toJson(errorResponse));
            }
        }
    }

    /**
     * Process individual commands
     */
    private JsonObject processCommand(String command, JsonObject data) {
        try {
            switch (command) {
                case "HANDSHAKE":
                    return handleHandshake(data);
                case "LOGIN":
                    return handleLogin(data);
                case "SIGNUP":
                    return handleSignup(data);
                case "LOGOUT":
                    return handleLogout(data);
                case "CREATE_POST":
                    return handleCreatePost(data);
                case "GET_FEED":
                    return handleGetFeed(data);
                case "LIKE_POST":
                    return handleLikePost(data);
                case "BOOKMARK_POST":
                    return handleBookmarkPost(data);
                case "DELETE_POST":
                    return handleDeletePost(data);
                case "GET_USER":
                    return handleGetUser(data);
                case "UPDATE_PROFILE":
                    return handleUpdateProfile(data);
                case "SEARCH_USERS":
                    return handleSearchUsers(data);
                case "FOLLOW_USER":
                    return handleFollowUser(data);
                case "GET_AVATAR_URL":
                    return handleGetAvatarUrl(data);
                case "UPDATE_AVATAR":
                    return handleUpdateAvatar(data);
                case "DELETE_AVATAR":
                    return handleDeleteAvatar(data);
                case "PING":
                    return handlePing(data);
                case "DISCONNECT":
                    return handleDisconnect(data);
                default:
                    return createErrorResponse("Unknown command: " + command);
            }
        } catch (Exception e) {
            Logger.error("Error processing command " + command + ": " + e.getMessage());
            return createErrorResponse("Error processing command: " + e.getMessage());
        }
    }

    // ==================== COMMAND HANDLERS ====================
    private JsonObject handleHandshake(JsonObject data) {
        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("message", "Handshake successful");
        response.addProperty("serverVersion", "1.0");
        return response;
    }

    private JsonObject handleLogin(JsonObject data) {
        String username = data.get("username").getAsString();
        String password = data.get("password").getAsString();

        User user = authService.authenticate(username, password);

        JsonObject response = new JsonObject();
        if (user != null) {
            currentUser = user;
            response.addProperty("success", true);
            response.addProperty("message", "Login successful");
            response.add("user", gson.toJsonTree(user));

            Logger.info("User logged in: " + username);
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid credentials");
        }

        return response;
    }

    private JsonObject handleSignup(JsonObject data) {
        String username = data.get("username").getAsString();
        String password = data.get("password").getAsString();

        User user = authService.createUser(username, password);

        JsonObject response = new JsonObject();
        if (user != null) {
            currentUser = user;
            response.addProperty("success", true);
            response.addProperty("message", "Signup successful");
            response.add("user", gson.toJsonTree(user));

            Logger.info("User signed up: " + username);
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "Username already exists");
        }

        return response;
    }

    private JsonObject handleLogout(JsonObject data) {
        if (currentUser != null) {
            Logger.info("User logged out: " + currentUser.getUsername());
            currentUser = null;
        }

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("message", "Logout successful");
        return response;
    }

    private JsonObject handleCreatePost(JsonObject data) {
        if (currentUser == null) {
            return createErrorResponse("Not authenticated");
        }

        String content = data.get("content").getAsString();
        Post createdPost = postService.createPost(currentUser.getId(), content);

        JsonObject response = new JsonObject();
        if (createdPost != null) {
            response.addProperty("success", true);
            response.add("post", gson.toJsonTree(createdPost));
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "Failed to create post");
        }
        return response;
    }

    private JsonObject handleGetFeed(JsonObject data) {
        if (currentUser == null) {
            return createErrorResponse("Not authenticated");
        }

        var posts = postService.getFeed(currentUser.getId());

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.add("posts", gson.toJsonTree(posts));
        return response;
    }

    private JsonObject handleLikePost(JsonObject data) {
        if (currentUser == null) {
            return createErrorResponse("Not authenticated");
        }

        int postId = data.get("postId").getAsInt();
        boolean success = postService.toggleLike(currentUser.getId(), postId);
        int likeCount = postService.getLikeCount(postId);

        JsonObject response = new JsonObject();
        response.addProperty("success", success);
        response.addProperty("likeCount", likeCount);
        response.addProperty("message", success ? "Like toggled" : "Failed to toggle like");
        return response;
    }

    private JsonObject handleBookmarkPost(JsonObject data) {
        if (currentUser == null) {
            return createErrorResponse("Not authenticated");
        }

        int postId = data.get("postId").getAsInt();
        boolean success = postService.toggleBookmark(currentUser.getId(), postId);

        JsonObject response = new JsonObject();
        response.addProperty("success", success);
        response.addProperty("message", success ? "Bookmark toggled" : "Failed to toggle bookmark");
        return response;
    }

    private JsonObject handleDeletePost(JsonObject data) {
        if (currentUser == null) {
            return createErrorResponse("Not authenticated");
        }

        int postId = data.get("postId").getAsInt();
        boolean success = postService.deletePost(currentUser.getId(), postId);

        JsonObject response = new JsonObject();
        response.addProperty("success", success);
        response.addProperty("message", success ? "Post deleted" : "Failed to delete post");
        return response;
    }

    private JsonObject handleGetUser(JsonObject data) {
        int userId = data.get("userId").getAsInt();
        User user = userService.getUserById(userId);

        JsonObject response = new JsonObject();
        if (user != null) {
            response.addProperty("success", true);
            response.add("user", gson.toJsonTree(user));
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "User not found");
        }

        return response;
    }

    private JsonObject handleUpdateProfile(JsonObject data) {
        if (currentUser == null) {
            return createErrorResponse("Not authenticated");
        }

        int userId = data.get("userId").getAsInt();
        if (userId != currentUser.getId()) {
            return createErrorResponse("Unauthorized");
        }

        String fullName = data.has("fullName") ? data.get("fullName").getAsString() : null;
        String bio = data.has("bio") ? data.get("bio").getAsString() : null;
        String avatarUrl = data.has("avatarUrl") ? data.get("avatarUrl").getAsString() : null;

        boolean success = userService.updateProfile(userId, fullName, bio, avatarUrl);

        JsonObject response = new JsonObject();
        response.addProperty("success", success);
        response.addProperty("message", success ? "Profile updated" : "Failed to update profile");
        return response;
    }

    private JsonObject handleSearchUsers(JsonObject data) {
        String query = data.get("query").getAsString();
        int limit = data.has("limit") ? data.get("limit").getAsInt() : 20;

        var users = userService.searchUsers(query, limit);

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.add("users", gson.toJsonTree(users));
        return response;
    }

    private JsonObject handleFollowUser(JsonObject data) {
        if (currentUser == null) {
            return createErrorResponse("Not authenticated");
        }

        int targetUserId = data.get("targetUserId").getAsInt();
        boolean success = userService.toggleFollow(currentUser.getId(), targetUserId);

        JsonObject response = new JsonObject();
        response.addProperty("success", success);
        response.addProperty("message", success ? "Follow toggled" : "Failed to toggle follow");
        return response;
    }

    private JsonObject handleGetAvatarUrl(JsonObject data) {
        int userId = data.get("userId").getAsInt();
        String avatarUrl = userService.getAvatarUrl(userId);

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("avatarUrl", avatarUrl);
        return response;
    }

    private JsonObject handleUpdateAvatar(JsonObject data) {
        if (currentUser == null) {
            return createErrorResponse("Not authenticated");
        }

        int userId = data.get("userId").getAsInt();
        if (userId != currentUser.getId()) {
            return createErrorResponse("Unauthorized");
        }

        String avatarData = data.get("avatarData").getAsString();
        String contentType = data.get("contentType").getAsString();

        String avatarUrl = userService.updateAvatar(userId, avatarData, contentType);

        JsonObject response = new JsonObject();
        if (avatarUrl != null) {
            response.addProperty("success", true);
            response.addProperty("avatarUrl", avatarUrl);
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "Failed to update avatar");
        }

        return response;
    }

    private JsonObject handleDeleteAvatar(JsonObject data) {
        if (currentUser == null) {
            return createErrorResponse("Not authenticated");
        }

        int userId = data.get("userId").getAsInt();
        if (userId != currentUser.getId()) {
            return createErrorResponse("Unauthorized");
        }

        boolean success = userService.deleteAvatar(userId);

        JsonObject response = new JsonObject();
        response.addProperty("success", success);
        response.addProperty("message", success ? "Avatar deleted" : "Failed to delete avatar");
        return response;
    }

    private JsonObject handlePing(JsonObject data) {
        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("message", "pong");
        response.addProperty("timestamp", System.currentTimeMillis());
        return response;
    }

    private JsonObject handleDisconnect(JsonObject data) {
        running = false;

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("message", "Disconnect acknowledged");
        return response;
    }

    // ==================== UTILITY METHODS ====================
    private JsonObject createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("success", false);
        response.addProperty("message", message);
        return response;
    }

    private void cleanup() {
        try {
            if (currentUser != null) {
                Logger.info("Client disconnected: " + currentUser.getUsername());
            } else {
                Logger.info("Client disconnected: " + clientSocket.getRemoteSocketAddress());
            }

            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            Logger.error("Error cleaning up client handler: " + e.getMessage());
        }
    }
}
