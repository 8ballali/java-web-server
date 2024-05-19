package org.example.javawebserver;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.net.InetAddress;

public class Logger {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
    public static final SimpleDateFormat logFileDateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
    public static String logFileName;

    static {
        try {
            logFileName = getLogFileName();
        } catch (IOException e) {
            e.printStackTrace();
            logFileName = "log_error.log"; // Fallback log file name
        }
    }
    //get log name
    private static String getLogFileName() throws IOException {
        Path logDir = Paths.get(HelloController.LOG_PATH);
        Files.createDirectories(logDir);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logDir, "*.log")) {
            for (Path entry : stream) {
                return entry.getFileName().toString();
            }
        }

        String timestamp = logFileDateFormat.format(new Date());

        //Format penamaan file baru
        String newLogFileName =timestamp + ".log";
        Path newLogFilePath = logDir.resolve(newLogFileName);
        Files.createFile(newLogFilePath);
        return newLogFileName;
    }
    //catat log
    public static void logActivity(HttpExchange exchange) throws IOException {
        InetAddress IP = InetAddress.getLocalHost();

        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();

        String formattedDate = dateFormat.format(new Date());

        String logEntry;
        Path logFilePath = Paths.get(HelloController.LOG_PATH, logFileName);
        Files.createDirectories(logFilePath.getParent());

        if (!Files.exists(Paths.get(HelloController.WEB_DIR, requestPath))) {
            logEntry = formattedDate + " | " + requestMethod + " | localhost:" + HelloController.PORT + requestPath + " | " + IP.getHostAddress() + " | 404 Not Found\n";
        } else {
            logEntry = formattedDate + " | " + requestMethod + " | localhost:" + HelloController.PORT + requestPath + " | " + IP.getHostAddress() + "\n";
        }

        Files.write(logFilePath, logEntry.getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
    }
    // to specify message when stop and start
    public static void logServerEvent(String message) {
        try {
            String formattedDate = dateFormat.format(new Date());
            String logEntry = String.format("%s | %s\n", formattedDate, message);

            Path logFilePath = Paths.get(HelloController.LOG_PATH, logFileName);
            Files.createDirectories(logFilePath.getParent());
            Files.write(logFilePath, logEntry.getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Error logging server event: " + e.getMessage());
        }
    }
}
