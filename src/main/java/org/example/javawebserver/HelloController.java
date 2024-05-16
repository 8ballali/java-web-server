package org.example.javawebserver;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class HelloController {

    protected static final String LOG_PATH = "D:/coba_log";
    protected static final String WEB_DIR = "D:/web_server";
    protected static final int PORT = 8009;

    private ServerManager server;

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    @FXML
    private Label serviceLabel;

    @FXML
    private Label portLabel;

    @FXML
    private TextArea logTextArea;

    @FXML
    private TextField logTextField;

    @FXML
    private Button logButton;

    @FXML
    public void logButtonOnAction(ActionEvent event) {
        chooseDirectory("Choose Log Directory", logTextField);
    }

    @FXML
    public void initialize() {
        server = new ServerManager(PORT, WEB_DIR, LOG_PATH);
        portLabel.setText(String.valueOf(PORT));
    }

    @FXML
    public void startButtonOnAction(ActionEvent event) {
        try {

            server.startServer();
            serviceLabel.setText("Running");
            serviceLabel.setStyle("-fx-text-fill: green");
            logTextArea.setEditable(false);
            logTextArea.setStyle("-fx-text-fill: black");
            readLogFile(); // Read log file
            watchLogFile(); // Start monitoring log file for changes
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void stopButtonOnAction(ActionEvent event) {
        server.stopServer();
        serviceLabel.setText("Stopped");
        serviceLabel.setStyle("-fx-text-fill: red");
        logTextArea.setStyle("-fx-text-fill: white");
    }

    private void watchLogFile() {
        try {
            // Obtain the directory of the log file
            Path logDir = Paths.get(LOG_PATH);

            // Create a WatchService to monitor file system events
            WatchService watchService = FileSystems.getDefault().newWatchService();

            // Register the directory for ENTRY_MODIFY events
            logDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            // Monitor file system events indefinitely in a background thread
            new Thread(() -> {
                while (true) {
                    WatchKey key;
                    try {
                        key = watchService.take(); // Wait for key to be signaled
                    } catch (InterruptedException e) {
                        return; // Thread interrupted, terminate
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Path modifiedFilePath = (Path) event.context();
                            if (modifiedFilePath.endsWith("access.log")) {
                                // Log file has been modified, update logTextArea
                                updateLogTextArea();
                            }
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break; // Key is no longer valid, exit loop
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLogTextArea() {
        Path logFilePath = Paths.get(LOG_PATH, "access.log");

        try {
            String logContent = Files.readString(logFilePath);
            Platform.runLater(() -> logTextArea.setText(logContent));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Belom tau ini bener apa engga
    private void readLogFile() {
        Path logFilePath = Paths.get(LOG_PATH, "access.log");

        try {
            // Check if the file exists
            if (Files.exists(logFilePath)) {
                // Read the file content
                String logContent = Files.readString(logFilePath);
                logTextArea.setText(logContent);
            } else {
                // Create the file if it doesn't exist
                Files.createFile(logFilePath);
                logTextArea.setText(""); // Initialize with empty content
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chooseDirectory(String title, TextField textField) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            textField.setText(selectedDirectory.getAbsolutePath());
        }
    }

}
