package com.client.views;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginView extends VBox {

    private TextField usernameField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Button loginButton;

    public LoginView() {
        // Set up the VBox
        setPrefWidth(242.0);
        setSpacing(20.0);
        setAlignment(Pos.CENTER);

        // Add Form Fields
        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("auth-input");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("auth-input");

        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");

        // Add Primary Action Button
        loginButton = new Button("Log In");
        loginButton.getStyleClass().add("primary-button");

        // Add children to the VBox
        getChildren().addAll(usernameField, passwordField, errorLabel, loginButton);
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public Label getErrorLabel() {
        return errorLabel;
    }

    public Button getLoginButton() {
        return loginButton;
    }
}