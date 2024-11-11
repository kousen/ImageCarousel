package edu.trincoll;

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
import javafx.stage.Screen;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;

import java.io.File;
import java.nio.file.*;
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

    private ZoomController zoomController;
    private TransitionController transitionController;
    private ThumbnailView thumbnailView;
    private StackPane viewContainer;
    private boolean showingThumbnails = false;
    private Stage primaryStage;

    private Settings currentSettings;

    @Override
    public void start(Stage primaryStage) {
        initializeComponents(primaryStage);
        setupMainContainer();
        setupWindow();
        setupEventHandlers();
        loadInitialImages();
    }

    private void initializeComponents(Stage stage) {
        this.primaryStage = stage;
        prefsManager = new PreferencesManager();
        currentSettings = prefsManager.loadSettings();  // Load saved settings
        statusBar = new StatusBar();
        transitionController = new TransitionController(imageContainer);
        thumbnailView = new ThumbnailView();

        imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: black;");
        VBox.setVgrow(imageContainer, Priority.ALWAYS);

        // Initialize zoom controller with a blank ImageView
        ImageView initialView = new ImageView();
        initialView.setPreserveRatio(true);
        zoomController = new ZoomController(initialView);
        zoomController.getScrollPane().setStyle("-fx-background-color: black;");

        viewContainer = new StackPane();
        viewContainer.setStyle("-fx-background-color: black;");
        viewContainer.getChildren().add(zoomController.getScrollPane());
        VBox.setVgrow(viewContainer, Priority.ALWAYS);

        // Set up thumbnail selection handler
        thumbnailView.setOnThumbnailSelected(index -> {
            currentIndex = index;
            setImage(images.get(currentIndex));
        });
    }

    private void setupMainContainer() {
        MenuBar menuBar = createMenuBar();

        toggleFullScreenButton = new Button("Toggle Full Screen");
        toggleFullScreenButton.setStyle("-fx-background-color: white; -fx-padding: 5 10 5 10;");

        StackPane buttonContainer = new StackPane(toggleFullScreenButton);
        buttonContainer.setPadding(new Insets(10));
        buttonContainer.setAlignment(Pos.TOP_RIGHT);

        StackPane mainContainer = new StackPane();
        mainContainer.getChildren().addAll(viewContainer, buttonContainer);
        VBox.setVgrow(mainContainer, Priority.ALWAYS);

        root = new VBox(menuBar, createToolbar(), mainContainer, statusBar);
        root.setFillWidth(true);
    }

    private void setupWindow() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double initialWidth = Math.min(800, screenBounds.getWidth() * 0.8);
        double initialHeight = Math.min(600, screenBounds.getHeight() * 0.8);

        primaryStage.setWidth(initialWidth);
        primaryStage.setHeight(initialHeight);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(300);

        Scene scene = createScene();
        primaryStage.setScene(scene);
        primaryStage.setTitle("Image Carousel");

        fullScreenHandler = new FullScreenHandler(primaryStage, imageContainer, toggleFullScreenButton, root);
    }

    private Scene createScene() {
        Scene scene = new Scene(root);
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE -> {
                    if (primaryStage.isFullScreen()) {
                        handleFullScreenToggle();
                    }
                }
                case LEFT -> showPreviousImage();
                case RIGHT -> showNextImage();
                case SPACE -> togglePause();
                case CONTROL -> zoomController.resetZoom();
            }
        });
        return scene;
    }

    private ToolBar createToolbar() {
        prevButton = new Button("Previous");
        nextButton = new Button("Next");
        pauseButton = new Button("Pause");
        ToggleButton thumbsButton = new ToggleButton("Thumbnails");

        prevButton.setOnAction(e -> showPreviousImage());
        nextButton.setOnAction(e -> showNextImage());
        pauseButton.setOnAction(e -> togglePause());
        thumbsButton.setOnAction(e -> toggleThumbnailView());

        return new ToolBar(
                prevButton,
                pauseButton,
                nextButton,
                new Separator(),
                thumbsButton
        );
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem openMenuItem = new MenuItem("Open Directory...");
        openMenuItem.setOnAction(event -> handleOpenDirectory());
        fileMenu.getItems().add(openMenuItem);

        // Settings menu
        Menu settingsMenu = new Menu("Settings");
        MenuItem settingsMenuItem = new MenuItem("Carousel Settings...");
        settingsMenuItem.setOnAction(event -> showSettingsDialog());
        settingsMenu.getItems().add(settingsMenuItem);

        menuBar.getMenus().addAll(fileMenu, settingsMenu);

        return menuBar;
    }

    private void showSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(currentSettings);
        dialog.showAndWait().ifPresent(newSettings -> {
            // Store old settings to check what changed
            Settings oldSettings = currentSettings;
            currentSettings = newSettings;
            prefsManager.saveSettings(newSettings);

            // If rotation speed changed and carousel is running, restart it
            if (oldSettings.rotationSpeed() != newSettings.rotationSpeed() &&
                rotationTimeline != null && !isPaused) {
                startImageRotation();
            }

            // Update status bar to reflect new settings
            updateStatusBar();
        });
    }

    private void updateStatusBar() {
        StringBuilder info = new StringBuilder();

        if (images != null && !images.isEmpty()) {
            Image currentImage = images.get(currentIndex);
            info.append(String.format("Image %d of %d", currentIndex + 1, images.size()));
            info.append(String.format(" (%dx%d)", (int)currentImage.getWidth(),
                    (int)currentImage.getHeight()));

            if (currentDirectory != null) {
                try {
                    String filename = prefsManager.getLastDirectory()
                            .relativize(currentDirectory)
                            .toString();
                    info.append(" - ").append(filename);
                } catch (IllegalArgumentException e) {
                    info.append(" - ").append(currentDirectory.getFileName());
                }
            }

            // Add current transition type
            info.append(" | Transition: ")
                    .append(currentSettings.transitionType().getDisplayName());
        }

        statusBar.updateImageInfo(info.toString());
    }

    private void handleOpenDirectory() {
        stopImageRotation();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Image Directory");

        File initialDir = currentDirectory != null && Files.exists(currentDirectory)
                ? currentDirectory.toFile()
                : new File(System.getProperty("user.home"));
        directoryChooser.setInitialDirectory(initialDir);

        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory != null) {
            loadImages(selectedDirectory.toPath());
        }

        if (!isPaused) {
            startImageRotationIfPossible();
        }
    }

    private void setupEventHandlers() {
        primaryStage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            fullScreenHandler.adjustCurrentImageSize();
            toggleFullScreenButton.setText(newVal ? "Exit Full Screen" : "Toggle Full Screen");
        });

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> handleWindowResize());
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> handleWindowResize());

        toggleFullScreenButton.setOnAction(event -> handleFullScreenToggle());
    }

    private void loadInitialImages() {
        Path startDirectory = prefsManager.getLastDirectory();
        if (!Files.exists(startDirectory)) {
            startDirectory = Paths.get("src/main/resources");
            if (!Files.exists(startDirectory)) {
                startDirectory = Paths.get(System.getProperty("user.home"));
            }
        }

        primaryStage.show();
        loadImages(startDirectory);
        startImageRotationIfPossible();
    }

    private void toggleThumbnailView() {
        showingThumbnails = !showingThumbnails;
        viewContainer.getChildren().clear();
        if (showingThumbnails) {
            thumbnailView.setImages(images, currentIndex);
            viewContainer.getChildren().add(thumbnailView.getScrollPane());
        } else {
            viewContainer.getChildren().add(zoomController.getScrollPane());
        }
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

    public void loadImages(Path directory) {
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
                new KeyFrame(Duration.seconds(currentSettings.rotationSpeed()),
                        event -> showNextImage())
        );
        rotationTimeline.setCycleCount(Timeline.INDEFINITE);
        rotationTimeline.play();
    }

    private void handleWindowResize() {
        if (images != null && !images.isEmpty()) {
            setImage(images.get(currentIndex));
        }
    }

    public void setImage(Image image) {
        if (image == null) return;

        ImageView oldView = getCurrentImageView();
        ImageView newView = new ImageView(image);
        newView.setPreserveRatio(true);

        // Calculate scaling to fit the window
        double windowWidth = primaryStage.getWidth();
        double windowHeight = primaryStage.getHeight()
                              - root.lookup(".tool-bar").getBoundsInLocal().getHeight()
                              - root.lookup(".menu-bar").getBoundsInLocal().getHeight()
                              - statusBar.getHeight();

        double scale = Math.min(
                windowWidth / image.getWidth(),
                windowHeight / image.getHeight()
        );

        newView.setFitWidth(image.getWidth() * scale);
        newView.setFitHeight(image.getHeight() * scale);

        if (oldView != null && !showingThumbnails) {
            transitionController.transition(oldView, newView,
                    currentSettings.transitionType());
        } else {
            imageContainer.getChildren().clear();
            imageContainer.getChildren().add(newView);
        }

        zoomController.setImage(newView);

        if (showingThumbnails) {
            thumbnailView.updateSelection(currentIndex);
        }

        updateStatusBar();  // Use the new updateStatusBar method
    }

    private ImageView getCurrentImageView() {
        return imageContainer.getChildren().stream()
                .filter(node -> node instanceof ImageView)
                .map(node -> (ImageView) node)
                .findFirst()
                .orElse(null);
    }

    private void updateStatusBar(Image image) {
        StringBuilder info = new StringBuilder();

        // Add image count
        info.append(String.format("Image %d of %d", currentIndex + 1, images.size()));

        // Add image dimensions
        info.append(String.format(" (%dx%d)", (int)image.getWidth(), (int)image.getHeight()));

        // Add directory information
        if (currentDirectory != null) {
            try {
                String filename = prefsManager.getLastDirectory().relativize(currentDirectory).toString();
                info.append(" - ").append(filename);
            } catch (IllegalArgumentException e) {
                info.append(" - ").append(currentDirectory.getFileName());
            }
        }

        statusBar.updateImageInfo(info.toString());
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

    // Getters for testing
    public int getCurrentIndex() {
        return currentIndex;
    }

    public List<Image> getImages() {
        return images;
    }

    public Button getNextButton() {
        return nextButton;
    }

    public Button getPreviousButton() {
        return prevButton;
    }
}