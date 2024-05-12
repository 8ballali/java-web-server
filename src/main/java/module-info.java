module org.example.javawebserver {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.httpserver;


    opens org.example.javawebserver to javafx.fxml;
    exports org.example.javawebserver;
}