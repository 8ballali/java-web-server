package org.example.javawebserver;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    public static String log_path = HelloController.LOG_PATH;
    public static int port = HelloController.PORT;
    public static String webDir = HelloController.WEB_DIR;
    public static void logActivity(HttpExchange exchange) throws IOException {
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
        String formattedDate = dateFormat.format(new Date());


        if (!Files.exists(Paths.get(webDir + requestPath))) {
            String logEntry = formattedDate + " | " + requestMethod + " | localhost:" + port + requestPath + " | " + clientIP + " | 404 Not Found\n";
            Path logFilePath = Paths.get(log_path);
            Files.createDirectories(logFilePath.getParent());
            Files.write(logFilePath, logEntry.getBytes(), java.nio.file.StandardOpenOption.APPEND);
            return;
        }

        String logEntry = formattedDate + " | " + requestMethod + " | localhost:"+ port + requestPath + " | " + clientIP + "\n";

        Path logFilePath = Paths.get(log_path);
        Files.createDirectories(logFilePath.getParent());
        Files.write(logFilePath, logEntry.getBytes(), java.nio.file.StandardOpenOption.APPEND);
    }

    //make a function to read log file

}
