package com.server;

import com.server.services.AuthService;
import com.server.services.PostService;
import com.server.services.UserService;
import com.server.utils.Logger;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main server class for UniSocial application
 */
public class UnisocialServer {
    private final ServerConfig config;
    private final ExecutorService clientThreadPool;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private ServerSocket serverSocket;
    private final DatabaseManager databaseManager;
    private AuthService authService;
    private PostService postService;
    private UserService userService;

    public UnisocialServer() {
        this.config = new ServerConfig();
        this.databaseManager = new DatabaseManager(config.getDatabaseUrl());
        this.clientThreadPool = Executors.newCachedThreadPool();

        initializeServices();
    }

    public UnisocialServer(ServerConfig config) {
        this.config = config;
        this.databaseManager = new DatabaseManager(config.getDatabaseUrl());
        this.clientThreadPool = Executors.newCachedThreadPool();

        initializeServices();
    }

    private void initializeServices() {
        this.authService = new AuthService(databaseManager);
        this.postService = new PostService(databaseManager);
        this.userService = new UserService(databaseManager);
    }

    /**
     * Start the server
     */
    public void start() {
        if (running.get()) {
            Logger.warn("Server is already running");
            return;
        }

        try {
            Logger.info("=== STARTING SERVER INITIALIZATION ===");

            // Initialize database
            Logger.info("Step 1: Initializing database...");
            if (!databaseManager.initialize()) {
                Logger.error("Failed to initialize database");
                return;
            }
            Logger.info("Step 2: Database initialization complete");

            // Create server socket
            Logger.info("Step 3: Creating server socket...");
            serverSocket = new ServerSocket(config.getPort());
            Logger.info("Step 4: Server socket created");

            running.set(true);

            Logger.info("Step 5: Server ready, starting to accept connections...");
            Logger.info("UniSocial Server started on port " + config.getPort());
            Logger.info("Database: " + config.getDatabaseUrl());
            Logger.info("Max clients: " + config.getMaxClients());

            // Accept client connections
            acceptClients();

        } catch (IOException e) {
            Logger.error("Failed to start server: " + e.getMessage());
            stop();
        }
    }

    /**
     * Accept incoming client connections
     */
    private void acceptClients() {
        while (running.get() && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();

                // Check if we've reached max clients
                if (getActiveClientCount() >= config.getMaxClients()) {
                    Logger.warn("Max clients reached, rejecting connection from " +
                            clientSocket.getRemoteSocketAddress());
                    clientSocket.close();
                    continue;
                }

                // Create and start client handler
                ClientHandler clientHandler = new ClientHandler(
                        clientSocket,
                        authService,
                        postService,
                        userService
                );

                clientThreadPool.submit(clientHandler);

                Logger.info("New client connected: " + clientSocket.getRemoteSocketAddress());

            } catch (IOException e) {
                if (running.get()) {
                    Logger.error("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Stop the server
     */
    public void stop() {
        if (!running.get()) {
            return;
        }

        Logger.info("Stopping UniSocial Server...");
        running.set(false);

        try {
            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            // Shutdown thread pool
            clientThreadPool.shutdown();

            // Close database connections
            databaseManager.close();

            Logger.info("Server stopped successfully");

        } catch (IOException e) {
            Logger.error("Error stopping server: " + e.getMessage());
        }
    }

    /**
     * Get the number of active client connections
     */
    public int getActiveClientCount() {
        return ((java.util.concurrent.ThreadPoolExecutor) clientThreadPool).getActiveCount();
    }

    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Get server configuration
     */
    public ServerConfig getConfig() {
        return config;
    }

    /**
     * Main method to start the server
     */
    public static void main(String[] args) {
        // Handle command line arguments
        ServerConfig config = parseArguments(args);

        // Create and start server
        UnisocialServer server = new UnisocialServer(config);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.info("Shutdown signal received");
            server.stop();
        }));

        // Start server
        server.start();
    }

    /**
     * Parse command line arguments
     */
    private static ServerConfig parseArguments(String[] args) {
        ServerConfig config = new ServerConfig();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--port":
                case "-p":
                    if (i + 1 < args.length) {
                        config.setPort(Integer.parseInt(args[++i]));
                    }
                    break;
                case "--database":
                case "-d":
                    if (i + 1 < args.length) {
                        config.setDatabaseUrl(args[++i]);
                    }
                    break;
                case "--max-clients":
                case "-m":
                    if (i + 1 < args.length) {
                        config.setMaxClients(Integer.parseInt(args[++i]));
                    }
                    break;
                case "--help":
                case "-h":
                    printUsage();
                    System.exit(0);
                    break;
            }
        }

        return config;
    }

    /**
     * Print usage information
     */
    private static void printUsage() {
        System.out.println("UniSocial Server");
        System.out.println("Usage: java -jar unisocial-server.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -p, --port <port>        Server port (default: 8080)");
        System.out.println("  -d, --database <url>     Database URL (default: jdbc:sqlite:unisocial.db)");
        System.out.println("  -m, --max-clients <num>  Maximum concurrent clients (default: 100)");
        System.out.println("  -h, --help               Show this help message");
    }
}
