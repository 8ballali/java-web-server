package org.example.javawebserver;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.*;

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
        logEvent("Server started on port " + port);
        System.out.println("Server started on port " + port);
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            logEvent("Server stopped on port " + port);
            System.out.println("Server stopped");
        }
    }

    private void logEvent(String message) {
        Logger.logServerEvent(message);
    }
}
