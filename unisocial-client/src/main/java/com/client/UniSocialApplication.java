package com.client;

import com.client.controllers.AuthController;
import com.client.controllers.FeedController;
import com.client.controllers.LoginController;
import com.client.core.ServiceManager;
import com.client.views.AuthView;
import com.client.views.FeedView;
import com.client.views.LoginView;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * JavaFX UniSocial Application
 */
public class UniSocialApplication extends Application {

    private static Scene scene;
    private static Stage primaryStage;
    
    // Keep references to controllers for proper cleanup
    private static AuthController currentAuthController;
    private static FeedController currentFeedController;
    private static LoginController currentLoginController;

    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;
            primaryStage.setTitle("UniSocial");
            primaryStage.initStyle(StageStyle.TRANSPARENT);

            // Initialize the service manager and core services
            ServiceManager.getInstance();

            // Initialize with AuthScreen
            showAuthScreen();

            // Handle application close
            primaryStage.setOnCloseRequest(e -> {
                cleanup();
            });

            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void setRoot(Parent newRootNode) {
        if (scene != null) {
            scene.setRoot(newRootNode);
        }
    }

    public static void showLoginScreen() {
        try {
            // Clean up previous controllers
            cleanupCurrentControllers();
            
            LoginView loginView = new LoginView();
            currentLoginController = new LoginController(loginView);
            setRoot(loginView);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAuthScreen() {
        try {
            // Clean up previous controllers
            cleanupCurrentControllers();
            
            AuthView authView = new AuthView();
            currentAuthController = new AuthController(authView);
            
            if (scene == null) {
                scene = new Scene(authView);
                scene.setFill(Color.TRANSPARENT);
                primaryStage.setScene(scene);
            } else {
                setRoot(authView);
            }
            
            // Set window size for auth screen
            primaryStage.setWidth(712);
            primaryStage.setHeight(500);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showFeedScreen() {
        try {
            // Clean up previous controllers
            cleanupCurrentControllers();
            
            FeedView feedView = new FeedView();
            currentFeedController = new FeedController(feedView);
            setRoot(feedView);

            // Set window size for feed screen
            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);
            primaryStage.setResizable(true);
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Clean up current controllers to prevent memory leaks
     */
    private static void cleanupCurrentControllers() {
        if (currentAuthController != null) {
            currentAuthController.cleanup();
            currentAuthController = null;
        }
        if (currentFeedController != null) {
            currentFeedController.cleanup();
            currentFeedController = null;
        }
        if (currentLoginController != null) {
            currentLoginController.cleanup();
            currentLoginController = null;
        }
    }
    
    /**
     * Application cleanup on exit
     */
    private void cleanup() {
        cleanupCurrentControllers();
        
        // Shutdown services
        try {
            ServiceManager serviceManager = ServiceManager.getInstance();
            if (serviceManager.getNetworkService() != null) {
                serviceManager.getNetworkService().disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get the primary stage reference
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Get the current scene reference
     */
    public static Scene getCurrentScene() {
        return scene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}