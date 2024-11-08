package edu.trincoll;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImageCarousel extends Application {
    private static final double DEFAULT_WIDTH = 800;
    private static final double DEFAULT_HEIGHT = 600;
    private static final double PADDING = 20;
    private static final double IMAGE_ROTATION_PERIOD_SECONDS = 3;

    private final VBox root = new VBox(10);
    private final StackPane imageContainer = new StackPane();
    private final ImageView imageView = new ImageView();
    private final Map<Image, String> imageFilenames = new HashMap<>();

    private List<Image> images;
    private Iterator<Image> imageIterator;

    private Stage stage;
    private Button toggleFullScreenButton;
    private boolean wasMaximized = false;
    private Timeline resizeTimeline;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        images = loadImages();

        if (images.isEmpty()) {
            System.err.println("No valid images found in resources directory!");
            return;
        }

        configureStage();
        initializeComponents();
        setupRoot();
        setupKeyboardHandlers();

        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        stage.setScene(scene);
        stage.show();
        startImageRotation();
    }

    private void configureStage() {
        stage.setTitle("Image Carousel");
    }

    private void initializeComponents() {
        setupImageView();
        setupControls();
    }

    private void setupControls() {
        toggleFullScreenButton = new Button("Toggle Full Screen (F11)");
        toggleFullScreenButton.setOnAction(e -> toggleFullScreen());
        toggleFullScreenButton.setMaxWidth(Double.MAX_VALUE);
    }

    private void setupRoot() {
        root.setPadding(new Insets(PADDING));
        imageContainer.getChildren().add(imageView);
        root.getChildren().addAll(toggleFullScreenButton, imageContainer);
        VBox.setVgrow(imageContainer, Priority.ALWAYS);
    }

    private void setupKeyboardHandlers() {
        Scene scene = stage.getScene();
        if (scene == null) {
            return;
        }

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11 || (e.getCode() == KeyCode.ESCAPE && stage.isFullScreen())) {
                toggleFullScreen();
            }
        });

        stage.fullScreenProperty().addListener((obs, wasFullScreen, isFullScreen) -> handleFullScreenToggle(isFullScreen));
    }

    private void handleFullScreenToggle(boolean isFullScreen) {
        if (isFullScreen) {
            wasMaximized = stage.isMaximized();
            imageContainer.setPrefSize(
                    Screen.getPrimary().getVisualBounds().getWidth(),
                    Screen.getPrimary().getVisualBounds().getHeight()
            );
            toggleFullScreenButton.setVisible(false);
        } else {
            stopResizeTimeline();
            startResizeTimeline();
        }
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

    private void attemptResize() {
        Platform.runLater(() -> {
            if (!stage.isFullScreen()) {
                toggleFullScreenButton.setVisible(true);
                imageContainer.setPrefSize(-1, -1);
                if (wasMaximized) {
                    stage.setMaximized(true);
                } else {
                    adjustCurrentImageSize();
                }
                root.layout();
                stage.sizeToScene();
            }
        });
    }

    private void toggleFullScreen() {
        stage.setFullScreen(!stage.isFullScreen());
    }

    private void setupImageView() {
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(imageContainer.widthProperty());
        imageView.fitHeightProperty().bind(imageContainer.heightProperty());
    }

    private void startImageRotation() {
        if (!images.isEmpty()) {
            imageIterator = images.iterator();
            changeImage();
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(IMAGE_ROTATION_PERIOD_SECONDS), e -> changeImage())
            );
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }
    }

    private List<Image> loadImages() {
        Path resourcesPath = Paths.get("src/main/resources").toAbsolutePath();
        try (Stream<Path> paths = Files.walk(resourcesPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedImage)
                    .map(this::loadImage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error scanning resources directory: " + e.getMessage());
            return List.of();
        }
    }

    private boolean isSupportedImage(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        boolean isSupported = name.endsWith(".jpg") ||
                              name.endsWith(".jpeg") ||
                              name.endsWith(".png") ||
                              name.endsWith(".gif");
        if (!isSupported) {
            System.out.println("Skipping unsupported file type: " + name);
        }
        return isSupported;
    }

    private Image loadImage(Path path) {
        try {
            try (var input = Files.newInputStream(path)) {
                Image img = new Image(input);
                if (!img.isError() && img.getWidth() > 0) {
                    String fileUri = path.toUri().toString();
                    Image finalImg = new Image(fileUri, true);
                    imageFilenames.put(finalImg, path.getFileName().toString());
                    return finalImg;
                }
                System.err.println("Initial load failed for " + path.getFileName() +
                                   " (width=" + img.getWidth() + ")");
            }
        } catch (Exception e) {
            System.err.println("Error loading " + path.getFileName() + ": " + e.getMessage());
        }
        return null;
    }

    private void changeImage() {
        if (!imageIterator.hasNext()) {
            imageIterator = images.iterator();
        }
        Image newImage = imageIterator.next();
        if (!newImage.isError()) {
            imageView.setImage(newImage);
            System.out.println("Displaying: " + imageFilenames.get(newImage));
            adjustCurrentImageSize();
        }
    }

    private void adjustCurrentImageSize() {
        Image currentImage = imageView.getImage();
        if (currentImage == null || currentImage.isError()) return;

        double imgWidth = currentImage.getWidth();
        double imgHeight = currentImage.getHeight();
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        if (stage.isFullScreen()) {
            imageContainer.setPrefSize(screenBounds.getWidth(), screenBounds.getHeight());
            toggleFullScreenButton.setVisible(false);
            return;
        }

        imageContainer.setPrefSize(-1, -1);
        toggleFullScreenButton.setVisible(true);

        double maxWidth = screenBounds.getWidth() * 0.9;
        double maxHeight = screenBounds.getHeight() * 0.9;
        double scale = Math.min(maxWidth / imgWidth, maxHeight / imgHeight);
        Insets padding = root.getPadding();
        double buttonHeight = toggleFullScreenButton.prefHeight(-1);
        double paddingWidth = padding.getLeft() + padding.getRight();
        double paddingHeight = padding.getTop() + padding.getBottom() + buttonHeight + 10;

        stage.setWidth(Math.max(DEFAULT_WIDTH, Math.min(imgWidth * scale, maxWidth) + paddingWidth));
        stage.setHeight(Math.max(DEFAULT_HEIGHT, Math.min(imgHeight * scale, maxHeight) + paddingHeight));
        stage.centerOnScreen();

        root.layout();
    }

    public static void main(String[] args) {
        launch(args);
    }
}