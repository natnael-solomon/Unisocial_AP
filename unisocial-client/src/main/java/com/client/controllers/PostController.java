package com.client.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.kordamp.ikonli.javafx.FontIcon;

import com.client.events.PostEvent;
import com.client.models.Post;
import com.client.models.User;
import com.client.utils.ImageUtils;
import com.client.utils.Logger;
import com.client.utils.ValidationUtils;
import com.client.views.PostView;

import javafx.application.Platform;

public class PostController extends BaseController {

    private static final String ICON_HEART_SOLID = "fas-heart";
    private static final String ICON_HEART_REGULAR = "far-heart";
    private static final String ICON_BOOKMARK_SOLID = "fas-bookmark";
    private static final String ICON_BOOKMARK_REGULAR = "far-bookmark";
    private static final String CLASS_LIKED = "liked";
    private static final String CLASS_BOOKMARKED = "bookmarked";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");

    private final PostView view;
    private final Post post;
    private final AtomicBoolean likeOperationInProgress = new AtomicBoolean(false);
    private final AtomicBoolean bookmarkOperationInProgress = new AtomicBoolean(false);
    private final AtomicInteger pendingLikeCount = new AtomicInteger(0);

    public PostController(PostView view, Post post) {
        this.view = view;
        this.post = post;
        initializeBaseController();
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
                view.getContentLabel().setTooltip(new javafx.scene.control.Tooltip(post.getCreatedAt()));
            }
        }
    }

    private void setupEventHandlers() {
        // Like button handler
        view.getLikeIcon().setOnMouseClicked(e -> handleLikeClick());

        // Bookmark button handler
        view.getBookmarkIcon().setOnMouseClicked(e -> handleBookmarkClick());

        // More options handler - show delete icon for post creator
        User currentUser = appState.getCurrentUser();
        if (currentUser != null && post.getUserId() == currentUser.getId()) {
            // Replace button with just the icon
            FontIcon deleteIcon = new FontIcon("fas-trash");
            deleteIcon.getStyleClass().add("icon-button-graphic");
            view.getMoreOptionsButton().setGraphic(deleteIcon);
            view.getMoreOptionsButton().getStyleClass().clear();
            view.getMoreOptionsButton().getStyleClass().add("icon-button");

            // Add hover and click effects like other icons
            deleteIcon.setOnMouseEntered(e -> {
                deleteIcon.setIconColor(javafx.scene.paint.Color.valueOf("#E81123")); // Red color
                deleteIcon.setScaleX(1.2);
                deleteIcon.setScaleY(1.2);
            });

            deleteIcon.setOnMouseExited(e -> {
                deleteIcon.setIconColor(javafx.scene.paint.Color.valueOf("#8899A6")); // Original color
                deleteIcon.setScaleX(1.0);
                deleteIcon.setScaleY(1.0);
            });

            deleteIcon.setOnMouseClicked(e -> handleDeletePost());
        } else {
            // For non-creators, show timestamp on click
            view.getMoreOptionsButton().setOnAction(e -> {
                try {
                    LocalDateTime createdAt = LocalDateTime.parse(post.getCreatedAt());
                    String timeText = createdAt.format(TIME_FORMATTER);
                    showInfo("Post Time", timeText);
                } catch (Exception ex) {
                    Logger.error("Error parsing post timestamp: " + ex.getMessage());
                }
            });
        }

        // Post image click handler
        view.getPostImageView().setOnMouseClicked(e -> handlePostImageClick());
    }

    private void handleLikeClick() {
        if (post == null) {
            Logger.warn("Cannot like: post is null");
            return;
        }

        // Prevent multiple simultaneous like operations
        if (!likeOperationInProgress.compareAndSet(false, true)) {
            Logger.debug("Like operation already in progress for post: " + post.getId());
            return;
        }

        try {
            // Disable the like button
            view.getLikeIcon().setDisable(true);

            // Get current state
            boolean currentLikedState = post.isLiked();
            int currentLikeCount = post.getLikeCount();

            // Calculate new state
            boolean newLikedState = !currentLikedState;
            int newLikeCount = calculateNewLikeCount(currentLikeCount, newLikedState);

            // Validate the new like count
            if (!isValidLikeCount(newLikeCount)) {
                Logger.warn("Invalid like count calculated: " + newLikeCount);
                revertLikeState(currentLikeCount);
                return;
            }

            // Store pending count for rollback if needed
            pendingLikeCount.set(newLikeCount);

            // Optimistically update the UI
            updateLikeState(newLikedState);
            view.getLikeCountLabel().setText(String.valueOf(newLikeCount));

            Logger.debug("Optimistic update - Post ID: " + post.getId()
                    + ", Liked: " + newLikedState
                    + ", Count: " + newLikeCount);

            // Send request to server
            postService.likePost(post.getId())
                    .whenComplete((result, error) -> {
                        Platform.runLater(() -> {
                            try {
                                if (error != null) {
                                    handleLikeError(currentLikeCount);
                                    return;
                                }

                                if (result != null && result) {
                                    handleLikeSuccess(newLikedState, newLikeCount);
                                } else {
                                    handleLikeFailure(currentLikeCount);
                                }
                            } finally {
                                // Always re-enable the button and clear operation flag
                                view.getLikeIcon().setDisable(false);
                                likeOperationInProgress.set(false);
                            }
                        });
                    });
        } catch (Exception e) {
            Logger.error("Unexpected error during like operation: " + e.getMessage());
            revertLikeState(post.getLikeCount());
            likeOperationInProgress.set(false);
            view.getLikeIcon().setDisable(false);
        }
    }

    private int calculateNewLikeCount(int currentCount, boolean newLikedState) {
        if (newLikedState) {
            return currentCount + 1;
        } else {
            return Math.max(0, currentCount - 1);
        }
    }

    private boolean isValidLikeCount(int count) {
        return count >= 0 && count <= Integer.MAX_VALUE;
    }

    private void handleLikeSuccess(boolean newLikedState, int newLikeCount) {
        // Update the post model
        post.setLiked(newLikedState);
        post.setLikeCount(newLikeCount);

        Logger.info("Like operation successful - Post ID: " + post.getId()
                + ", Liked: " + newLikedState
                + ", Count: " + newLikeCount);
    }

    private void handleLikeError(int originalCount) {
        // Revert to original state
        revertLikeState(originalCount);
        Logger.error("Failed to update like state for post: " + post.getId());

        // Show error to user
        showError("Like Error", "Failed to update like status. Please try again.");
    }

    private void handleLikeFailure(int originalCount) {
        // Revert to original state
        revertLikeState(originalCount);
        Logger.warn("Server rejected like operation for post: " + post.getId());

        // Show warning to user
        showWarning("Like Failed", "Unable to update like status. Please try again.");
    }

    private void revertLikeState(int originalCount) {
        // Revert the like state
        boolean originalLikedState = !post.isLiked();
        updateLikeState(originalLikedState);
        view.getLikeCountLabel().setText(String.valueOf(originalCount));

        // Revert the post model
        post.setLiked(originalLikedState);
        post.setLikeCount(originalCount);

        Logger.debug("Reverted like state - Post ID: " + post.getId()
                + ", Liked: " + originalLikedState
                + ", Count: " + originalCount);
    }

    private void updateLikeState(boolean liked) {
        if (view == null || view.getLikeIcon() == null) {
            return;
        }

        // Remove existing like-related classes
        view.getLikeIcon().getStyleClass().removeIf(style
                -> style.equals(ICON_HEART_SOLID)
                || style.equals(ICON_HEART_REGULAR)
                || style.equals(CLASS_LIKED)
        );

        // Add appropriate classes
        if (liked) {
            view.getLikeIcon().getStyleClass().addAll(ICON_HEART_SOLID, CLASS_LIKED);
        } else {
            view.getLikeIcon().getStyleClass().add(ICON_HEART_REGULAR);
        }

        // Update icon literal
        view.getLikeIcon().setIconLiteral(liked ? ICON_HEART_SOLID : ICON_HEART_REGULAR);
    }

    private void handleLikeToggled(PostEvent.LikeToggled event) {
        if (post != null && event.getPostId() == post.getId()) {
            Platform.runLater(() -> {
                // Only update if the event is newer than our current state
                if (event.getTimestamp() > System.currentTimeMillis()) {
                    // Update the post model
                    post.setLiked(event.isLiked());
                    post.setLikeCount(event.getLikeCount());

                    // Update the UI
                    updateLikeState(event.isLiked());
                    view.getLikeCountLabel().setText(String.valueOf(event.getLikeCount()));

                    Logger.debug("Like state updated from event - Post ID: " + post.getId()
                            + ", Liked: " + event.isLiked()
                            + ", Count: " + event.getLikeCount());
                }
            });
        }
    }

    private void handleBookmarkClick() {
        if (post == null) {
            Logger.warn("Cannot bookmark: post is null");
            return;
        }

        // Prevent multiple simultaneous bookmark operations
        if (!bookmarkOperationInProgress.compareAndSet(false, true)) {
            Logger.debug("Bookmark operation already in progress for post ID: " + post.getId());
            return;
        }

        try {
            // Toggle bookmark state optimistically
            boolean newBookmarkedState = !post.isBookmarked();

            // Update the model
            post.setBookmarked(newBookmarkedState);

            // Update UI immediately for better responsiveness
            updateBookmarkState(newBookmarkedState);

            Logger.debug("Optimistic update - Post ID: " + post.getId()
                    + ", Bookmarked: " + newBookmarkedState);

            // Send request to server
            postService.bookmarkPost(post.getId()).whenComplete((success, error) -> {
                Platform.runLater(() -> {
                    if (error != null || !success) {
                        // Revert optimistic update on error
                        boolean revertBookmarkedState = !newBookmarkedState;
                        post.setBookmarked(revertBookmarkedState);
                        updateBookmarkState(revertBookmarkedState);

                        String errorMessage = error != null ? error.getMessage() : "Failed to update bookmark state";
                        Logger.error("Failed to update bookmark state: " + errorMessage);
                        showError("Bookmark Failed", "Unable to update bookmark. Please try again.");
                    }
                    bookmarkOperationInProgress.set(false);
                });
            });
        } catch (Exception e) {
            Logger.error("Error in handleBookmarkClick: " + e.getMessage());
            bookmarkOperationInProgress.set(false);
            showError("Bookmark Error", "An unexpected error occurred. Please try again.");
        }
    }

    private void updateBookmarkState(boolean bookmarked) {
        if (view == null || view.getBookmarkIcon() == null) {
            return;
        }

        // Remove existing bookmark-related classes
        view.getBookmarkIcon().getStyleClass().removeIf(style
                -> style.equals(ICON_BOOKMARK_SOLID)
                || style.equals(ICON_BOOKMARK_REGULAR)
                || style.equals(CLASS_BOOKMARKED)
        );

        // Add appropriate classes
        if (bookmarked) {
            view.getBookmarkIcon().getStyleClass().addAll(ICON_BOOKMARK_SOLID, CLASS_BOOKMARKED);
        } else {
            view.getBookmarkIcon().getStyleClass().add(ICON_BOOKMARK_REGULAR);
        }

        // Update icon literal
        view.getBookmarkIcon().setIconLiteral(bookmarked ? ICON_BOOKMARK_SOLID : ICON_BOOKMARK_REGULAR);
    }

    private void handleDeletePost() {
        // Optimistically remove the post from UI
        view.getPostRoot().setVisible(false);
        view.getPostRoot().setManaged(false);

        // Delete post in background
        postService.deletePost(post.getId())
                .thenAccept(success -> {
                    if (!success) {
                        // If deletion failed, show the post again
                        Platform.runLater(() -> {
                            view.getPostRoot().setVisible(true);
                            view.getPostRoot().setManaged(true);
                            showError("Delete Failed", "Could not delete the post. Please try again.");
                        });
                    }
                })
                .exceptionally(e -> {
                    // If any error occurs, show the post again
                    Platform.runLater(() -> {
                        view.getPostRoot().setVisible(true);
                        view.getPostRoot().setManaged(true);
                        showError("Delete Failed", "An error occurred while deleting the post. Please try again.");
                        Logger.error("Error deleting post: " + e.getMessage());
                    });
                    return null;
                });
    }

    private void handlePostImageClick() {
        // Open image in full screen or larger view
        if (ValidationUtils.isNotEmpty(post.getImageUrl())) {
            showInfo("Image Viewer", "Full image viewer would open here");
        }
    }

    private void handleBookmarkToggled(PostEvent.BookmarkToggled event) {
        if (post != null && event.getPostId() == post.getId()) {
            Platform.runLater(() -> {
                // Only update if the event is newer than our current state
                if (event.getTimestamp() > System.currentTimeMillis()) {
                    // Update the post model
                    post.setBookmarked(event.isBookmarked());

                    // Update the UI
                    updateBookmarkState(event.isBookmarked());

                    Logger.debug("Bookmark state updated from event - Post ID: " + post.getId()
                            + ", Bookmarked: " + event.isBookmarked());
                }
            });
        }
    }

    public Post getPost() {
        return post;
    }
}
