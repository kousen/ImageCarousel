package edu.trincoll;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class StatusBar extends HBox {
    private final Label directoryLabel;
    private final Label imageLabel;
    private final Label errorLabel;

    public StatusBar() {
        super(10); // 10px spacing between elements
        setPadding(new Insets(5));
        setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");

        directoryLabel = new Label();
        imageLabel = new Label();
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(directoryLabel, spacer, imageLabel, errorLabel);
    }

    public void updateDirectory(String directory) {
        directoryLabel.setText("Directory: " + directory);
    }

    public void updateImageInfo(String imageInfo) {
        imageLabel.setText(imageInfo);
    }

    public void showError(String error) {
        errorLabel.setText(error);
        // Clear error after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            javafx.application.Platform.runLater(() -> errorLabel.setText(""));
        }).start();
    }

    public void clearError() {
        errorLabel.setText("");
    }
}