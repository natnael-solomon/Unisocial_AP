package com.client.controllers;

import com.client.events.PostEvent;
import com.client.models.Post;
import com.client.models.User;
import com.client.utils.ImageUtils;
import com.client.utils.ValidationUtils;
import com.client.views.FeedView;
import com.client.views.PostView;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.util.List;

public class FeedController extends BaseController {
    private final FeedView view;
    private double xOffset = 0;
    private double yOffset = 0;

    public FeedController(FeedView view) {
        this.view = view;

        // Initialize base controller components
        initializeBaseController();

        // Then initialize this controller
        initialize();
        subscribeToEvents();
    }

    @Override
    protected void initialize() {
        setupUserProfile();
        setupWindowControls();
        setupPostCreation();
        setupWindowDragging();
        loadSampleData(); // Load sample data initially
        loadInitialData();
    }

    @Override
    protected void subscribeToEvents() {
        eventBus.subscribe(PostEvent.CreateSuccess.class, this::handlePostCreated);
        eventBus.subscribe(PostEvent.CreateFailure.class, this::handlePostCreateFailure);
        eventBus.subscribe(PostEvent.FeedLoaded.class, this::handleFeedLoaded);
        eventBus.subscribe(PostEvent.LikeToggled.class, this::handleLikeToggled);
    }

    private void setupUserProfile() {
        User currentUser = appState.getCurrentUser();
        if (currentUser != null) {
            // Validate and sanitize user data
            String fullName = ValidationUtils.sanitizeInput(currentUser.getFullName());
            String username = ValidationUtils.sanitizeInput(currentUser.getUsername());
            String bio = ValidationUtils.sanitizeInput(currentUser.getBio());

            view.getProfileFullName().setText(ValidationUtils.isNotEmpty(fullName) ? fullName : "Current User");
            view.getProfileUserHandle().setText("@" + (ValidationUtils.isNotEmpty(username) ? username : "user"));
            view.getProfileFollowingCount().setText(String.valueOf(Math.max(0, currentUser.getFollowingCount())));
            view.getProfileFollowersCount().setText(String.valueOf(Math.max(0, currentUser.getFollowersCount())));

            if (ValidationUtils.isValidBio(bio) && ValidationUtils.isNotEmpty(bio)) {
                view.getProfileBio().setText(bio);
            } else {
                view.getProfileBio().setText("Welcome to UniSocial!");
            }

            // Load user avatar
            ImageUtils.loadUserAvatar(currentUser.getId(), view.getProfileAvatar());
        }
    }

    private void setupWindowControls() {
        view.getMinimizeButtonFeed().setOnMouseClicked(e -> minimizeWindow());
        view.getMaximizeButtonFeed().setOnMouseClicked(e -> toggleMaximizeWindow());
        view.getCloseButtonFeed().setOnMouseClicked(e -> closeWindow());
    }

    private void setupPostCreation() {
        view.getCreatePostButton().setOnAction(e -> handleCreatePost());
        view.getPostTextField().setOnAction(e -> handleCreatePost());

        // Real-time character counter and validation
        view.getPostTextField().textProperty().addListener((obs, oldText, newText) -> {
            updatePostValidation(newText);
        });

        // Initial state
        updatePostValidation("");
    }

    private void updatePostValidation(String content) {
        if (view == null) return;

        // Sanitize content for validation
        String sanitized = ValidationUtils.sanitizeInput(content);
        int length = sanitized != null ? sanitized.trim().length() : 0;
        int remaining = ValidationUtils.MAX_POST_LENGTH - length;

        // Update button state
        boolean isValid = ValidationUtils.isValidPostContent(sanitized);
        view.getCreatePostButton().setDisable(!isValid);

        // Update button text with character count
        if (remaining < 20) {
            view.getCreatePostButton().setText("Post (" + remaining + ")");
        } else {
            view.getCreatePostButton().setText("Post");
        }

        // Change text field style based on validation
        if (length > 0 && !isValid) {
            view.getPostTextField().getStyleClass().add("text-field-error");
        } else {
            view.getPostTextField().getStyleClass().remove("text-field-error");
        }
    }

    private void setupWindowDragging() {
        view.getCustomTitleBar().setOnMousePressed(this::handleMousePressed);
        view.getCustomTitleBar().setOnMouseDragged(this::handleMouseDragged);
    }

    private void loadSampleData() {
        // Add sample posts to demonstrate the UI with validated content
        addSamplePost("Jane Doe", "@janedoe",
                ValidationUtils.sanitizeInput("Just implemented a Twitter clone with JavaFX! #JavaFX #Programming"),
                LocalDateTime.now().minusHours(1), 110);
        addSamplePost("Tech News", "@technews",
                ValidationUtils.sanitizeInput("Java 21 is now available with exciting new features for developers! Check out the official release notes. #Java21 #JDK"),
                LocalDateTime.now().minusMinutes(30), 256);
        addSamplePost("JavaFX Fan", "@javafxfan",
                ValidationUtils.sanitizeInput("Creating beautiful UIs with JavaFX is so satisfying. Loved working on this feed! #UIUX #DarkMode"),
                LocalDateTime.now().minusHours(2), 75);
        addSamplePost("User One", "@user1",
                ValidationUtils.sanitizeInput("Exploring the new dark mode. It looks sleek!"),
                LocalDateTime.now().minusDays(1), 5);
    }

    private void addSamplePost(String username, String handle, String content, LocalDateTime timestamp, int likeCount) {
        // Validate all post data before creating
        if (!ValidationUtils.isValidUsername(username.replace("@", "")) ||
                !ValidationUtils.isValidPostContent(content)) {
            return; // Skip invalid posts
        }

        Post post = new Post();
        post.setId((int) (Math.random() * 10000));
        post.setUserId((int) (Math.random() * 100));
        post.setUsername(ValidationUtils.sanitizeInput(username));
        post.setContent(ValidationUtils.sanitizeInput(content));
        post.setCreatedAt(timestamp != null ? timestamp.toString() : LocalDateTime.now().toString());
        post.setUpdatedAt(LocalDateTime.now().toString());
        post.setLikeCount(Math.max(0, likeCount)); // Ensure non-negative
        post.setLiked(false);
        post.setBookmarked(false);

        PostView postView = new PostView();
        PostController postController = new PostController(postView, post);
        view.getPostsVBox().getChildren().add(postView);
    }

    private void loadInitialData() {
        postService.getFeed();
    }

    private void handleCreatePost() {
        String content = view.getPostTextField().getText();

        // Sanitize and validate content
        String sanitizedContent = ValidationUtils.sanitizeInput(content);
        String validationError = ValidationUtils.getPostContentValidationError(sanitizedContent);

        if (validationError != null) {
            showError("Invalid Post", validationError);
            return;
        }

        setPostCreationLoading(true);
        postService.createPost(sanitizedContent.trim());
    }

    private void handlePostCreated(PostEvent.CreateSuccess event) {
        Platform.runLater(() -> {
            view.getPostTextField().clear();
            setPostCreationLoading(false);

            // Add the new post to the top of the feed with validation
            User currentUser = appState.getCurrentUser();
            if (currentUser != null) {
                String username = ValidationUtils.sanitizeInput(currentUser.getUsername());
                String content = ValidationUtils.sanitizeInput(event.getContent());

                if (ValidationUtils.isValidUsername(username) && ValidationUtils.isValidPostContent(content)) {
                    addSamplePost(username, "@" + username, content, LocalDateTime.now(), 0);
                }
            }
        });
    }

    private void handlePostCreateFailure(PostEvent.CreateFailure event) {
        Platform.runLater(() -> {
            setPostCreationLoading(false);
            showError("Post Creation Failed", ValidationUtils.sanitizeInput(event.getMessage()));
        });
    }

    private void handleFeedLoaded(PostEvent.FeedLoaded event) {
        Platform.runLater(() -> {
            displayPosts(event.getPosts());
        });
    }

    private void handleLikeToggled(PostEvent.LikeToggled event) {
        // The individual post controller handles the UI update
        // This could trigger a feed refresh if needed
    }

    private void displayPosts(List<Post> posts) {
        view.getPostsVBox().getChildren().clear();

        for (Post post : posts) {
            // Validate post data before displaying
            if (post != null &&
                    ValidationUtils.isValidPostContent(post.getContent()) &&
                    ValidationUtils.isNotEmpty(post.getUsername())) {

                // Sanitize post data
                post.setUsername(ValidationUtils.sanitizeInput(post.getUsername()));
                post.setContent(ValidationUtils.sanitizeInput(post.getContent()));
                post.setLikeCount(Math.max(0, post.getLikeCount())); // Ensure non-negative

                PostView postView = new PostView();
                PostController postController = new PostController(postView, post);
                view.getPostsVBox().getChildren().add(postView);
            }
        }
    }

    private void setPostCreationLoading(boolean loading) {
        view.getCreatePostButton().setDisable(loading);
        view.getCreatePostButton().setText(loading ? "Posting..." : "Post");
        view.getPostTextField().setDisable(loading);
    }

    private void handleMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    private void handleMouseDragged(MouseEvent event) {
        if (view != null && view.getScene() != null && view.getScene().getWindow() != null) {
            Stage stage = (Stage) view.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }

    private void minimizeWindow() {
        getStage().setIconified(true);
    }

    private void toggleMaximizeWindow() {
        Stage stage = getStage();
        stage.setMaximized(!stage.isMaximized());
    }

    private void closeWindow() {
        authService.logout();
        getStage().close();
    }

    private Stage getStage() {
        return (Stage) view.getScene().getWindow();
    }
}