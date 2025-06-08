package com.client.events;

import com.client.models.User;

public abstract class AuthEvent {
    
    public static class LoginAttempt extends AuthEvent {
        private final String username;
        
        public LoginAttempt(String username) {
            this.username = username;
        }
        
        public String getUsername() { return username; }
    }
    
    public static class LoginSuccess extends AuthEvent {
        private final User user;
        
        public LoginSuccess(User user) {
            this.user = user;
        }
        
        public User getUser() { return user; }
    }
    
    public static class LoginFailure extends AuthFailure {
        public LoginFailure(String message) {
            super(message);
        }
    }
    
    public static class SignupAttempt extends AuthEvent {
        private final String username;
        
        public SignupAttempt(String username) {
            this.username = username;
        }
        
        public String getUsername() { return username; }
    }
    
    public static class SignupSuccess extends AuthEvent {
        private final User user;
        
        public SignupSuccess(User user) {
            this.user = user;
        }
        
        public User getUser() { return user; }
    }
    
    public static class SignupFailure extends AuthFailure {
        public SignupFailure(String message) {
            super(message);
        }
    }
    
    public static class Logout extends AuthEvent {
        private final User user;
        
        public Logout(User user) {
            this.user = user;
        }
        
        public User getUser() { return user; }
    }
    
    public abstract static class AuthFailure extends AuthEvent {
        private final String message;
        
        protected AuthFailure(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
    }
}