package com.client.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

public class ImageUtils {

    private static final Map<String, Image> imageCache = new HashMap<>();
    private static final Image DEFAULT_AVATAR = loadImage("/images/default-avatar.png");
    private static final Map<Integer, Boolean> failedAvatarLoads = new HashMap<>();

    public static void loadUserAvatar(int userId, ImageView imageView) {
        if (imageView == null) {
            return;
        }

        // If we've already failed to load this user's avatar, use default immediately
        if (failedAvatarLoads.containsKey(userId)) {
            imageView.setImage(DEFAULT_AVATAR);
            applyCircularClip(imageView);
            return;
        }

        String avatarPath = "/images/avatars/user_" + userId + ".png";
        Image avatar = loadImage(avatarPath);

        if (avatar == null) {
            // Cache the failure to prevent repeated attempts
            failedAvatarLoads.put(userId, true);
            avatar = DEFAULT_AVATAR;
        }

        imageView.setImage(avatar);
        applyCircularClip(imageView);
    }

    private static void applyCircularClip(ImageView imageView) {
        if (imageView.getClip() == null) {
            double radius = Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2;
            Circle clip = new Circle(radius, radius, radius);
            imageView.setClip(clip);
        }
    }

    public static void loadPostImage(String imageUrl, ImageView imageView) {
        if (imageView == null) {
            return;
        }

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

        if (image != null) {
            imageView.setImage(image);
        }
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
            // Only log the first failure for each path
            if (!imageCache.containsKey(path)) {
                Logger.debug("Failed to load image: " + path);
            }
            return null;
        }
    }

    public static void clearCache() {
        imageCache.clear();
        failedAvatarLoads.clear();
    }
}
