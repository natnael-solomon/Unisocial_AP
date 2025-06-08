package com.client.controllers;

import com.client.events.PostEvent;
import com.client.models.Post;
import com.client.utils.ImageUtils;
import com.client.utils.ValidationUtils;
import com.client.views.PostView;
import javafx.application.Platform;
import java.time.format.DateTimeFormatter;

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
        } else {
            view.getImageStackPane().setVisible(false);
        }

        // Set timestamp tooltip
        if (post.getCreatedAt() != null) {
            String timeText = post.getCreatedAt().format(TIME_FORMATTER);
            view.getContentLabel().setTooltip(new javafx.scene.control.Tooltip(timeText));
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
        if (post == null) return;

        // Toggle like state optimistically
        boolean newLikedState = !post.isLiked();
        post.setLiked(newLikedState);

        // Update like count
        int newLikeCount = newLikedState ? post.getLikeCount() + 1 : Math.max(0, post.getLikeCount() - 1);
        post.setLikeCount(newLikeCount);

        // Update UI
        updateLikeState(newLikedState);
        view.getLikeCountLabel().setText(String.valueOf(newLikeCount));

        // Send request to server
        postService.likePost(post.getId());
    }

    private void handleBookmarkClick() {
        if (post == null) return;

        // Toggle bookmark state optimistically
        boolean newBookmarkedState = !post.isBookmarked();
        post.setBookmarked(newBookmarkedState);

        // Update UI
        updateBookmarkState(newBookmarkedState);

        // Send request to server
        postService.bookmarkPost(post.getId());
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
        if (view == null) return;

        if (liked) {
            view.getLikeIcon().setIconLiteral("fas-heart");
            view.getLikeIcon().getStyleClass().add("liked");
        } else {
            view.getLikeIcon().setIconLiteral("far-heart");
            view.getLikeIcon().getStyleClass().remove("liked");
        }
    }

    private void updateBookmarkState(boolean bookmarked) {
        if (view == null) return;

        if (bookmarked) {
            view.getBookmarkIcon().setIconLiteral("fas-bookmark");
            view.getBookmarkIcon().getStyleClass().add("bookmarked");
        } else {
            view.getBookmarkIcon().setIconLiteral("far-bookmark");
            view.getBookmarkIcon().getStyleClass().remove("bookmarked");
        }
    }

    private void handleLikeToggled(PostEvent.LikeToggled event) {
        if (post != null && event.getPostId() == post.getId()) {
            Platform.runLater(() -> {
                post.setLiked(event.isLiked());
                post.setLikeCount(Math.max(0, event.getLikeCount()));
                updateLikeState(event.isLiked());
                view.getLikeCountLabel().setText(String.valueOf(event.getLikeCount()));
            });
        }
    }

    private void handleBookmarkToggled(PostEvent.BookmarkToggled event) {
        if (post != null && event.getPostId() == post.getId()) {
            Platform.runLater(() -> {
                post.setBookmarked(event.isBookmarked());
                updateBookmarkState(event.isBookmarked());
            });
        }
    }

    public Post getPost() {
        return post;
    }
}