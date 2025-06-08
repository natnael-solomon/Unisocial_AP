package com.client.views;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class FeedView extends BorderPane {

    // Top Section
    private HBox customTitleBar;
    private Circle minimizeButtonFeed;
    private Circle maximizeButtonFeed;
    private Circle closeButtonFeed;

    // Left Sidebar
    private VBox leftSidebar;
    private ImageView profileAvatar;
    private Label profileFullName;
    private Label profileUserHandle;
    private Label profileBio;
    private Label profileFollowingCount;
    private Label profileFollowersCount;
    private FontIcon activityIcon;
    private Label activityLabel;

    // Center Content Area
    private VBox centerFeedArea;
    private TextField postTextField;
    private Button createPostButton;
    private ScrollPane feedScrollPane;
    private VBox postsVBox;

    // Right Sidebar
    private VBox rightSidebar;
    private TextField searchTwitterField;
    private VBox trendsContainer;
    private VBox whoToFollowContainer;
    private ImageView followAvatar1;
    private ImageView followAvatar2;

    public FeedView() {
        // Overall BorderPane settings
        setPrefWidth(1200);
        setPrefHeight(800);
        getStyleClass().add("root");
        getStylesheets().add(getClass().getResource("/styles/feed.css").toExternalForm());
        setPadding(new Insets(10, 10, 10, 10));

        // --- Top Section (Custom Title Bar) ---
        customTitleBar = new HBox();
        customTitleBar.setAlignment(Pos.CENTER_LEFT);
        customTitleBar.setSpacing(10.0);
        customTitleBar.getStyleClass().add("custom-title-bar");
        customTitleBar.setPadding(new Insets(5.0, 5.0, 5.0, 15.0));

        Label appNameTitlebar = new Label("Unisocial");
        appNameTitlebar.getStyleClass().add("app-name-titlebar");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox windowControls = new HBox();
        windowControls.setAlignment(Pos.CENTER_RIGHT);
        windowControls.setSpacing(10.0);

        minimizeButtonFeed = new Circle(8.0);
        minimizeButtonFeed.getStyleClass().addAll("window-control-button", "window-minimize-button");

        maximizeButtonFeed = new Circle(8.0);
        maximizeButtonFeed.getStyleClass().addAll("window-control-button", "window-maximize-button");

        closeButtonFeed = new Circle(8.0);
        closeButtonFeed.getStyleClass().addAll("window-control-button", "window-close-button");

        windowControls.getChildren().addAll(minimizeButtonFeed, maximizeButtonFeed, closeButtonFeed);
        customTitleBar.getChildren().addAll(appNameTitlebar, spacer, windowControls);
        setTop(customTitleBar);

        // --- Left Sidebar ---
        leftSidebar = new VBox();
        leftSidebar.setSpacing(15);
        leftSidebar.getStyleClass().add("sidebar-left-profile");
        leftSidebar.setPadding(new Insets(20, 15, 20, 15));

        profileAvatar = new ImageView();
        profileAvatar.setFitHeight(220);
        profileAvatar.setFitWidth(220);
        profileAvatar.getStyleClass().add("profile-avatar-large");

        profileFullName = new Label("Current User");
        profileFullName.getStyleClass().add("profile-name-large");

        profileUserHandle = new Label("fix@currentuser");
        profileUserHandle.getStyleClass().add("profile-handle");

        profileBio = new Label("This is a sample bio. JavaFX enthusiast!");
        profileBio.getStyleClass().add("profile-bio");
        profileBio.setWrapText(true);
        profileBio.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        HBox profileStats = new HBox();
        profileStats.setAlignment(Pos.CENTER);
        profileStats.setSpacing(20);
        profileStats.getStyleClass().add("profile-stats");

        VBox followingVBox = new VBox();
        followingVBox.setAlignment(Pos.CENTER);
        Label followingLabel = new Label("Following");
        followingLabel.getStyleClass().add("profile-stat-label");
        profileFollowingCount = new Label("150");
        profileFollowingCount.getStyleClass().add("profile-stat-value");
        followingVBox.getChildren().addAll(followingLabel, profileFollowingCount);

        VBox followersVBox = new VBox();
        followersVBox.setAlignment(Pos.CENTER);
        Label followersLabel = new Label("Followers");
        followersLabel.getStyleClass().add("profile-stat-label");
        profileFollowersCount = new Label("350K");
        profileFollowersCount.getStyleClass().add("profile-stat-value");
        followersVBox.getChildren().addAll(followersLabel, profileFollowersCount);

        profileStats.getChildren().addAll(followingVBox, followersVBox);

        HBox activityStatus = new HBox();
        activityStatus.setAlignment(Pos.CENTER);
        activityStatus.setSpacing(5);
        activityStatus.getStyleClass().add("activity-status");

        activityIcon = new FontIcon(FontAwesomeSolid.CIRCLE);
        activityIcon.getStyleClass().add("activity-icon");

        activityLabel = new Label("Active Now");
        activityLabel.getStyleClass().add("activity-label");
        activityStatus.getChildren().addAll(activityIcon, activityLabel);

        Button editProfileButton = new Button("Edit Profile");
        editProfileButton.getStyleClass().add("button-secondary");

        leftSidebar.getChildren().addAll(profileAvatar, profileFullName, profileUserHandle, profileBio, profileStats, activityStatus, editProfileButton);
        setLeft(leftSidebar);

        // --- Center Content Area ---
        centerFeedArea = new VBox();
        centerFeedArea.getStyleClass().add("feed-content-area");
        BorderPane.setAlignment(centerFeedArea, Pos.CENTER);

        // Create Post Section
        VBox createPostSection = new VBox();
        createPostSection.setPrefHeight(48.0);
        createPostSection.setSpacing(10);
        createPostSection.getStyleClass().add("create-post-section");

        HBox createPostHBox = new HBox();
        createPostHBox.setPrefHeight(85.0);
        createPostHBox.setSpacing(10);
        createPostHBox.setStyle("-fx-alignment: center;");

        postTextField = new TextField();
        postTextField.setPrefHeight(60.0);
        postTextField.setPromptText("What's on your mind?");
        postTextField.getStyleClass().add("text-field-glowing");
        HBox.setHgrow(postTextField, Priority.ALWAYS);

        createPostButton = new Button("Post");
        createPostButton.setAlignment(Pos.CENTER);
        createPostButton.setPrefHeight(60.0);
        createPostButton.setPrefWidth(150.0);
        createPostButton.getStyleClass().add("button");

        createPostHBox.getChildren().addAll(postTextField, createPostButton);
        createPostSection.getChildren().add(createPostHBox);

        // Feed ScrollPane
        feedScrollPane = new ScrollPane();
        feedScrollPane.setFitToWidth(true);
        feedScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        feedScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        feedScrollPane.getStyleClass().add("scroll-pane");
        VBox.setVgrow(feedScrollPane, Priority.ALWAYS);

        postsVBox = new VBox();
        postsVBox.setAlignment(Pos.CENTER);
        postsVBox.setSpacing(10);
        postsVBox.setStyle("-fx-background-color: #15202B; -fx-padding: 10");

        feedScrollPane.setContent(postsVBox);
        centerFeedArea.getChildren().addAll(createPostSection, feedScrollPane);
        setCenter(centerFeedArea);

        // --- Right Sidebar ---
        rightSidebar = new VBox();
        rightSidebar.setPrefWidth(350);
        rightSidebar.setSpacing(20);
        rightSidebar.getStyleClass().add("sidebar-right");
        rightSidebar.setPadding(new Insets(10, 15, 15, 15));

        searchTwitterField = new TextField();
        searchTwitterField.setPromptText("Search Twitter");
        searchTwitterField.getStyleClass().add("search-bar-right");

        // Trends Section
        VBox trendsSection = new VBox();
        trendsSection.setSpacing(10);
        trendsSection.getStyleClass().add("sidebar-section");
        Label trendsTitle = new Label("Trends for you");
        trendsTitle.getStyleClass().add("sidebar-section-title");
        trendsContainer = new VBox();
        trendsContainer.setSpacing(15);
        Label trend1 = new Label("#JavaFXDev");
        trend1.getStyleClass().add("text-link");
        Label trend2 = new Label("#DarkModeMagic");
        trend2.getStyleClass().add("text-link");
        Label trend3 = new Label("#OpenJFX");
        trend3.getStyleClass().add("text-link");
        Label showMoreTrends = new Label("Show more");
        showMoreTrends.getStyleClass().addAll("text-link");
        showMoreTrends.setStyle("-fx-font-size: 14px;");
        trendsContainer.getChildren().addAll(trend1, trend2, trend3, showMoreTrends);
        trendsSection.getChildren().addAll(trendsTitle, trendsContainer);

        // Who to Follow Section
        VBox whoToFollowSection = new VBox();
        whoToFollowSection.setSpacing(10);
        whoToFollowSection.getStyleClass().add("sidebar-section");
        Label whoToFollowTitle = new Label("Who to follow");
        whoToFollowTitle.getStyleClass().add("sidebar-section-title");
        whoToFollowContainer = new VBox();
        whoToFollowContainer.setSpacing(15);

        // Add sample follow suggestions
        HBox followSuggestion1 = createFollowSuggestion("John Doe", "@johndoe");
        HBox followSuggestion2 = createFollowSuggestion("Jane Smith", "@janesmith");

        whoToFollowContainer.getChildren().addAll(followSuggestion1, followSuggestion2);
        whoToFollowSection.getChildren().addAll(whoToFollowTitle, whoToFollowContainer);

        rightSidebar.getChildren().addAll(searchTwitterField, trendsSection, whoToFollowSection);
        setRight(rightSidebar);
    }

    private HBox createFollowSuggestion(String name, String handle) {
        HBox suggestion = new HBox();
        suggestion.setSpacing(10);
        suggestion.setAlignment(Pos.CENTER_LEFT);

        ImageView avatar = new ImageView();
        avatar.setFitHeight(40);
        avatar.setFitWidth(40);
        avatar.getStyleClass().add("follow-avatar");

        VBox userInfo = new VBox();
        userInfo.setSpacing(2);
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("follow-name");
        Label handleLabel = new Label(handle);
        handleLabel.getStyleClass().add("follow-handle");
        userInfo.getChildren().addAll(nameLabel, handleLabel);

        Button followButton = new Button("Follow");
        followButton.getStyleClass().add("button-secondary");
        followButton.setPrefWidth(80);

        suggestion.getChildren().addAll(avatar, userInfo, followButton);
        return suggestion;
    }

    // Getters
    public HBox getCustomTitleBar() {
        return customTitleBar;
    }

    public Circle getMinimizeButtonFeed() {
        return minimizeButtonFeed;
    }

    public Circle getMaximizeButtonFeed() {
        return maximizeButtonFeed;
    }

    public Circle getCloseButtonFeed() {
        return closeButtonFeed;
    }

    public VBox getLeftSidebar() {
        return leftSidebar;
    }

    public ImageView getProfileAvatar() {
        return profileAvatar;
    }

    public Label getProfileFullName() {
        return profileFullName;
    }

    public Label getProfileUserHandle() {
        return profileUserHandle;
    }

    public Label getProfileBio() {
        return profileBio;
    }

    public Label getProfileFollowingCount() {
        return profileFollowingCount;
    }

    public Label getProfileFollowersCount() {
        return profileFollowersCount;
    }

    public FontIcon getActivityIcon() {
        return activityIcon;
    }

    public Label getActivityLabel() {
        return activityLabel;
    }

    public TextField getPostTextField() {
        return postTextField;
    }

    public Button getCreatePostButton() {
        return createPostButton;
    }

    public ScrollPane getFeedScrollPane() {
        return feedScrollPane;
    }

    public VBox getPostsVBox() {
        return postsVBox;
    }

    public VBox getRightSidebar() {
        return rightSidebar;
    }

    public TextField getSearchTwitterField() {
        return searchTwitterField;
    }

    public VBox getTrendsContainer() {
        return trendsContainer;
    }

    public VBox getWhoToFollowContainer() {
        return whoToFollowContainer;
    }

    public ImageView getFollowAvatar1() {
        return followAvatar1;
    }

    public ImageView getFollowAvatar2() {
        return followAvatar2;
    }
}