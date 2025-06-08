/*
package com.client.services;

import com.client.models.Post;
import com.client.models.User;
import com.client.utils.ValidationUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

*/
/**
 * Mock implementation of NetworkService for testing and development
 * Uses in-memory data and simulates network delays
 *//*

public class MockNetworkService extends NetworkService {

    // Mock data storage
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Post> posts = new HashMap<>();
    private final Map<Integer, Set<Integer>> userLikes = new HashMap<>();
    private final Map<Integer, Set<Integer>> userBookmarks = new HashMap<>();
    private final Map<Integer, Set<Integer>> userFollows = new HashMap<>();
    private final Map<String, User> usersByUsername = new HashMap<>();

    // Current state
    private boolean connected = false;
    private User currentUser = null;
    private int nextUserId = 1;
    private int nextPostId = 1;

    // Configuration
    private final boolean simulateNetworkDelay;
    private final int minDelayMs;
    private final int maxDelayMs;
    private final double failureRate; // 0.0 to 1.0

    // Default avatar path
    private static final String DEFAULT_AVATAR_PATH = "/images/default-avatar.png";

    public MockNetworkService() {
        this(true, 100, 500, 0.05); // 5% failure rate
    }

    public MockNetworkService(boolean simulateNetworkDelay, int minDelayMs, int maxDelayMs, double failureRate) {
        this.simulateNetworkDelay = simulateNetworkDelay;
        this.minDelayMs = minDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.failureRate = failureRate;

        initializeMockData();
    }

    */
/**
     * Initialize mock data with sample users and posts
     *//*

    private void initializeMockData() {
        // Create sample users
        createMockUser("john_doe", "John Doe", "Software developer passionate about JavaFX and social media apps.");
        createMockUser("jane_smith", "Jane Smith", "UI/UX designer who loves creating beautiful interfaces.");
        createMockUser("mike_wilson", "Mike Wilson", "Full-stack developer and coffee enthusiast.");
        createMockUser("sarah_jones", "Sarah Jones", "Product manager with a passion for user experience.");
        createMockUser("alex_brown", "Alex Brown", "DevOps engineer who automates everything.");

        // Create sample posts
        createMockPost(1, "Just finished implementing the new user authentication system! 🚀");
        createMockPost(2, "Working on some exciting UI improvements. Can't wait to share them!");
        createMockPost(1, "Coffee + Code = Perfect Monday morning ☕");
        createMockPost(3, "Anyone else excited about the new JavaFX features?");
        createMockPost(4, "User feedback is so valuable for product development. Keep it coming!");
        createMockPost(2, "Design tip: Always consider accessibility when creating interfaces.");
        createMockPost(5, "Automated deployment pipeline is now live! No more manual deployments 🎉");
        createMockPost(1, "Debugging is like being a detective in a crime movie where you are also the murderer.");

        // Create some follow relationships
        addFollowRelationship(1, 2);
        addFollowRelationship(1, 3);
        addFollowRelationship(2, 1);
        addFollowRelationship(2, 4);
        addFollowRelationship(3, 1);
        addFollowRelationship(3, 5);
        addFollowRelationship(4, 2);
        addFollowRelationship(5, 1);
        addFollowRelationship(5, 3);

        // Add some likes
        addLike(1, 2); // John likes Jane's post
        addLike(2, 1); // Jane likes John's post
        addLike(3, 1); // Mike likes John's post
        addLike(1, 4); // John likes Sarah's post

        // Add some bookmarks
        addBookmark(1, 2); // John bookmarks Jane's post
        addBookmark(2, 7); // Jane bookmarks Alex's post
    }

    private void createMockUser(String username, String fullName, String bio) {
        User user = new User();
        user.setId(nextUserId++);
        user.setUsername(username);
        user.setFullName(fullName);
        user.setBio(bio);
        user.setAvatarUrl(DEFAULT_AVATAR_PATH);
        user.setOnline(ThreadLocalRandom.current().nextBoolean());

        users.put(user.getId(), user);
        usersByUsername.put(username, user);
        userLikes.put(user.getId(), new HashSet<>());
        userBookmarks.put(user.getId(), new HashSet<>());
        userFollows.put(user.getId(), new HashSet<>());

        updateFollowerCounts(user.getId());
    }

    private void createMockPost(int userId, String content) {
        User author = users.get(userId);
        if (author == null) return;

        Post post = new Post();
        post.setId(nextPostId++);
        post.setUserId(userId);
        post.setUsername(author.getUsername());
        post.setContent(content);
        post.setCreatedAt(LocalDateTime.now());
        post.setLikeCount(ThreadLocalRandom.current().nextInt(0, 15));
        post.setLiked(false);
        post.setBookmarked(false);

        posts.put(post.getId(), post);
    }

    private void addFollowRelationship(int followerId, int followeeId) {
        userFollows.get(followerId).add(followeeId);
        updateFollowerCounts(followerId);
        updateFollowerCounts(followeeId);
    }

    private void addLike(int userId, int postId) {
        userLikes.get(userId).add(postId);
        Post post = posts.get(postId);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
        }
    }

    private void addBookmark(int userId, int postId) {
        userBookmarks.get(userId).add(postId);
    }

    private void updateFollowerCounts(int userId) {
        User user = users.get(userId);
        if (user == null) return;

        // Count following
        int followingCount = userFollows.get(userId).size();
        user.setFollowingCount(followingCount);

        // Count followers
        int followersCount = (int) userFollows.values().stream()
                .mapToLong(follows -> follows.contains(userId) ? 1 : 0)
                .sum();
        user.setFollowersCount(followersCount);
    }

    // ==================== NETWORK SIMULATION ====================

    private void simulateNetworkDelay() {
        if (!simulateNetworkDelay) return;

        try {
            int delay = ThreadLocalRandom.current().nextInt(minDelayMs, maxDelayMs + 1);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void simulateNetworkFailure() {
        if (ThreadLocalRandom.current().nextDouble() < failureRate) {
            throw new RuntimeException("Simulated network failure");
        }
    }

    // ==================== CONNECTION MANAGEMENT ====================

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean connect() {
        simulateNetworkDelay();
        simulateNetworkFailure();

        connected = true;
        System.out.println("MockNetworkService: Connected to mock server");
        return true;
    }

    @Override
    public boolean disconnect() {
        connected = false;
        currentUser = null;
        System.out.println("MockNetworkService: Disconnected from mock server");
        return true;
    }

    @Override
    public boolean reconnect() {
        disconnect();
        return connect();
    }

    // ==================== AUTHENTICATION ====================

    @Override
    public User login(String username, String password) {
        simulateNetworkDelay();
        simulateNetworkFailure();

        if (!connected) {
            throw new RuntimeException("Not connected to server");
        }

        // Validate credentials (mock validation)
        if (ValidationUtils.isEmpty(username) || ValidationUtils.isEmpty(password)) {
            return null;
        }

        User user = usersByUsername.get(username);
        if (user != null) {
            currentUser = user;
            user.setOnline(true);
            System.out.println("MockNetworkService: User logged in: " + username);
            return user;
        }

        return null;
    }

    @Override
    public User signup(String username, String password) {
        simulateNetworkDelay();
        simulateNetworkFailure();

        if (!connected) {
            throw new RuntimeException("Not connected to server");
        }

        // Check if username already exists
        if (usersByUsername.containsKey(username)) {
            return null;
        }

        // Create new user
        User newUser = new User();
        newUser.setId(nextUserId++);
        newUser.setUsername(username);
        newUser.setFullName(username);
        newUser.setBio("New user on UniSocial!");
        newUser.setAvatarUrl(DEFAULT_AVATAR_PATH);
        newUser.setOnline(true);
        newUser.setFollowingCount(0);
        newUser.setFollowersCount(0);

        users.put(newUser.getId(), newUser);
        usersByUsername.put(username, newUser);
        userLikes.put(newUser.getId(), new HashSet<>());
        userBookmarks.put(newUser.getId(), new HashSet<>());
        userFollows.put(newUser.getId(), new HashSet<>());

        currentUser = newUser;
        System.out.println("MockNetworkService: User signed up: " + username);
        return newUser;
    }

    @Override
    public boolean logout() {
        simulateNetworkDelay();

        if (currentUser != null) {
            currentUser.setOnline(false);
            System.out.println("MockNetworkService: User logged out: " + currentUser.getUsername());
            currentUser = null;
        }

        return true;
    }

    // ==================== POST OPERATIONS ====================

    @Override
    public boolean createPost(String content) {
        simulateNetworkDelay();
        simulateNetworkFailure();

        if (!connected || currentUser == null) {
            throw new RuntimeException("Not connected or not logged in");
        }

        Post newPost = new Post();
        newPost.setId(nextPostId++);
        newPost.setUserId(currentUser.getId());
        newPost.setUsername(currentUser.getUsername());
        newPost.setContent(content);
        newPost.setCreatedAt(LocalDateTime.now());
        newPost.setLikeCount(0);
        newPost.setLiked(false);
        newPost.setBookmarked(false);

        posts.put(newPost.getId(), newPost);
        System.out.println("MockNetworkService: Post created by " + currentUser.getUsername());
        return true;
    }

    @Override
    public List<Post> getFeed() {
        simulateNetworkDelay();
        simulateNetworkFailure();

        if (!connected || currentUser == null) {
            throw new RuntimeException("Not connected or not logged in");
        }

        // Get posts from followed users and own posts
        Set<Integer> followedUsers = userFollows.get(currentUser.getId());
        Set<Integer> relevantUsers = new HashSet<>(followedUsers);
        relevantUsers.add(currentUser.getId());

        List<Post> feed = posts.values().stream()
                .filter(post -> relevantUsers.contains(post.getUserId()))
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .limit(50) // Limit feed size
                .collect(Collectors.toList());

        // Update like/bookmark status for current user
        for (Post post : feed) {
            post.setLiked(userLikes.get(currentUser.getId()).contains(post.getId()));
            post.setBookmarked(userBookmarks.get(currentUser.getId()).contains(post.getId()));
        }

        System.out.println("MockNetworkService: Feed loaded with " + feed.size() + " posts");
        return feed;
    }

    @Override
    public boolean likePost(int postId) {
        simulateNetworkDelay();
        simulateNetworkFailure();

        if (!connected || currentUser == null) {
            throw new RuntimeException("Not connected or not logged in");
        }

        Post post = posts.get(postId);
        if (post == null) return false;

        Set<Integer> userLikedPosts = userLikes.get(currentUser.getId());

        if (userLikedPosts.contains(postId)) {
            // Unlike
            userLikedPosts.remove(postId);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            post.setLiked(false);
        } else {
            // Like
            userLikedPosts.add(postId);
            post.setLikeCount(post.getLikeCount() + 1);
            post.setLiked(true);
        }

        System.out.println("MockNetworkService: Post " + postId + " like toggled");
        return true;
    }

    @Override
    public boolean bookmarkPost(int postId) {
        simulateNetworkDelay();
        simulateNetworkFailure();

        if (!connected || currentUser == null) {
            throw new RuntimeException("Not connected or not logged in");
        }

        Post post = posts.get(postId);
        if (post == null) return false;

        Set<Integer> userBookmarkedPosts = userBookmarks.get(currentUser.getId());

        if (userBookmarkedPosts.contains(postId)) {
            // Remove bookmark
            userBookmarkedPosts.remove(postId);
            post.setBookmarked(false);
        } else {
            // Add bookmark
            userBookmarkedPosts.add(postId);
            post.setBookmarked(true);
        }

        System.out.println("MockNetworkService: Post " + postId + " bookmark toggled");
        return true;
    }

    @Override
    public boolean deletePost(int postId) {
        simulateNetworkDelay();
        simulateNetworkFailure();

        if (!connected || currentUser == null) {
            throw new RuntimeException("Not connected or not logged in");
        }

        Post post = posts.get(postId);
        if (post == null || post.getUserId() != currentUser.getId()) {
            return false; // Can only delete own posts
        }

        posts.remove(postId);

        // Remove from all user likes and bookmarks
        userLikes.values().forEach(likes -> likes.remove(postId));
        userBookmarks.values().forEach(bookmarks -> bookmarks.remove(postId));

        System.out.println("MockNetworkService: Post " + postId + " deleted");
        return true;
    }

    // ==================== USER OPERATIONS ====================

    @Override
    public User getUserById(int userId) {
        simulateNetworkDelay();
        simulateNetworkFailure();

        if (!connected) {
            throw new RuntimeException("Not connected to server");
        }

        return users.get(userId);
    }

    @Override
    public boolean updateProfile(User user) {
        simulateNetworkDelay();
        simulateNetworkFailure();

        if (!connected || currentUser == null || user.getId() != currentUser.getId()) {
            throw new RuntimeException("Not connected, not logged in, or unauthorized");
        }

        User existingUser = users.get(user.getId());
        if (existingUser != null) {
            existingUser.setFullName(user.getFullName());
            existingUser.setBio(user.getBio());
            if (user.getAvatarUrl() != null) {
                existingUser.setAvatarUrl(user.getAvatarUrl());
            }

            System.out.println("MockNetworkService: Profile updated for " + user.getUsername());
            return true;
        }

        return false;
    }

    @Override
    public List<User> searchUsers(String query) {
        simulateNetworkDelay();
        simulateNetworkFailure();

        if (!connected) {
            throw new RuntimeException("Not connected to server");
        }

        String lowerQuery = query.toLowerCase();

        return users.values().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(lowerQuery) ||
                        (user.getFullName() != null && user.getFullName().toLowerCase().contains(lowerQuery)))
                .limit(20)
                .collect(Collectors.toList());
    }

    @Override
    public boolean followUser(int targetUserId) {
        simulateNetworkDelay();
        simulateNetworkFailure();

        if (!connected || currentUser == null) {
            throw new RuntimeException("Not connected or not logged in");
        }

        if (targetUserId == currentUser.getId()) {
            return false; // Can't follow yourself
        }

        User targetUser = users.get(targetUserId);
        if (targetUser == null) return false;

        Set<Integer> following = userFollows.get(currentUser.getId());

        if (following.contains(targetUserId)) {
            // Unfollow
            following.remove(targetUserId);
        } else {
            // Follow
            following.add(targetUserId);
        }

        updateFollowerCounts(currentUser.getId());
        updateFollowerCounts(targetUserId);

        System.out.println("MockNetworkService: Follow relationship toggled");
        return true;
    }

    public String getAvatarUrl(int userId) {
        simulateNetworkDelay();

        if (!connected) {
            return DEFAULT_AVATAR_PATH;
        }

        User user = users.get(userId);
        return user != null ? user.getAvatarUrl() : DEFAULT_AVATAR_PATH;
    }

    public String updateAvatar(int userId, byte[] avatarData) {
        simulateNetworkDelay();
        simulateNetworkFailure();

        if (!connected || currentUser == null || userId != currentUser.getId()) {
            throw new RuntimeException("Not connected, not logged in, or unauthorized");
        }

        // Simulate avatar upload - in real implementation, this would upload to server
        String newAvatarUrl = "/images/avatar_" + userId + "_" + System.currentTimeMillis() + ".jpg";

        User user = users.get(userId);
        if (user != null) {
            user.setAvatarUrl(newAvatarUrl);
            System.out.println("MockNetworkService: Avatar updated for user " + userId);
            return newAvatarUrl;
        }

        return null;
    }

    public String getCurrentUserAvatarUrl() {
        if (currentUser != null) {
            return getAvatarUrl(currentUser.getId());
        }
        return DEFAULT_AVATAR_PATH;
    }

    public boolean deleteAvatar(int userId) {
        simulateNetworkDelay();

        if (!connected || currentUser == null || userId != currentUser.getId()) {
            return false;
        }

        User user = users.get(userId);
        if (user != null) {
            user.setAvatarUrl(DEFAULT_AVATAR_PATH);
            System.out.println("MockNetworkService: Avatar deleted for user " + userId);
            return true;
        }

        return false;
    }

    // ==================== MOCK-SPECIFIC METHODS ====================

    */
/**
     * Get current mock user (for testing)
     *//*

    public User getCurrentMockUser() {
        return currentUser;
    }

    */
/**
     * Get all mock users (for testing)
     *//*

    public Collection<User> getAllMockUsers() {
        return users.values();
    }

    */
/**
     * Get all mock posts (for testing)
     *//*

    public Collection<Post> getAllMockPosts() {
        return posts.values();
    }

    */
/**
     * Reset mock data
     *//*

    public void resetMockData() {
        users.clear();
        posts.clear();
        userLikes.clear();
        userBookmarks.clear();
        userFollows.clear();
        usersByUsername.clear();
        currentUser = null;
        nextUserId = 1;
        nextPostId = 1;

        initializeMockData();
        System.out.println("MockNetworkService: Mock data reset");
    }

    */
/**
     * Set failure rate for testing error handling
     *//*

    public void setFailureRate(double failureRate) {
        // This would require making the field non-final, but for demo purposes
        System.out.println("MockNetworkService: Failure rate would be set to " + failureRate);
    }
}
*/
