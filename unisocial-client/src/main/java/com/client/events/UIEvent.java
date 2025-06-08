package com.client.events;

public abstract class UIEvent {
    
    public static class Loading extends UIEvent {
        private final String message;
        
        public Loading(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
    }
    
    public static class Success extends UIEvent {
        private final String message;
        
        public Success(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
    }
    
    public static class Error extends UIEvent {
        private final String message;
        
        public Error(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
    }
    
    public static class NavigationRequest extends UIEvent {
        private final String destination;
        
        public NavigationRequest(String destination) {
            this.destination = destination;
        }
        
        public String getDestination() { return destination; }
    }
    
    public static class ThemeChange extends UIEvent {
        private final boolean darkMode;
        
        public ThemeChange(boolean darkMode) {
            this.darkMode = darkMode;
        }
        
        public boolean isDarkMode() { return darkMode; }
    }
}