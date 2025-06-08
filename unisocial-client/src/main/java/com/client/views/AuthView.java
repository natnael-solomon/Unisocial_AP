package com.client.views;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.util.Objects;

public class AuthView extends AnchorPane {

    private ImageView authWelcomeImage;
    private Circle closeButton;
    private StackPane contentArea;
    private ToggleSwitch viewToggle;

    public AuthView() {
        // Set up the AnchorPane
        setPrefHeight(500.0);
        setPrefWidth(712.0);
        getStyleClass().add("auth-card");
        getStylesheets().add(getClass().getResource("/styles/auth.css").toExternalForm());

        // Add ImageView for the image
        authWelcomeImage = new ImageView();
        authWelcomeImage.setFitHeight(486.0);
        authWelcomeImage.setFitWidth(342.0);
        authWelcomeImage.setLayoutX(7.0);
        authWelcomeImage.setLayoutY(7.0);
        authWelcomeImage.setPickOnBounds(true);
        authWelcomeImage.setPreserveRatio(true);
        authWelcomeImage.getStyleClass().add("auth-image");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/welcome_image.png")));
        authWelcomeImage.setImage(image);

        // Add Circle for close button
        closeButton = new Circle();
        closeButton.setLayoutX(686.0);
        closeButton.setLayoutY(20.0);
        closeButton.setRadius(10.0);
        closeButton.getStyleClass().add("close-button");

        // Add StackPane for content area (Login/Signup views)
        contentArea = new StackPane();
        contentArea.setLayoutX(409.5);
        contentArea.setLayoutY(150.0);
        contentArea.setPrefHeight(270.0);
        contentArea.setPrefWidth(241.0);

        // Instantiate ToggleSwitch
        viewToggle = new ToggleSwitch();
        viewToggle.setLayoutX(430.3);
        viewToggle.setLayoutY(94.0);

        // Add children to the AnchorPane in correct order
        getChildren().addAll(authWelcomeImage, closeButton, viewToggle, contentArea);
    }

    public StackPane getContentArea() {
        return contentArea;
    }

    public Circle getCloseButton() {
        return closeButton;
    }

    public ToggleSwitch getViewToggle() {
        return viewToggle;
    }
}