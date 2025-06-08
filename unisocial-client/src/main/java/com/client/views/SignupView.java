package com.client.views;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class SignupView extends VBox {

    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Label errorLabel;
    private Button signupButton;

    public SignupView() {
        // Set up the VBox
        setPrefWidth(242.0);
        setSpacing(7.8);
        setAlignment(Pos.CENTER);

        // Add Form Fields
        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("auth-input");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("auth-input");

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.getStyleClass().add("auth-input");

        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false); // Initially hidden

        // Add Primary Action Button
        signupButton = new Button("Sign Up");
        signupButton.getStyleClass().add("primary-button");

        // Add children to the VBox
        getChildren().addAll(usernameField, passwordField, confirmPasswordField, errorLabel, signupButton);
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public PasswordField getConfirmPasswordField() {
        return confirmPasswordField;
    }

    public Label getErrorLabel() {
        return errorLabel;
    }

    public Button getSignupButton() {
        return signupButton;
    }
}