package org.example.javawebserver;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final String LOG_FILE_NAME = "access.log";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

    public static void logActivity(HttpExchange exchange) throws IOException {
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
        String formattedDate = dateFormat.format(new Date());

        String logEntry;
        Path logFilePath = Paths.get(HelloController.LOG_PATH, "access.log");
        Files.createDirectories(logFilePath.getParent());

        if (!Files.exists(Paths.get(HelloController.WEB_DIR, requestPath))) {
            logEntry = formattedDate + " | " + requestMethod + " | localhost:" + HelloController.PORT + requestPath + " | " + clientIP + " | 404 Not Found\n";
        } else {
            logEntry = formattedDate + " | " + requestMethod + " | localhost:" + HelloController.PORT + requestPath + " | " + clientIP + "\n";
        }

        Files.write(logFilePath, logEntry.getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
    }

    public static void logServerEvent(String message) {
        try {
            String formattedDate = dateFormat.format(new Date());
            String logEntry = String.format("%s | %s\n", formattedDate, message);

            Path logFilePath = Paths.get(HelloController.LOG_PATH, LOG_FILE_NAME);
            Files.createDirectories(logFilePath.getParent());
            Files.write(logFilePath, logEntry.getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Error logging server event: " + e.getMessage());
        }
    }
}
