package edu.trincoll;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ImageCarousel extends Application {
    private FullScreenHandler fullScreenHandler;
    private StackPane imageContainer;
    private Button toggleFullScreenButton;
    private VBox root;
    private List<Image> images;
    private int currentIndex = 0;
    private Timeline rotationTimeline;
    private Path currentDirectory;
    private PreferencesManager prefsManager;
    private StatusBar statusBar;
    private Button prevButton;
    private Button nextButton;
    private Button pauseButton;
    private boolean isPaused = false;

    @Override
    public void start(Stage primaryStage) {
        prefsManager = new PreferencesManager();
        statusBar = new StatusBar();

        // Create menu bar and control buttons
        MenuBar menuBar = createMenuBar(primaryStage);
        ToolBar toolbar = createToolbar();

        // Initialize UI components with better positioning
        imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: black;");

        VBox.setVgrow(imageContainer, Priority.ALWAYS);

        toggleFullScreenButton = new Button("Toggle Full Screen");
        toggleFullScreenButton.setStyle("-fx-background-color: white; -fx-padding: 5 10 5 10;");

        StackPane buttonContainer = new StackPane(toggleFullScreenButton);
        buttonContainer.setPadding(new Insets(10));
        buttonContainer.setAlignment(Pos.TOP_RIGHT);

        StackPane mainContainer = new StackPane(imageContainer, buttonContainer);
        VBox.setVgrow(mainContainer, Priority.ALWAYS);

        root = new VBox(menuBar, toolbar, mainContainer, statusBar);
        root.setFillWidth(true);

        // Try to load images from last used directory, fall back to resources, then user.home
        Path startDirectory = prefsManager.getLastDirectory();
        if (!Files.exists(startDirectory)) {
            startDirectory = Paths.get("src/main/resources");
            if (!Files.exists(startDirectory)) {
                startDirectory = Paths.get(System.getProperty("user.home"));
            }
        }
        loadImages(startDirectory);

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    if (primaryStage.isFullScreen()) {
                        handleFullScreenToggle();
                    }
                    break;
                case LEFT:
                    showPreviousImage();
                    break;
                case RIGHT:
                    showNextImage();
                    break;
                case SPACE:
                    togglePause();
                    break;
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Image Carousel");

        fullScreenHandler = new FullScreenHandler(primaryStage, imageContainer, toggleFullScreenButton, root);

        primaryStage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            fullScreenHandler.adjustCurrentImageSize();
            toggleFullScreenButton.setText(newVal ? "Exit Full Screen" : "Toggle Full Screen");
        });

        toggleFullScreenButton.setOnAction(event -> handleFullScreenToggle());

        primaryStage.show();
        startImageRotationIfPossible();
    }

    private ToolBar createToolbar() {
        prevButton = new Button("Previous");
        nextButton = new Button("Next");
        pauseButton = new Button("Pause");

        prevButton.setOnAction(e -> showPreviousImage());
        nextButton.setOnAction(e -> showNextImage());
        pauseButton.setOnAction(e -> togglePause());

        return new ToolBar(prevButton, pauseButton, nextButton);
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            stopImageRotation();
            pauseButton.setText("Resume");
        } else {
            startImageRotationIfPossible();
            pauseButton.setText("Pause");
        }
    }

    private void showPreviousImage() {
        if (images != null && !images.isEmpty()) {
            currentIndex = (currentIndex - 1 + images.size()) % images.size();
            setImage(images.get(currentIndex));
        }
    }

    private void showNextImage() {
        if (images != null && !images.isEmpty()) {
            currentIndex = (currentIndex + 1) % images.size();
            setImage(images.get(currentIndex));
        }
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem openMenuItem = new MenuItem("Open Directory...");

        openMenuItem.setOnAction(event -> {
            stopImageRotation();

            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Image Directory");

            File initialDir = currentDirectory != null && Files.exists(currentDirectory)
                    ? currentDirectory.toFile()
                    : new File(System.getProperty("user.home"));
            directoryChooser.setInitialDirectory(initialDir);

            File selectedDirectory = directoryChooser.showDialog(stage);
            if (selectedDirectory != null) {
                loadImages(selectedDirectory.toPath());
                if (!isPaused) {
                    startImageRotationIfPossible();
                }
            } else if (!isPaused) {
                startImageRotationIfPossible();
            }
        });

        fileMenu.getItems().add(openMenuItem);
        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    private void loadImages(Path directory) {
        try {
            currentDirectory = directory;
            System.out.println("Loading images from: " + directory.toAbsolutePath());
            statusBar.updateDirectory(directory.getFileName().toString());

            ImageLoader imageLoader = new ImageLoader(directory);
            images = imageLoader.loadImages();

            if (!images.isEmpty()) {
                System.out.println("Found " + images.size() + " images");
                currentIndex = 0;
                setImage(images.get(currentIndex));
                prefsManager.saveLastDirectory(directory);
                updateNavigationButtons(true);
            } else {
                System.out.println("No images found in directory");
                statusBar.showError("No images found in selected directory");
                imageContainer.getChildren().clear();
                updateNavigationButtons(false);
            }
        } catch (SecurityException e) {
            String error = "Access denied to directory: " + directory;
            System.err.println(error);
            statusBar.showError(error);
            updateNavigationButtons(false);
        } catch (Exception e) {
            String error = "Error loading images: " + e.getMessage();
            System.err.println(error);
            statusBar.showError(error);
            updateNavigationButtons(false);
        }
    }

    private void updateNavigationButtons(boolean enabled) {
        prevButton.setDisable(!enabled);
        nextButton.setDisable(!enabled);
        pauseButton.setDisable(!enabled);
    }

    private void startImageRotationIfPossible() {
        if (images != null && !images.isEmpty() && !isPaused) {
            startImageRotation();
        }
    }

    private void stopImageRotation() {
        if (rotationTimeline != null) {
            rotationTimeline.stop();
            rotationTimeline = null;
        }
    }

    private void startImageRotation() {
        stopImageRotation();
        rotationTimeline = new Timeline(
                new KeyFrame(Duration.seconds(3), event -> rotateImage())
        );
        rotationTimeline.setCycleCount(Timeline.INDEFINITE);
        rotationTimeline.play();
    }

    private void rotateImage() {
        showNextImage();
    }

    private void setImage(Image image) {
        imageContainer.getChildren().clear();
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);

        imageView.fitWidthProperty().bind(imageContainer.widthProperty());
        imageView.fitHeightProperty().bind(imageContainer.heightProperty());

        imageContainer.getChildren().add(imageView);

        if (fullScreenHandler != null) {
            fullScreenHandler.adjustCurrentImageSize();
        }

        // Update status bar with current image info
        String imageInfo = String.format("Image %d of %d", currentIndex + 1, images.size());
        if (currentDirectory != null) {
            try {
                String filename = prefsManager.getLastDirectory().relativize(currentDirectory).toString();
                imageInfo += " - " + filename;
            } catch (IllegalArgumentException e) {
                imageInfo += " - " + currentDirectory.getFileName();
            }
        }
        statusBar.updateImageInfo(imageInfo);
    }

    private void handleFullScreenToggle() {
        if (fullScreenHandler != null) {
            boolean willBeFullScreen = !fullScreenHandler.getStage().isFullScreen();
            fullScreenHandler.handleFullScreenToggle(willBeFullScreen);
            fullScreenHandler.getStage().setFullScreen(willBeFullScreen);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}