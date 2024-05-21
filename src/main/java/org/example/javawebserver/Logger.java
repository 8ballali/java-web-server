package org.example.javawebserver;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
    public static final SimpleDateFormat LOG_FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
    public static String logFileName;

    static {
        try {
            logFileName = initializeLogFileName();
        } catch (IOException e) {
            e.printStackTrace();
            logFileName = "log_error.log"; // Fallback log file name
        }
    }
    //Menentukan nama file lognya
    private static String initializeLogFileName() throws IOException {
        Path logDir = Paths.get(HelloController.LOG_PATH);
        Files.createDirectories(logDir);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logDir, "*.log")) {
            for (Path entry : stream) {
                return entry.getFileName().toString();
            }
        }

        String timestamp = LOG_FILE_DATE_FORMAT.format(new Date());
        String newLogFileName = timestamp + ".log";
        Files.createFile(logDir.resolve(newLogFileName));
        return newLogFileName;
    }
    //nambahin lognya
    public static void logActivity(HttpExchange exchange) throws IOException {
        InetAddress clientIP = InetAddress.getLocalHost();
        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();
        String formattedDate = DATE_FORMAT.format(new Date());

        String logEntry = formatLogEntry(formattedDate, requestMethod, requestPath, clientIP, !Files.exists(Paths.get(HelloController.WEB_DIR, requestPath)));
        appendLog(logEntry);
    }
    //kalau error
    public static void logServerEvent(String message) {
        String formattedDate = DATE_FORMAT.format(new Date());
        String logEntry = String.format("%s | %s\n", formattedDate, message);
        try {
            appendLog(logEntry);
        } catch (IOException e) {
            System.err.println("Error logging server event: " + e.getMessage());
        }
    }
    //formatting
    private static String formatLogEntry(String date, String method, String path, InetAddress ip, boolean notFound) {
        if (notFound) {
            return String.format("%s | %s | localhost:%d%s | %s | 404 Not Found\n", date, method, HelloController.PORT, path, ip.getHostAddress());
        } else {
            return String.format("%s | %s | localhost:%d%s | %s\n", date, method, HelloController.PORT, path, ip.getHostAddress());
        }
    }

    private static void appendLog(String logEntry) throws IOException {
        Path logFilePath = Paths.get(HelloController.LOG_PATH, logFileName);
        Files.createDirectories(logFilePath.getParent());
        Files.write(logFilePath, logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
