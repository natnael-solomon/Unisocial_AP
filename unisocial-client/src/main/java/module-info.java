module com.unisocialclient {
    requires javafx.fxml;
    requires com.google.gson;
    requires org.kordamp.ikonli.javafx;
    requires javafx.controls;
    requires org.kordamp.ikonli.fontawesome6;


    opens com.client to javafx.fxml;
    exports com.client;
}