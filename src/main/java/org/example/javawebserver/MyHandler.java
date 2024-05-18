package org.example.javawebserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyHandler implements HttpHandler {
    private final String webDir;

    public MyHandler(String webDir) {
        this.webDir = webDir;
    }
    //to check request
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();

        // Log the HTTP request activity
        Logger.logActivity(exchange);

        try {
            Path filePath = Paths.get(webDir, requestPath).normalize().toAbsolutePath();

            if (!filePath.startsWith(Paths.get(webDir).toAbsolutePath())) {
                sendErrorResponse(exchange, 403, "Forbidden");
                return;
            }

            if (Files.exists(filePath)) {
                if (Files.isDirectory(filePath)) {
                    serveDirectoryListing(exchange, filePath);
                } else {
                    serveFile(exchange, filePath);
                }
            } else {
                sendErrorResponse(exchange, 404, "Not Found");
            }
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Internal Server Error");
            e.printStackTrace();
        }
    }
    // Digunakan untuk menampilkan daftar file dalam direktori
    private void serveDirectoryListing(HttpExchange exchange, Path directoryPath) throws IOException {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><body><h1>Directory Listing:</h1><ul>");

        Files.list(directoryPath).forEach(path -> {
            String fileName = path.getFileName().toString();
            String link = fileName + (Files.isDirectory(path) ? "/" : "");
            htmlBuilder.append("<li><a href=\"")
                    .append(link)
                    .append("\">")
                    .append(fileName)
                    .append("</a></li>");
        });

        htmlBuilder.append("</ul></body></html>");
        sendResponse(exchange, 200, htmlBuilder.toString(), "text/html");
    }

    private void serveFile(HttpExchange exchange, Path filePath) throws IOException {
        byte[] fileBytes = Files.readAllBytes(filePath);
        String contentType = Files.probeContentType(filePath);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        sendResponse(exchange, 200, fileBytes, contentType);
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        sendResponse(exchange, statusCode, message, "text/plain");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        byte[] responseBytes = response.getBytes();
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }

    private void sendResponse(HttpExchange exchange, int statusCode, byte[] responseBytes, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }
}
