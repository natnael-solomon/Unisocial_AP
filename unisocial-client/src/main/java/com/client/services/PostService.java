package com.client.services;

import com.client.core.EventBus;
import com.client.events.PostEvent;
import com.client.models.Post;
import com.client.utils.ValidationUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PostService {
    private final NetworkService networkService;
    private final EventBus eventBus;

    public PostService(NetworkService networkService, EventBus eventBus) {
        this.networkService = networkService;
        this.eventBus = eventBus;
    }

    public void createPost(String content) {
        CompletableFuture.supplyAsync(() -> {
            try {
                // Validate content before sending to server
                ValidationUtils.getPostContentValidationError(content);

                // Sanitize content
                String sanitizedContent = ValidationUtils.sanitizeInput(content.trim());

                // Publish attempt event
                eventBus.publish(new PostEvent.CreateAttempt(sanitizedContent));

                // Check network connection
                if (!networkService.isConnected() && !networkService.connect()) {
                    throw new RuntimeException("Unable to connect to server");
                }

                // Create post on server
                boolean success = networkService.createPost(sanitizedContent);

                if (success) {
                    eventBus.publish(new PostEvent.CreateSuccess(sanitizedContent));
                } else {
                    eventBus.publish(new PostEvent.CreateFailure("Failed to create post on server"));
                }

                return success;

            } catch (IllegalArgumentException e) {
                // Validation error
                String sanitizedError = ValidationUtils.sanitizeInput(e.getMessage());
                eventBus.publish(new PostEvent.CreateFailure(sanitizedError));
                return false;
            } catch (Exception e) {
                // Network or other error
                String errorMessage = "Post creation failed: " + ValidationUtils.sanitizeInput(e.getMessage());
                eventBus.publish(new PostEvent.CreateFailure(errorMessage));
                return false;
            }
        });
    }

    public CompletableFuture<List<Post>> getFeed() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check network connection
                if (!networkService.isConnected() && !networkService.connect()) {
                    throw new RuntimeException("Unable to connect to server");
                }

                // Get feed from server
                List<Post> posts = networkService.getFeed();

                // Validate and sanitize posts
                List<Post> validPosts = posts.stream()
                        .filter(this::isValidPost)
                        .map(this::sanitizePost)
                        .collect(Collectors.toList());

                eventBus.publish(new PostEvent.FeedLoaded(validPosts));
                return validPosts;

            } catch (Exception e) {
                String errorMessage = "Failed to load feed: " + ValidationUtils.sanitizeInput(e.getMessage());
                eventBus.publish(new PostEvent.FeedLoadFailure(errorMessage));
                return List.of();
            }
        });
    }

    public CompletableFuture<Boolean> likePost(int postId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate post ID
                if (postId <= 0) {
                    throw new IllegalArgumentException("Invalid post ID");
                }

                // Check network connection
                if (!networkService.isConnected() && !networkService.connect()) {
                    throw new RuntimeException("Unable to connect to server");
                }

                // Toggle like on server
                boolean success = networkService.likePost(postId);

                if (success) {
                    // Note: In a real implementation, the server should return
                    // the updated like status and count
                    eventBus.publish(new PostEvent.LikeToggled(postId, true, 0));
                } else {
                    eventBus.publish(new PostEvent.LikeFailure(postId, "Failed to toggle like"));
                }

                return success;

            } catch (Exception e) {
                String errorMessage = ValidationUtils.sanitizeInput(e.getMessage());
                eventBus.publish(new PostEvent.LikeFailure(postId, errorMessage));
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> bookmarkPost(int postId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate post ID
                if (postId <= 0) {
                    throw new IllegalArgumentException("Invalid post ID");
                }

                // Check network connection
                if (!networkService.isConnected() && !networkService.connect()) {
                    throw new RuntimeException("Unable to connect to server");
                }

                // Toggle bookmark on server
                boolean success = networkService.bookmarkPost(postId);

                if (success) {
                    eventBus.publish(new PostEvent.BookmarkToggled(postId, true));
                } else {
                    eventBus.publish(new PostEvent.BookmarkFailure(postId, "Failed to toggle bookmark"));
                }

                return success;

            } catch (Exception e) {
                String errorMessage = ValidationUtils.sanitizeInput(e.getMessage());
                eventBus.publish(new PostEvent.BookmarkFailure(postId, errorMessage));
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> deletePost(int postId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate post ID
                if (postId <= 0) {
                    throw new IllegalArgumentException("Invalid post ID");
                }

                // Check network connection
                if (!networkService.isConnected() && !networkService.connect()) {
                    throw new RuntimeException("Unable to connect to server");
                }

                // Delete post on server
                boolean success = networkService.deletePost(postId);

                if (success) {
                    eventBus.publish(new PostEvent.PostDeleted(postId));
                } else {
                    eventBus.publish(new PostEvent.DeleteFailure(postId, "Failed to delete post"));
                }

                return success;

            } catch (Exception e) {
                String errorMessage = ValidationUtils.sanitizeInput(e.getMessage());
                eventBus.publish(new PostEvent.DeleteFailure(postId, errorMessage));
                return false;
            }
        });
    }

    /**
     * Validate if a post is valid
     */
    private boolean isValidPost(Post post) {
        return post != null &&
                ValidationUtils.isNotEmpty(post.getUsername()) &&
                ValidationUtils.isValidPostContent(post.getContent()) &&
                post.getId() > 0;
    }

    /**
     * Sanitize post data
     */
    private Post sanitizePost(Post post) {
        if (post == null) return null;

        // Sanitize text fields
        post.setUsername(ValidationUtils.sanitizeInput(post.getUsername()));
        post.setContent(ValidationUtils.sanitizeInput(post.getContent()));

        // Ensure non-negative counts
        post.setLikeCount(Math.max(0, post.getLikeCount()));

        // Validate image URL if present
        if (ValidationUtils.isNotEmpty(post.getImageUrl()) &&
                !isValidImageUrl(post.getImageUrl())) {
            post.setImageUrl(null); // Remove invalid URL
        }

        return post;
    }

    /**
     * Basic URL validation for images
     */
    private boolean isValidImageUrl(String url) {
        if (ValidationUtils.isEmpty(url)) return false;

        // Basic URL pattern check
        return url.matches("^https?://.*\\.(jpg|jpeg|png|gif|webp)$");
    }

    /**
     * Get character count for post content
     */
    public int getPostCharacterCount(String content) {
        return ValidationUtils.isEmpty(content) ? 0 : content.length();
    }


}
