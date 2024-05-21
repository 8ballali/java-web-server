package org.example.javawebserver;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class HelloController {

    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_WEB_DIR = "D:/web_server";
    private static final String DEFAULT_LOG_PATH = "D:/log_server";
    private static final int DEFAULT_PORT = 8009;

    protected static String LOG_PATH;
    protected static String WEB_DIR;
    protected static int PORT;
    private static String logFileName;

    private ServerManager server;

    @FXML
    private Button startButton, stopButton, logButton, dirButton;
    @FXML
    private Label serviceLabel, portLabel;
    @FXML
    private TextArea logTextArea;
    @FXML
    private TextField logTextField, dirTextField, portTextField;

    @FXML
    public void initialize() {
        loadConfig();
        logFileName = Logger.logFileName;
        server = new ServerManager(PORT, WEB_DIR, LOG_PATH);
        updateUIWithConfig();
    }

    @FXML
    public void dirButtonOnAction(ActionEvent event) {
        chooseFileOrDirectory("Choose Directory or Root HTML File", dirTextField);
    }

    @FXML
    public void logButtonOnAction(ActionEvent event) {
        chooseDirectory("Choose Log Directory", logTextField);
    }

    @FXML
    public void startButtonOnAction(ActionEvent event) {
        try {
            restartServer();
            updateUIForServerRunning();
            saveConfig();
            readLogFile();
            watchLogFile();
        } catch (NumberFormatException e) {
            updateServiceLabel("Invalid Port", "red");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void stopButtonOnAction(ActionEvent event) {
        if (server != null) {
            server.stopServer();
        }

        updateServiceLabel("Stopped", "red");
        logTextArea.setStyle("-fx-text-fill: white");
    }

    private void restartServer() throws IOException {
        if (server != null) {
            server.stopServer();
        }
        PORT = Integer.parseInt(portTextField.getText());
        WEB_DIR = dirTextField.getText();
        LOG_PATH = logTextField.getText();
        server = new ServerManager(PORT, WEB_DIR, LOG_PATH);
        server.startServer();
    }

    private void updateUIForServerRunning() {
        updateServiceLabel("Running", "green");
        logTextArea.setEditable(false);
        logTextArea.setStyle("-fx-text-fill: black");
    }

    private void updateServiceLabel(String text, String color) {
        serviceLabel.setText(text);
        serviceLabel.setStyle("-fx-text-fill: " + color);
    }

    private void updateUIWithConfig() {
        portTextField.setText(String.valueOf(PORT));
        dirTextField.setText(WEB_DIR);
        logTextField.setText(LOG_PATH);
    }

    private void watchLogFile() {
        try {
            Path logDir = Paths.get(LOG_PATH);
            WatchService watchService = FileSystems.getDefault().newWatchService();
            logDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                Path modifiedFilePath = (Path) event.context();
                                if (modifiedFilePath.endsWith(logFileName)) {
                                    updateLogTextArea();
                                }
                            }
                        }
                        if (!key.reset()) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLogTextArea() {
        Path logFilePath = Paths.get(LOG_PATH, logFileName);
        try {
            String logContent = Files.readString(logFilePath);
            Platform.runLater(() -> logTextArea.setText(logContent));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readLogFile() {
        Path logFilePath = Paths.get(LOG_PATH, logFileName);
        try {
            if (Files.exists(logFilePath)) {
                String logContent = Files.readString(logFilePath);
                logTextArea.setText(logContent);
            } else {
                Files.createFile(logFilePath);
                logTextArea.setText("");
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

    private void chooseFileOrDirectory(String title, TextField textField) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("HTML Files", "*.html"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            if (selectedFile.isDirectory()) {
                textField.setText(selectedFile.getAbsolutePath());
            } else if (selectedFile.getAbsolutePath().toLowerCase().endsWith(".html")) {
                textField.setText(selectedFile.getAbsolutePath());
            } else {
                chooseDirectory(title, textField);
            }
        } else {
            chooseDirectory(title, textField);
        }
    }

    public void loadConfig() {
        Properties prop = new Properties();
        File configFile = new File(CONFIG_FILE);

        // Load the properties
        boolean updated = false;
        try (InputStream input = new FileInputStream(configFile)) {
            prop.load(input);

            // Check and assign default values if properties are missing or invalid
            PORT = getIntProperty(prop, "port", DEFAULT_PORT);
            if (prop.getProperty("port") == null) {
                prop.setProperty("port", String.valueOf(DEFAULT_PORT));
                updated = true;
            }

            WEB_DIR = prop.getProperty("webDirectory", DEFAULT_WEB_DIR);
            if (WEB_DIR == null || WEB_DIR.isEmpty()) {
                WEB_DIR = DEFAULT_WEB_DIR;
                prop.setProperty("webDirectory", DEFAULT_WEB_DIR);
                updated = true;
            }

            LOG_PATH = prop.getProperty("logDirectory", DEFAULT_LOG_PATH);
            if (LOG_PATH == null || LOG_PATH.isEmpty()) {
                LOG_PATH = DEFAULT_LOG_PATH;
                prop.setProperty("logDirectory", DEFAULT_LOG_PATH);
                updated = true;
            }

        } catch (IOException e) {
            e.printStackTrace();
            PORT = DEFAULT_PORT;
            WEB_DIR = DEFAULT_WEB_DIR;
            LOG_PATH = DEFAULT_LOG_PATH;
            updated = true;
        }

        // If any defaults were assigned, save them back to the config file
        if (updated) {
            try (OutputStream output = new FileOutputStream(configFile)) {
                prop.store(output, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getIntProperty(Properties prop, String key, int defaultValue) {
        String value = prop.getProperty(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    private void saveConfig() throws IOException {
        Properties prop = new Properties();
        prop.setProperty("port", String.valueOf(PORT));
        prop.setProperty("webDirectory", WEB_DIR);
        prop.setProperty("logDirectory", LOG_PATH);
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.store(output, null);
        }
    }
}
