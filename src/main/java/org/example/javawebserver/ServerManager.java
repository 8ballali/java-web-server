package org.example.javawebserver;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.file.*;

import static org.example.javawebserver.Logger.logFileDateFormat;
import static org.example.javawebserver.Logger.logFileName;

public class ServerManager {

    private HttpServer server;
    private int port;
    private String webDir;
    private String logPath;

    public ServerManager(int port, String webDir, String logPath) {
        this.port = port;
        this.webDir = webDir;
        this.logPath = logPath;
    }

    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new MyHandler(webDir));
        server.setExecutor(null);
        server.start();
        Logger.logServerEvent("Server started on port " + port);
        System.out.println("Server started on port " + port);
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            Logger.logServerEvent("Server stopped on port " + port);
            System.out.println("Server stopped");
        }
    }
    // Digunakan ketika server dinyalakan atau dihentikan
    private void logServerEvent(String message) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE-MMM-dd HH:mm:ss yyyy");
            String formattedDate = dateFormat.format(new Date());
            String logEntry = formattedDate + " | " + message + "\n";

            Path logFilePath = Paths.get(logPath, logFileName);
            Files.createDirectories(logFilePath.getParent());
            // Append the log entry to the file, creating it if it doesn't exist
            Files.write(logFilePath, logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            System.out.println("Error logging server event: " + e.getMessage());
        }
    }
    //get Log name
    private String getLogFileName() throws IOException {
        Path logDir = Paths.get(logPath);
        Files.createDirectories(logDir);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logDir, "*.log")) {
            for (Path entry : stream) {
                return entry.getFileName().toString();
            }
        }
        String timestamp = logFileDateFormat.format(new Date());
        String newLogFileName = "log_" + timestamp + ".log";
        Path newLogFilePath = logDir.resolve(newLogFileName);
        Files.createFile(newLogFilePath);
        return newLogFileName;
    }
}
