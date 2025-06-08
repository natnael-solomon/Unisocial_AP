package com.client.views;

import java.util.Objects;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ToggleSwitch extends StackPane {

    private static final String SIGN_IN_TEXT = "Sign In";
    private static final String SIGN_UP_TEXT = "Sign Up";

    private final Label signInLabel = new Label(SIGN_IN_TEXT);
    private final Label signUpLabel = new Label(SIGN_UP_TEXT);

    private final StackPane signInPane = new StackPane(signInLabel);
    private final StackPane signUpPane = new StackPane(signUpLabel);
    private final HBox buttonContainer = new HBox(signInPane, signUpPane);

    private final Rectangle selector = new Rectangle();

    private String currentState;
    private EventHandler<ActionEvent> onSwitchAction;

    public ToggleSwitch() {
        getStyleClass().add("toggle-container");
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/toggle.css")).toExternalForm());

        setupSelector();
        setupButtons();

        getChildren().addAll(selector, buttonContainer);
        switchToSignIn(); // default state
    }

    private void setupSelector() {
        selector.getStyleClass().add("slider-background");
        selector.setManaged(false); // prevent layout interference
    }

    private void setupButtons() {
        signInPane.getStyleClass().add("toggle-button");
        signUpPane.getStyleClass().add("toggle-button");

        StackPane.setAlignment(signInLabel, Pos.CENTER);
        StackPane.setAlignment(signUpLabel, Pos.CENTER);

        HBox.setHgrow(signInPane, Priority.ALWAYS);
        HBox.setHgrow(signUpPane, Priority.ALWAYS);

        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPrefHeight(USE_COMPUTED_SIZE);
        buttonContainer.setPrefWidth(USE_COMPUTED_SIZE);

        signInPane.setOnMouseClicked(this::handleSignInClick);
        signUpPane.setOnMouseClicked(this::handleSignUpClick);
    }

    private void handleSignInClick(MouseEvent event) {
        if (!SIGN_IN_TEXT.equals(currentState)) {
            switchToSignIn();
        }
    }

    private void handleSignUpClick(MouseEvent event) {
        if (!SIGN_UP_TEXT.equals(currentState)) {
            switchToSignUp();
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        double width = getWidth();
        double height = getHeight();

        if (width > 0 && height > 0) {
            selector.setWidth(width / 2);
            selector.setHeight(height);
            selector.setLayoutX(0);
            selector.setLayoutY(0);
        }
    }

    public void switchToSignIn() {
        currentState = SIGN_IN_TEXT;
        animateSelector(0);
    }

    public void switchToSignUp() {
        currentState = SIGN_UP_TEXT;
        animateSelector(getWidth() / 2);
    }

    private void animateSelector(double toX) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), selector);
        tt.setToX(toX);
        tt.setOnFinished(e -> {
            if (onSwitchAction != null) {
                onSwitchAction.handle(new ActionEvent(this, null));
            }
        });
        tt.play();
    }

    public String getSelectedView() {
        return currentState;
    }

    public void setOnSwitchAction(EventHandler<ActionEvent> action) {
        this.onSwitchAction = action;
    }
}