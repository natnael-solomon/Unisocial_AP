package com.server;

/**
 * Server configuration class
 */
public class ServerConfig {
    private int port = 8080;
    private String databaseUrl = "jdbc:sqlite:unisocial.db" ;
    private int maxClients = 100;
    private int connectionTimeout = 30000; // 30 seconds
    private int readTimeout = 60000; // 60 seconds
    private boolean enableLogging = true;
    private String logLevel = "INFO";
    private String uploadDirectory = "uploads/";
    private long maxFileSize = 10 * 1024 * 1024; // 10MB

    // Getters and setters
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getDatabaseUrl() { return databaseUrl; }
    public void setDatabaseUrl(String databaseUrl) { this.databaseUrl = databaseUrl; }

    public int getMaxClients() { return maxClients; }
    public void setMaxClients(int maxClients) { this.maxClients = maxClients; }

    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }

    public int getReadTimeout() { return readTimeout; }
    public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }

    public boolean isEnableLogging() { return enableLogging; }
    public void setEnableLogging(boolean enableLogging) { this.enableLogging = enableLogging; }

    public String getLogLevel() { return logLevel; }
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }

    public String getUploadDirectory() { return uploadDirectory; }
    public void setUploadDirectory(String uploadDirectory) { this.uploadDirectory = uploadDirectory; }

    public long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }
}
