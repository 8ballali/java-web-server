package org.example.javawebserver;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ServerManager {

    private HttpServer server;
    private int port;
    private String webDir;
    private String log_path;

    public ServerManager(int port, String webDir, String log_path) {
        this.port = port;
        this.webDir = webDir;
        this.log_path = log_path;
    }
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new MyHandler(webDir));
        server.setExecutor(null);
        server.start();
        logServerStarted();
        System.out.println("Server started on port " + port);
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            logServerStopped();
            System.out.println("Server stopped");
        }
    }

    private void logServerStarted(){
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            String formattedDate = dateFormat.format(new Date());
            String logEntry = formattedDate + " | Server started on port " + port + "\n";

            Path logFilePath = Paths.get(log_path);
            Files.createDirectories(logFilePath.getParent());
            Files.write(logFilePath, logEntry.getBytes(), java.nio.file.StandardOpenOption.APPEND);
        }catch (Exception e){
            System.out.println("Error logging server start: " + e.getMessage());
        }
    }
    private void logServerStopped(){
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            String formattedDate = dateFormat.format(new Date());
            String logEntry = formattedDate + " | Server stopped on port " + port + "\n";

            Path logFilePath = Paths.get(log_path);
            Files.createDirectories(logFilePath.getParent());
            Files.write(logFilePath, logEntry.getBytes(), java.nio.file.StandardOpenOption.APPEND);
        }catch (Exception e){
            System.out.println("Error logging server stop: " + e.getMessage());
        }
    }
}
