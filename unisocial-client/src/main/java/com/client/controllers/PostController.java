package com.client.controllers;

import com.client.events.PostEvent;
import com.client.models.Post;
import com.client.utils.ImageUtils;
import com.client.utils.ValidationUtils;
import com.client.views.PostView;
import javafx.application.Platform;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PostController extends BaseController {
    private final PostView view;
    private final Post post;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");

    public PostController(PostView view, Post post) {
        this.view = view;
        this.post = post;

        // Initialize base controller components
        initializeBaseController();

        // Then initialize this controller
        initialize();
        subscribeToEvents();
    }

    @Override
    protected void initialize() {
        if (view != null && post != null) {
            setupPostData();
            setupEventHandlers();
        }
    }

    @Override
    protected void subscribeToEvents() {
        eventBus.subscribe(PostEvent.LikeToggled.class, this::handleLikeToggled);
        eventBus.subscribe(PostEvent.BookmarkToggled.class, this::handleBookmarkToggled);
    }

    private void setupPostData() {
        // Validate and sanitize post data before displaying
        String username = ValidationUtils.sanitizeInput(post.getUsername());
        String content = ValidationUtils.sanitizeInput(post.getContent());

        if (!ValidationUtils.isValidPostContent(content)) {
            content = "Invalid post content";
        }

        if (ValidationUtils.isEmpty(username)) {
            username = "Unknown User";
        }

        // Set user information
        view.getUsernameLabel().setText(username);
        view.getHandleLabel().setText("@" + username.toLowerCase().replaceAll("[^a-zA-Z0-9_]", ""));

        // Set post content
        view.getContentLabel().setText(content);

        // Set like count (ensure non-negative)
        int likeCount = Math.max(0, post.getLikeCount());
        view.getLikeCountLabel().setText(String.valueOf(likeCount));

        // Set like state
        updateLikeState(post.isLiked());

        // Set bookmark state
        updateBookmarkState(post.isBookmarked());

        // Load user avatar
        ImageUtils.loadUserAvatar(post.getUserId(), view.getAvatarImage());

        // Load post image if available
        if (ValidationUtils.isNotEmpty(post.getImageUrl()) && ValidationUtils.isValidUrl(post.getImageUrl())) {
            ImageUtils.loadPostImage(post.getImageUrl(), view.getPostImageView());
            view.getImageStackPane().setVisible(true);
            view.getImageStackPane().setManaged(true);
        } else {
            // Completely remove the image container from layout when there's no image
            view.getImageStackPane().setVisible(false);
            view.getImageStackPane().setManaged(false);
        }

        // Set timestamp tooltip
        if (post.getCreatedAt() != null && !post.getCreatedAt().isEmpty()) {
            try {
                LocalDateTime createdAt = LocalDateTime.parse(post.getCreatedAt());
                String timeText = createdAt.format(TIME_FORMATTER);
                view.getContentLabel().setTooltip(new javafx.scene.control.Tooltip(timeText));
            } catch (Exception e) {
                // Fallback to raw string if parsing fails
                view.getContentLabel().setTooltip(new javafx.scene.control.Tooltip(post.getCreatedAt()));
            }
        }
    }

    private void setupEventHandlers() {
        // Like button handler
        view.getLikeIcon().setOnMouseClicked(e -> handleLikeClick());

        // Bookmark button handler
        view.getBookmarkIcon().setOnMouseClicked(e -> handleBookmarkClick());

        // More options handler
        view.getMoreOptionsButton().setOnAction(e -> handleMoreOptions());

        // Post image click handler
        view.getPostImageView().setOnMouseClicked(e -> handlePostImageClick());
    }

    private void handleLikeClick() {
        if (post == null) {
            System.out.println("Cannot like: post is null");
            return;
        }

        // Disable the like button to prevent multiple clicks
        view.getLikeIcon().setDisable(true);
        
        // Get the new state we want to set
        boolean newLikedState = !post.isLiked();
        int currentLikeCount = post.getLikeCount();
        int newLikeCount = newLikedState ? currentLikeCount + 1 : Math.max(0, currentLikeCount - 1);
        
        // Optimistically update the UI
        updateLikeState(newLikedState);
        view.getLikeCountLabel().setText(String.valueOf(newLikeCount));
        
        System.out.println("Optimistic update - Post ID: " + post.getId() +
                         ", Liked: " + newLikedState + 
                         ", Count: " + newLikeCount);
        
        // Send request to server
        postService.likePost(post.getId()).whenComplete((result, error) -> {
            Platform.runLater(() -> {
                // Re-enable the button
                view.getLikeIcon().setDisable(false);
                
                if (error != null) {
                    // Revert optimistic update on error
                    updateLikeState(!newLikedState);
                    view.getLikeCountLabel().setText(String.valueOf(currentLikeCount));
                    System.err.println("Failed to update like state: " + error.getMessage());
                    return;
                }
                
                // Update UI based on server response
                if (result != null && result) {
                    // The server will send a LikeToggled event with the final state
                    // No need to update the UI here as it's already been updated optimistically
                    System.out.println("Server confirmed like state - Post ID: " + post.getId() +
                                   ", Liked: " + newLikedState + 
                                   ", Count: " + newLikeCount);
                } else {
                    // Revert optimistic update on failure
                    updateLikeState(!newLikedState);
                    view.getLikeCountLabel().setText(String.valueOf(currentLikeCount));
                    System.err.println("Server returned failure when toggling like");
                }
            });
        });
    }

    private void handleBookmarkClick() {
        if (post == null) {
            System.out.println("Cannot bookmark: post is null");
            return;
        }

        // Toggle bookmark state optimistically
        boolean newBookmarkedState = !post.isBookmarked();
        
        // Update the model
        post.setBookmarked(newBookmarkedState);
        
        // Update UI immediately for better responsiveness
        updateBookmarkState(newBookmarkedState);
        
        // Log the optimistic update
        System.out.println("Optimistic update - Post ID: " + post.getId() + 
                         ", Bookmarked: " + newBookmarkedState);
        
        // Send request to server
        postService.bookmarkPost(post.getId()).whenComplete((_success, error) -> {
            if (error != null) {
                // Revert optimistic update on error
                boolean revertBookmarkedState = !newBookmarkedState;
                
                Platform.runLater(() -> {
                    post.setBookmarked(revertBookmarkedState);
                    updateBookmarkState(revertBookmarkedState);
                    
                    System.err.println("Failed to update bookmark state: " + error.getMessage());
                });
            }
        });
    }

    private void handleMoreOptions() {
        // Show context menu with options like delete, edit, report, etc.
        // Implementation depends on requirements
        showInfo("More Options", "More options menu would appear here");
    }

    private void handlePostImageClick() {
        // Open image in full screen or larger view
        // Implementation depends on requirements
        if (ValidationUtils.isNotEmpty(post.getImageUrl())) {
            showInfo("Image Viewer", "Full image viewer would open here");
        }
    }

    private void updateLikeState(boolean liked) {
        if (view == null || view.getLikeIcon() == null) {
            return;
        }
        
        // Get current classes for debugging
        String currentClasses = String.join(" ", view.getLikeIcon().getStyleClass());
        System.out.println("Before update - Current classes: " + currentClasses);
        
        // Remove any existing like-related classes
        boolean removed = view.getLikeIcon().getStyleClass().removeIf(style -> 
            style.equals("fas-heart") || 
            style.equals("far-heart") || 
            style.equals("liked")
        );
        System.out.println("Removed old icon classes: " + removed);
        
        // Add the appropriate classes
        if (liked) {
            view.getLikeIcon().getStyleClass().add("fas-heart");
            view.getLikeIcon().getStyleClass().add("liked");
            System.out.println("Added fas-heart and liked classes");
        } else {
            view.getLikeIcon().getStyleClass().add("far-heart");
            System.out.println("Added far-heart class");
        }
        
        // Force update the icon literal
        String iconLiteral = liked ? "fas-heart" : "far-heart";
        view.getLikeIcon().setIconLiteral(iconLiteral);
        
        // Log the final state
        String finalClasses = String.join(" ", view.getLikeIcon().getStyleClass());
        System.out.println("updateLikeState - Liked: " + liked + 
                         ", Current classes: " + finalClasses + 
                         ", Icon literal: " + iconLiteral);
    }

    private void updateBookmarkState(boolean bookmarked) {
        if (view == null) return;

        // First remove any existing icon classes to prevent duplicates
        view.getBookmarkIcon().getStyleClass().removeAll("fas-bookmark", "far-bookmark", "bookmarked");
        
        if (bookmarked) {
            view.getBookmarkIcon().getStyleClass().addAll("fas-bookmark", "bookmarked");
        } else {
            view.getBookmarkIcon().getStyleClass().add("far-bookmark");
        }
        
        // Force update the icon literal
        view.getBookmarkIcon().setIconLiteral(bookmarked ? "fas-bookmark" : "far-bookmark");
    }

    private void handleLikeToggled(PostEvent.LikeToggled event) {
        if (post == null || event.getPostId() != post.getId()) {
            return;
        }

        Platform.runLater(() -> {
            boolean currentLiked = post.isLiked();
            int currentLikeCount = post.getLikeCount();
            boolean newLikedState = event.isLiked();
            int newLikeCount = Math.max(0, event.getLikeCount());
            
            // Debug logging
            System.out.println("Processing LikeToggled event - Current: " + currentLiked + 
                             ", New: " + newLikedState + 
                             ", CurrentCount: " + currentLikeCount + 
                             ", NewCount: " + newLikeCount);
            
            // Only update if the state is actually different
            if (currentLiked != newLikedState || currentLikeCount != newLikeCount) {
                // Update the model
                post.setLiked(newLikedState);
                post.setLikeCount(newLikeCount);
                
                // Update the UI
                updateLikeState(newLikedState);
                view.getLikeCountLabel().setText(String.valueOf(newLikeCount));
                
                System.out.println("Like state updated from event - Post ID: " + post.getId() + 
                                 ", Liked: " + newLikedState + 
                                 ", Count: " + newLikeCount);
            } else {
                System.out.println("Ignoring duplicate like toggle event - state unchanged");
            }
        });
    }

    private void handleBookmarkToggled(PostEvent.BookmarkToggled event) {
        if (post != null && event.getPostId() == post.getId()) {
            Platform.runLater(() -> {
                // Update the post's bookmark state
                boolean newBookmarkedState = event.isBookmarked();
                
                // Update the model
                post.setBookmarked(newBookmarkedState);
                
                // Update the UI
                updateBookmarkState(newBookmarkedState);
                
                // Log for debugging
                System.out.println("Bookmark toggled - Post ID: " + post.getId() + 
                                 ", Bookmarked: " + newBookmarkedState);
            });
        }
    }

    public Post getPost() {
        return post;
    }
}