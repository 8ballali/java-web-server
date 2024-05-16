package org.example.javawebserver;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class HelloController {

    protected static String LOG_PATH;
    protected static String WEB_DIR;
    protected static int PORT;

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
    private TextField dirTextField;

    @FXML
    private Button logButton;

    @FXML
    private Button dirButton;

    @FXML
    private TextField portTextField;

    @FXML
    public void dirButtonOnAction(ActionEvent event) {
        chooseDirectory("Choose Directory", dirTextField);
    }

    @FXML
    public void logButtonOnAction(ActionEvent event) {
        chooseDirectory("Choose Log Directory", logTextField);
    }

    @FXML
    public void initialize() {
        loadConfig();
        server = new ServerManager(PORT, WEB_DIR, LOG_PATH);
        portTextField.setText(String.valueOf(PORT));
        dirTextField.setText(WEB_DIR);
        logTextField.setText(LOG_PATH);
    }

    @FXML
    public void startButtonOnAction(ActionEvent event) {
        try {
            if (server != null) {
                server.stopServer();
            }

            PORT = Integer.parseInt(portTextField.getText());
            WEB_DIR = dirTextField.getText();
            LOG_PATH = logTextField.getText();

            server = new ServerManager(PORT, WEB_DIR, LOG_PATH);
            server.startServer();
            saveConfig();

            serviceLabel.setText("Running");
            serviceLabel.setStyle("-fx-text-fill: green");
            logTextArea.setEditable(false);
            logTextArea.setStyle("-fx-text-fill: black");
            readLogFile();
            watchLogFile();
        } catch (NumberFormatException e) {
            serviceLabel.setText("Invalid Port");
            serviceLabel.setStyle("-fx-text-fill: red");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void stopButtonOnAction(ActionEvent event) {
        if (server != null) {
            server.stopServer();
        }
        serviceLabel.setText("Stopped");
        serviceLabel.setStyle("-fx-text-fill: red");
        logTextArea.setStyle("-fx-text-fill: white");
    }

    private void watchLogFile() {
        try {
            Path logDir = Paths.get(LOG_PATH);

            WatchService watchService = FileSystems.getDefault().newWatchService();
            logDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            new Thread(() -> {
                while (true) {
                    WatchKey key;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException e) {
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Path modifiedFilePath = (Path) event.context();
                            if (modifiedFilePath.endsWith("access.log")) {
                                updateLogTextArea();
                            }
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
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

    private void readLogFile() {
        Path logFilePath = Paths.get(LOG_PATH, "access.log");

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

    public static void loadConfig() {
        try (InputStream input = new FileInputStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            PORT = Integer.parseInt(prop.getProperty("port", "8009"));
            WEB_DIR = prop.getProperty("webDirectory", "D:/web_server");
            LOG_PATH = prop.getProperty("logDirectory", "D:/log_server");
        } catch (IOException e) {
            PORT = 8009;
            WEB_DIR = "D:/web_server";
            LOG_PATH = "D:/log_server";
        }
    }

    private void saveConfig() throws IOException {
        Properties prop = new Properties();
        prop.setProperty("port", String.valueOf(PORT));
        prop.setProperty("webDirectory", WEB_DIR);
        prop.setProperty("logDirectory", LOG_PATH);
        try (OutputStream output = new FileOutputStream("config.properties")) {
            prop.store(output, null);
        }
    }
}
