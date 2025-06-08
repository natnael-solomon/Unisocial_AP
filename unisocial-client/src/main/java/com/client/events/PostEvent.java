package com.client.events;

import java.util.List;

import com.client.models.Post;

/**
 * Post related events
 */
public class PostEvent {

    /**
     * Base class for all post events
     */
    public static abstract class PostEventBase {

        private final long timestamp;

        public PostEventBase() {
            this.timestamp = System.currentTimeMillis();
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Event fired when a post creation is attempted
     */
    public static class CreateAttempt extends PostEventBase {

        private final String content;

        public CreateAttempt(String content) {
            super();
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        @Override
        public String toString() {
            return "CreateAttempt{content='" + content + "'}";
        }
    }

    /**
     * Event fired when post creation is successful
     */
    public static class CreateSuccess extends PostEventBase {

        private final String content;
        private final Post post; // Optional: if server returns the created post

        public CreateSuccess(String content) {
            super();
            this.content = content;
            this.post = null;
        }

        public CreateSuccess(String content, Post post) {
            super();
            this.content = content;
            this.post = post;
        }

        public String getContent() {
            return content;
        }

        public Post getPost() {
            return post;
        }

        @Override
        public String toString() {
            return "CreateSuccess{content='" + content + "', post=" + post + "}";
        }
    }

    /**
     * Event fired when post creation fails
     */
    public static class CreateFailure extends PostEventBase {

        private final String message;
        private final Throwable cause;

        public CreateFailure(String message) {
            super();
            this.message = message;
            this.cause = null;
        }

        public CreateFailure(String message, Throwable cause) {
            super();
            this.message = message;
            this.cause = cause;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getCause() {
            return cause;
        }

        @Override
        public String toString() {
            return "CreateFailure{message='" + message + "'}";
        }
    }

    /**
     * Event fired when feed is successfully loaded
     */
    public static class FeedLoaded extends PostEventBase {

        private final List<Post> posts;

        public FeedLoaded(List<Post> posts) {
            super();
            this.posts = posts;
        }

        public List<Post> getPosts() {
            return posts;
        }

        public int getPostCount() {
            return posts != null ? posts.size() : 0;
        }

        @Override
        public String toString() {
            return "FeedLoaded{postCount=" + getPostCount() + "}";
        }
    }

    /**
     * Event fired when feed loading fails
     */
    public static class FeedLoadFailure extends PostEventBase {

        private final String message;
        private final Throwable cause;

        public FeedLoadFailure(String message) {
            super();
            this.message = message;
            this.cause = null;
        }

        public FeedLoadFailure(String message, Throwable cause) {
            super();
            this.message = message;
            this.cause = cause;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getCause() {
            return cause;
        }

        @Override
        public String toString() {
            return "FeedLoadFailure{message='" + message + "'}";
        }
    }

    /**
     * Event fired when a post is liked/unliked
     */
    public static class LikeToggled extends PostEventBase {

        private final int postId;
        private final boolean isLiked;
        private final int likeCount;

        public LikeToggled(int postId) {
            super();
            this.postId = postId;
            this.isLiked = true; // Default assumption
            this.likeCount = 0; // Will be updated by server response
        }

        public LikeToggled(int postId, boolean isLiked, int likeCount) {
            super();
            this.postId = postId;
            this.isLiked = isLiked;
            this.likeCount = likeCount;
        }

        public int getPostId() {
            return postId;
        }

        public boolean isLiked() {
            return isLiked;
        }

        public int getLikeCount() {
            return likeCount;
        }

        @Override
        public String toString() {
            return "LikeToggled{postId=" + postId + ", isLiked=" + isLiked + ", likeCount=" + likeCount + "}";
        }
    }

    /**
     * Event fired when liking a post fails
     */
    public static class LikeFailure extends PostEventBase {

        private final int postId;
        private final String message;
        private final Throwable cause;

        public LikeFailure(int postId, String message) {
            super();
            this.postId = postId;
            this.message = message;
            this.cause = null;
        }

        public LikeFailure(int postId, String message, Throwable cause) {
            super();
            this.postId = postId;
            this.message = message;
            this.cause = cause;
        }

        public int getPostId() {
            return postId;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getCause() {
            return cause;
        }

        @Override
        public String toString() {
            return "LikeFailure{postId=" + postId + ", message='" + message + "'}";
        }
    }

    /**
     * Event fired when a post is bookmarked/unbookmarked
     */
    public static class BookmarkToggled extends PostEventBase {

        private final int postId;
        private final boolean isBookmarked;

        public BookmarkToggled(int postId, boolean isBookmarked) {
            super();
            this.postId = postId;
            this.isBookmarked = isBookmarked;
        }

        public int getPostId() {
            return postId;
        }

        public boolean isBookmarked() {
            return isBookmarked;
        }

        @Override
        public String toString() {
            return "BookmarkToggled{postId=" + postId + ", isBookmarked=" + isBookmarked + "}";
        }
    }

    /**
     * Event fired when bookmarking a post fails
     */
    public static class BookmarkFailure extends PostEventBase {

        private final int postId;
        private final String message;
        private final Throwable cause;

        public BookmarkFailure(int postId, String message) {
            super();
            this.postId = postId;
            this.message = message;
            this.cause = null;
        }

        public BookmarkFailure(int postId, String message, Throwable cause) {
            super();
            this.postId = postId;
            this.message = message;
            this.cause = cause;
        }

        public int getPostId() {
            return postId;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getCause() {
            return cause;
        }

        @Override
        public String toString() {
            return "BookmarkFailure{postId=" + postId + ", message='" + message + "'}";
        }
    }

    /**
     * Event fired when a post is deleted
     */
    public static class PostDeleted extends PostEventBase {

        private final int postId;
        private final String deletedBy; // Username who deleted the post

        public PostDeleted(int postId) {
            super();
            this.postId = postId;
            this.deletedBy = null;
        }

        public PostDeleted(int postId, String deletedBy) {
            super();
            this.postId = postId;
            this.deletedBy = deletedBy;
        }

        public int getPostId() {
            return postId;
        }

        public String getDeletedBy() {
            return deletedBy;
        }

        @Override
        public String toString() {
            return "PostDeleted{postId=" + postId + ", deletedBy='" + deletedBy + "'}";
        }
    }

    /**
     * Event fired when post deletion fails
     */
    public static class DeleteFailure extends PostEventBase {

        private final int postId;
        private final String message;
        private final Throwable cause;

        public DeleteFailure(int postId, String message) {
            super();
            this.postId = postId;
            this.message = message;
            this.cause = null;
        }

        public DeleteFailure(int postId, String message, Throwable cause) {
            super();
            this.postId = postId;
            this.message = message;
            this.cause = cause;
        }

        public int getPostId() {
            return postId;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getCause() {
            return cause;
        }

        @Override
        public String toString() {
            return "DeleteFailure{postId=" + postId + ", message='" + message + "'}";
        }
    }

    /**
     * Event fired when a post is shared
     */
    public static class PostShared extends PostEventBase {

        private final int postId;
        private final String sharedBy;
        private final String platform; // e.g., "twitter", "facebook", "copy_link"

        public PostShared(int postId, String sharedBy, String platform) {
            super();
            this.postId = postId;
            this.sharedBy = sharedBy;
            this.platform = platform;
        }

        public int getPostId() {
            return postId;
        }

        public String getSharedBy() {
            return sharedBy;
        }

        public String getPlatform() {
            return platform;
        }

        @Override
        public String toString() {
            return "PostShared{postId=" + postId + ", sharedBy='" + sharedBy + "', platform='" + platform + "'}";
        }
    }

    /**
     * Event fired when sharing a post fails
     */
    public static class ShareFailure extends PostEventBase {

        private final int postId;
        private final String message;
        private final String platform;

        public ShareFailure(int postId, String message, String platform) {
            super();
            this.postId = postId;
            this.message = message;
            this.platform = platform;
        }

        public int getPostId() {
            return postId;
        }

        public String getMessage() {
            return message;
        }

        public String getPlatform() {
            return platform;
        }

        @Override
        public String toString() {
            return "ShareFailure{postId=" + postId + ", message='" + message + "', platform='" + platform + "'}";
        }
    }

    /**
     * Event fired when a post is reported
     */
    public static class PostReported extends PostEventBase {

        private final int postId;
        private final String reportedBy;
        private final String reason;

        public PostReported(int postId, String reportedBy, String reason) {
            super();
            this.postId = postId;
            this.reportedBy = reportedBy;
            this.reason = reason;
        }

        public int getPostId() {
            return postId;
        }

        public String getReportedBy() {
            return reportedBy;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public String toString() {
            return "PostReported{postId=" + postId + ", reportedBy='" + reportedBy + "', reason='" + reason + "'}";
        }
    }
}
