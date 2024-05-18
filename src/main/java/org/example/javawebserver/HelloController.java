package org.example.javawebserver;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class HelloController {

    protected static String LOG_PATH;
    protected static String WEB_DIR;
    protected static int PORT;
    private static String logFileName;

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
        chooseFileOrDirectory("Choose Directory or Root HTML File", dirTextField);
    }

    @FXML
    public void logButtonOnAction(ActionEvent event) {
        chooseDirectory("Choose Log Directory", logTextField);
    }
    //first method executed when app executed
    @FXML
    public void initialize() {
        loadConfig();
        logFileName = Logger.logFileName; // Use the same log file name determined by Logger
        server = new ServerManager(PORT, WEB_DIR, LOG_PATH);
        portTextField.setText(String.valueOf(PORT));
        dirTextField.setText(WEB_DIR);
        logTextField.setText(LOG_PATH);
    }
    //to start
    @FXML
    public void startButtonOnAction(ActionEvent event) {
        try {
            if (server != null) {
                server.stopServer();
            }

            PORT = Integer.parseInt(portTextField.getText());
            WEB_DIR = dirTextField.getText();
            LOG_PATH = logTextField.getText();

            logFileName = Logger.logFileName; // Update log file name
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
    //to stop
    @FXML
    public void stopButtonOnAction(ActionEvent event) {
        if (server != null) {
            server.stopServer();
        }
        serviceLabel.setText("Stopped");
        serviceLabel.setStyle("-fx-text-fill: red");
        logTextArea.setStyle("-fx-text-fill: white");
    }
    //to prevent blinked screen when updating, use thread to automatically update
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
                            if (modifiedFilePath.endsWith(logFileName)) {
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
    //to update Text area
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
    //function to select .html file or choose directory
    private void chooseFileOrDirectory(String title, TextField textField) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        // Set up extension filters for HTML files and all files
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("HTML Files", "*.html"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Show file chooser dialog to select a file
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            if (selectedFile.isDirectory()) {
                // If a directory is selected, set the text field with the directory path
                textField.setText(selectedFile.getAbsolutePath());
            } else {
                // If a file is selected, check if it's an HTML file
                String filePath = selectedFile.getAbsolutePath();
                if (filePath.toLowerCase().endsWith(".html")) {
                    // If it's an HTML file, set the text field with the file path
                    textField.setText(filePath);
                } else {
                    // Otherwise, prompt to choose a directory
                    chooseDirectory(title, textField);
                }
            }
        } else {
            // If no file is selected, prompt to choose a directory
            chooseDirectory(title, textField);
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
