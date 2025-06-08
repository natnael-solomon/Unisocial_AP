package com.client.core;

import com.client.services.AuthService;
import com.client.services.NetworkService;
import com.client.services.PostService;
import com.client.services.UserService;

public class ServiceManager {

    private static ServiceManager instance;

    private final AuthService authService;
    private final PostService postService;
    private final UserService userService;
    private final NetworkService networkService;
    private final EventBus eventBus;

    private ServiceManager() {
        this.eventBus = new EventBus();

        // mock service for testing
        // this.networkService = new MockNetworkService();
        this.networkService = new NetworkService(); //real server

        this.authService = new AuthService(networkService, eventBus);
        this.postService = new PostService(networkService, eventBus);
        this.userService = new UserService(networkService, eventBus);
    }

    public static ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public PostService getPostService() {
        return postService;
    }

    public UserService getUserService() {
        return userService;
    }

    public NetworkService getNetworkService() {
        return networkService;
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}
