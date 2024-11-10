package edu.trincoll;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import java.nio.file.Paths;
import java.util.List;

public class ImageCarousel extends Application {
    private FullScreenHandler fullScreenHandler;
    private StackPane imageContainer;
    private Button toggleFullScreenButton;
    private VBox root;
    private List<Image> images;
    private int currentIndex = 0;

    @Override
    public void start(Stage primaryStage) {
        // Initialize UI components with better positioning
        imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: black;");

        // Make imageContainer fill available space
        VBox.setVgrow(imageContainer, Priority.ALWAYS);

        toggleFullScreenButton = new Button("Toggle Full Screen");
        toggleFullScreenButton.setStyle("-fx-background-color: white; -fx-padding: 5 10 5 10;");

        // Create a container for the button with proper alignment
        StackPane buttonContainer = new StackPane(toggleFullScreenButton);
        buttonContainer.setPadding(new Insets(10));
        buttonContainer.setAlignment(Pos.TOP_RIGHT);

        // Stack the button container over the image container
        StackPane mainContainer = new StackPane(imageContainer, buttonContainer);
        VBox.setVgrow(mainContainer, Priority.ALWAYS);

        // The root VBox now just contains the main container
        root = new VBox(mainContainer);
        root.setFillWidth(true);

        // Load images from resources
        loadImages();

        // Set up the stage and scene
        Scene scene = new Scene(root);

        // Add ESC key handler
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE && primaryStage.isFullScreen()) {
                handleFullScreenToggle();
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Image Carousel");

        // Initialize full-screen handler
        fullScreenHandler = new FullScreenHandler(primaryStage, imageContainer, toggleFullScreenButton, root);

        // Add full screen property listener
        primaryStage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            fullScreenHandler.adjustCurrentImageSize();
            toggleFullScreenButton.setText(newVal ? "Exit Full Screen" : "Toggle Full Screen");
        });

        // Set button action for toggling full-screen
        toggleFullScreenButton.setOnAction(event -> handleFullScreenToggle());

        primaryStage.show();

        // Start image rotation if images are loaded
        if (images != null && !images.isEmpty()) {
            startImageRotation();
        }
    }

    private void loadImages() {
        ImageLoader imageLoader = new ImageLoader(Paths.get("src/main/resources"));
        images = imageLoader.loadImages();

        if (!images.isEmpty()) {
            setImage(images.get(currentIndex));
        }
    }

    private void startImageRotation() {
        Timeline rotationTimeline = new Timeline(
                new KeyFrame(Duration.seconds(3), event -> rotateImage())
        );
        rotationTimeline.setCycleCount(Timeline.INDEFINITE);
        rotationTimeline.play();
    }

    private void rotateImage() {
        currentIndex = (currentIndex + 1) % images.size();
        setImage(images.get(currentIndex));
    }

    private void setImage(Image image) {
        imageContainer.getChildren().clear();
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);

        // Bind the ImageView size to its container
        imageView.fitWidthProperty().bind(imageContainer.widthProperty());
        imageView.fitHeightProperty().bind(imageContainer.heightProperty());

        imageContainer.getChildren().add(imageView);

        // Adjust size when setting new image
        if (fullScreenHandler != null) {
            fullScreenHandler.adjustCurrentImageSize();
        }
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