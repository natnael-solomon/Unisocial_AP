module com.client {
    requires javafx.fxml;
    requires com.google.gson;
    requires org.kordamp.ikonli.javafx;
    requires javafx.controls;
    requires org.kordamp.ikonli.fontawesome6;

    opens com.client to javafx.fxml;
    opens com.client.controllers to javafx.fxml;
    opens com.client.models to com.google.gson;
    
    exports com.client;
    exports com.client.controllers;
    exports com.client.models;
    exports com.client.services;
    exports com.client.views;
    exports com.client.core;
    exports com.client.events;
    exports com.client.exceptions;
    exports com.client.utils;
}