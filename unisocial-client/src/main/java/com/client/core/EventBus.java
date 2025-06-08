package com.client.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javafx.application.Platform;

public class EventBus {
    private final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                  .add((Consumer<Object>) handler);
    }
    
    public <T> void publish(T event) {
        Class<?> eventType = event.getClass();
        CopyOnWriteArrayList<Consumer<Object>> handlers = subscribers.get(eventType);
        
        if (handlers != null) {
            for (Consumer<Object> handler : handlers) {
                Platform.runLater(() -> handler.accept(event));
            }
        }
    }
    
    public <T> void unsubscribe(Class<T> eventType, Consumer<T> handler) {
        CopyOnWriteArrayList<Consumer<Object>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }
}