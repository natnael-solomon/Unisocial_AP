module com.server {
    requires com.google.gson;
    requires java.sql;
    requires jbcrypt;
    requires org.xerial.sqlitejdbc;

    // Allows Gson to use reflection on the models package
    opens com.server.models to com.google.gson;
}