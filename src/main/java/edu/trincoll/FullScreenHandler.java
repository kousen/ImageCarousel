package edu.trincoll;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FullScreenHandler {
    private final Stage stage;
    private final StackPane imageContainer;
    private final Button toggleFullScreenButton;
    private final VBox root;
    private boolean wasMaximized = false;
    private Timeline resizeTimeline;

    public FullScreenHandler(Stage stage, StackPane imageContainer, Button toggleFullScreenButton, VBox root) {
        this.stage = stage;
        this.imageContainer = imageContainer;
        this.toggleFullScreenButton = toggleFullScreenButton;
        this.root = root;
    }

    public Stage getStage() {
        return stage;
    }

    public void handleFullScreenToggle(boolean isFullScreen) {
        if (isFullScreen) {
            wasMaximized = stage.isMaximized();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            imageContainer.setPrefSize(screenBounds.getWidth(), screenBounds.getHeight());
            toggleFullScreenButton.setText("Exit Full Screen");
        } else {
            toggleFullScreenButton.setText("Toggle Full Screen");
            stopResizeTimeline();
            startResizeTimeline();
        }
        toggleFullScreenButton.setVisible(true);
        adjustCurrentImageSize();
    }

    private void stopResizeTimeline() {
        if (resizeTimeline != null) {
            resizeTimeline.stop();
        }
    }

    private void startResizeTimeline() {
        resizeTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> attemptResize()),
                new KeyFrame(Duration.millis(100), e -> attemptResize()),
                new KeyFrame(Duration.millis(250), e -> attemptResize()),
                new KeyFrame(Duration.millis(500), e -> attemptResize())
        );
        resizeTimeline.play();
    }

    public void adjustCurrentImageSize() {
        Image currentImage = imageContainer.getChildren().stream()
                .filter(node -> node instanceof ImageView)
                .map(node -> (ImageView) node)
                .map(ImageView::getImage)
                .findFirst()
                .orElse(null);

        if (currentImage == null || currentImage.isError()) return;

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        if (stage.isFullScreen()) {
            imageContainer.setPrefSize(screenBounds.getWidth(), screenBounds.getHeight());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
            return;
        }

        double imgWidth = currentImage.getWidth();
        double imgHeight = currentImage.getHeight();
        double maxWidth = screenBounds.getWidth() * 0.9;
        double maxHeight = screenBounds.getHeight() * 0.9;
        double scale = Math.min(maxWidth / imgWidth, maxHeight / imgHeight);

        double targetWidth = Math.max(800, Math.min(imgWidth * scale, maxWidth));
        double targetHeight = Math.max(600, Math.min(imgHeight * scale, maxHeight));

        stage.setWidth(targetWidth);
        stage.setHeight(targetHeight);

        // Set container to match stage size
        imageContainer.setPrefSize(targetWidth, targetHeight);

        stage.centerOnScreen();
        root.layout();
    }

    private void attemptResize() {
        Platform.runLater(() -> {
            if (!stage.isFullScreen()) {
                if (wasMaximized) {
                    stage.setMaximized(true);
                } else {
                    adjustCurrentImageSize();
                }
                root.layout();
            }
        });
    }
}