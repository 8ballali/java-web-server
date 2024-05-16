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
        String formattedDate = dateFormat.format(new Date());

        String logEntry;
        Path filePath = Paths.get(HelloController.WEB_DIR, requestPath).normalize().toAbsolutePath();
        if (!filePath.startsWith(Paths.get(HelloController.WEB_DIR).toAbsolutePath()) || !Files.exists(filePath)) {
            logEntry = String.format("%s | %s | localhost:%d%s | %s | 404 Not Found\n",
                    formattedDate, requestMethod, HelloController.PORT, requestPath, clientIP);
        } else {
            logEntry = String.format("%s | %s | localhost:%d%s | %s\n",
                    formattedDate, requestMethod, HelloController.PORT, requestPath, clientIP);
        }

        Path logFilePath = Paths.get(HelloController.LOG_PATH, LOG_FILE_NAME);
        Files.createDirectories(logFilePath.getParent());
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
