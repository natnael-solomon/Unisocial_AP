package com.client.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ImageUtils {
    private static final Map<String, Image> imageCache = new HashMap<>();
    private static final Image DEFAULT_AVATAR = loadImage("/images/default-avatar.png");
    private static final Image DEFAULT_POST_IMAGE = loadImage("/images/default-post-welcome_image.png");
    
    public static void loadUserAvatar(int userId, ImageView imageView) {
        if (imageView == null) return;
        
        String avatarPath = "/images/avatars/user_" + userId + ".png";
        Image avatar = loadImage(avatarPath);
        
        if (avatar == null) {
            avatar = DEFAULT_AVATAR;
        }
        
        imageView.setImage(avatar);
        
        // Apply circular clip if not already applied
        if (imageView.getClip() == null) {
            double radius = Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2;
            Circle clip = new Circle(radius, radius, radius);
            imageView.setClip(clip);
        }
    }
    
    public static void loadPostImage(String imageUrl, ImageView imageView) {
        if (imageView == null) return;
        
        Image image = null;
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("http")) {
                // Load from URL
                image = loadImage(imageUrl, true);
            } else {
                // Load from resources
                image = loadImage(imageUrl);
            }
        }
        
        if (image == null) {
            image = DEFAULT_POST_IMAGE;
        }
        
        imageView.setImage(image);
    }
    
    public static Image loadImage(String path) {
        return loadImage(path, false);
    }
    
    public static Image loadImage(String path, boolean isUrl) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        // Check cache first
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }
        
        try {
            Image image;
            if (isUrl) {
                image = new Image(path, true);
            } else {
                image = new Image(Objects.requireNonNull(ImageUtils.class.getResourceAsStream(path)));
            }
            
            // Cache the image
            imageCache.put(path, image);
            return image;
        } catch (Exception e) {
            System.err.println("Failed to load image: " + path + " - " + e.getMessage());
            return null;
        }
    }
    
    public static void clearCache() {
        imageCache.clear();
    }
}