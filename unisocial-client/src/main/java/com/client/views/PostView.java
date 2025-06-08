package com.client.views;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class PostView extends VBox {

    // Main VBox (this class itself) - fx:id="postRoot"
    // Top Section
    private HBox topSection;
    private ImageView avatarImage;
    private Label usernameLabel;
    private Label handleLabel;
    private Button moreOptionsButton;

    // Caption Section
    private VBox captionSection;
    private Label contentLabel;
    private StackPane imageStackPane; // Not an fx:id in FXML, but useful for programmatic access to clip
    private ImageView postImageView;

    // Actions Bar
    private HBox actionsBar;
    private FontIcon likeIcon;
    private Label likeCountLabel;
    private FontIcon bookmarkIcon;

    public PostView() {
        // Main VBox properties (fx:id="postRoot")
        setAlignment(Pos.CENTER); // FXML had ALIGNMENT="CENTER", might be Pos.TOP_CENTER for actual posts
        getStyleClass().add("post-card");

        // --- Top Section ---
        topSection = new HBox();
        topSection.getStyleClass().add("post-top-section");
        topSection.setAlignment(Pos.CENTER_LEFT);
        topSection.setSpacing(8.0);
        topSection.setPadding(new Insets(8.0, 16.0, 8.0, 16.0));

        avatarImage = new ImageView();
        avatarImage.setFitHeight(60.0);
        avatarImage.setFitWidth(60.0);
        avatarImage.getStyleClass().add("avatar-small");
        // Image to be set by controller

        VBox userInfoVBox = new VBox(); // No fx:id needed for this internal VBox
        HBox.setHgrow(userInfoVBox, Priority.ALWAYS);
        userInfoVBox.getStyleClass().add("post-user-info-frame");

        usernameLabel = new Label("Username"); // Placeholder text
        usernameLabel.getStyleClass().add("text-username-bold");

        handleLabel = new Label("@handle"); // Placeholder text
        handleLabel.getStyleClass().add("text-handle-secondary");

        userInfoVBox.getChildren().addAll(usernameLabel, handleLabel);

        moreOptionsButton = new Button();
        moreOptionsButton.getStyleClass().add("icon-button-subtle");
        FontIcon moreOptionsIcon = new FontIcon("fas-ellipsis-h");
        moreOptionsIcon.getStyleClass().add("icon-button-graphic");
        moreOptionsButton.setGraphic(moreOptionsIcon);
        // Event handler to be set by controller: onAction="#handleMoreOptions"

        topSection.getChildren().addAll(avatarImage, userInfoVBox, moreOptionsButton);

        // --- Caption Section ---
        captionSection = new VBox();
        captionSection.getStyleClass().add("post-caption-section");
        captionSection.setPadding(new Insets(8.0, 16.0, 8.0, 16.0));

        contentLabel = new Label("Post content will appear here."); // Placeholder text
        contentLabel.getStyleClass().add("text-caption");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);

        imageStackPane = new StackPane(); // fx:id was on ImageView, but StackPane is key for clip
        imageStackPane.getStyleClass().add("post-image-rounded"); // Assuming this style applies to StackPane

        Rectangle imageClip = new Rectangle(600, 300); // Dimensions from FXML
        imageClip.setArcWidth(12);
        imageClip.setArcHeight(12);
        imageStackPane.setClip(imageClip); // Apply clip to StackPane

        postImageView = new ImageView();
        postImageView.setFitWidth(600); // Match clip dimensions
        postImageView.setFitHeight(300); // Match clip dimensions
        postImageView.setPreserveRatio(true); // As per FXML
        // Image to be set by controller
        // Event handler: onMouseClicked="#handlePostImageClick"

        imageStackPane.getChildren().add(postImageView);
        captionSection.getChildren().addAll(contentLabel, imageStackPane);

        // --- Actions Bar ---
        actionsBar = new HBox();
        actionsBar.getStyleClass().add("post-actions-bar");
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        actionsBar.setPadding(new Insets(16.0, 16.0, 16.0, 16.0));

        HBox leftActionsFrame = new HBox(); // No fx:id
        leftActionsFrame.getStyleClass().add("post-actions-frame-left");
        leftActionsFrame.setAlignment(Pos.CENTER_LEFT);
        leftActionsFrame.setSpacing(24.0);

        HBox likeActionGroup = new HBox(); // No fx:id
        likeActionGroup.setAlignment(Pos.CENTER_LEFT);
        likeActionGroup.setSpacing(8.0);
        likeActionGroup.getStyleClass().add("post-action-group");

        likeIcon = new FontIcon("far-heart"); // Icon literal from FXML
        likeIcon.getStyleClass().add("icon-button-graphic");
        // Event handler: onMouseClicked="#handleLikeClick"

        likeCountLabel = new Label("0"); // Placeholder text
        likeCountLabel.getStyleClass().add("text-action-count");

        likeActionGroup.getChildren().addAll(likeIcon, likeCountLabel);
        leftActionsFrame.getChildren().add(likeActionGroup);

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox rightActionsFrame = new HBox(); // This represents the bookmark group conceptually
        rightActionsFrame.setAlignment(Pos.CENTER_LEFT); // Match FXML structure (though it's just one icon)
        rightActionsFrame.setSpacing(8.0); // Match FXML structure
        rightActionsFrame.getStyleClass().add("post-action-group"); // Match FXML structure

        bookmarkIcon = new FontIcon("far-bookmark"); // Icon literal from FXML
        bookmarkIcon.getStyleClass().add("icon-button-graphic");
        // Event handler: onMouseClicked="#handleBookmarkClick"

        rightActionsFrame.getChildren().add(bookmarkIcon); // Add bookmark to its conceptual frame

        actionsBar.getChildren().addAll(leftActionsFrame, spacer, rightActionsFrame);

        // Add all sections to the main VBox (this)
        getChildren().addAll(topSection, captionSection, actionsBar);
    }

    // --- Getters for components with fx:id or needed by controller ---
    // Post Root (this VBox itself)
    public VBox getPostRoot() {
        return this;
    } // As fx:id="postRoot" was on the VBox itself

    // Top Section
    public HBox getTopSection() {
        return topSection;
    }

    public ImageView getAvatarImage() {
        return avatarImage;
    }

    public Label getUsernameLabel() {
        return usernameLabel;
    }

    public Label getHandleLabel() {
        return handleLabel;
    }

    public Button getMoreOptionsButton() {
        return moreOptionsButton;
    }

    // Caption Section
    public VBox getCaptionSection() {
        return captionSection;
    }

    public Label getContentLabel() {
        return contentLabel;
    }

    public ImageView getPostImageView() {
        return postImageView;
    }

    public StackPane getImageStackPane() {
        return imageStackPane;
    } // For controller to manage visibility perhaps

    // Actions Bar
    public HBox getActionsBar() {
        return actionsBar;
    }

    public FontIcon getLikeIcon() {
        return likeIcon;
    }

    public Label getLikeCountLabel() {
        return likeCountLabel;
    }

    public FontIcon getBookmarkIcon() {
        return bookmarkIcon;
    }
}