package edu.trincoll;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

public class ZoomController {
    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 5.0;
    private static final double ZOOM_FACTOR = 1.1;

    private final ScrollPane scrollPane;
    private final StackPane zoomPane;
    private ImageView currentImageView;
    private double currentZoom = 1.0;
    private Point2D dragAnchor;

    public ZoomController(ImageView initialImageView) {
        this.currentImageView = initialImageView;
        this.zoomPane = new StackPane(initialImageView);
        this.scrollPane = new ScrollPane(zoomPane);

        setupScrollPane();
        setupZoomHandling();
        setupDragHandling();
    }

    private void setupScrollPane() {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        scrollPane.setStyle("-fx-background-color: black;");
        zoomPane.setStyle("-fx-background-color: black;");
    }

    private void setupZoomHandling() {
        EventHandler<KeyEvent> keyHandler = event -> {
            if (event.isControlDown()) {
                if (event.getCode() == KeyCode.EQUALS ||
                    event.getCode() == KeyCode.PLUS ||
                    event.getCode() == KeyCode.ADD) {
                    event.consume();
                    zoom(ZOOM_FACTOR, getCenterPoint());
                }
                else if (event.getCode() == KeyCode.MINUS ||
                         event.getCode() == KeyCode.SUBTRACT) {
                    event.consume();
                    zoom(1 / ZOOM_FACTOR, getCenterPoint());
                }
            }
        };

        // Add key handler to scene
        scrollPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
            }
        });

        // Also add to individual components for redundancy
        scrollPane.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        zoomPane.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        currentImageView.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
    }

    private Point2D getCenterPoint() {
        return new Point2D(
                scrollPane.getViewportBounds().getWidth() / 2,
                scrollPane.getViewportBounds().getHeight() / 2
        );
    }

    public void zoom(double factor, Point2D pivot) {
        double newZoom = currentZoom * factor;

        if (newZoom >= MIN_ZOOM && newZoom <= MAX_ZOOM) {
            currentZoom = newZoom;
            currentImageView.setScaleX(currentZoom);
            currentImageView.setScaleY(currentZoom);

            double mouseX = pivot.getX();
            double mouseY = pivot.getY();
            double relativeX = (mouseX - currentImageView.getTranslateX()) / currentImageView.getScaleX();
            double relativeY = (mouseY - currentImageView.getTranslateY()) / currentImageView.getScaleY();
            double newX = mouseX - (relativeX * currentZoom);
            double newY = mouseY - (relativeY * currentZoom);

            currentImageView.setTranslateX(newX);
            currentImageView.setTranslateY(newY);
        }
    }

    private void setupDragHandling() {
        zoomPane.setOnMousePressed(event ->
                dragAnchor = new Point2D(event.getX(), event.getY()));

        zoomPane.setOnMouseDragged(event -> {
            if (dragAnchor != null && currentZoom > MIN_ZOOM) {
                double xDelta = event.getX() - dragAnchor.getX();
                double yDelta = event.getY() - dragAnchor.getY();

                currentImageView.setTranslateX(currentImageView.getTranslateX() + xDelta);
                currentImageView.setTranslateY(currentImageView.getTranslateY() + yDelta);

                dragAnchor = new Point2D(event.getX(), event.getY());
            }
        });

        zoomPane.setOnMouseReleased(e -> dragAnchor = null);
    }

    public void resetZoom() {
        currentZoom = 1.0;
        currentImageView.setScaleX(1.0);
        currentImageView.setScaleY(1.0);
        currentImageView.setTranslateX(0);
        currentImageView.setTranslateY(0);
        scrollPane.setHvalue(0.5);
        scrollPane.setVvalue(0.5);
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public void setImage(ImageView newImageView) {
        resetZoom();
        zoomPane.getChildren().clear();
        this.currentImageView = newImageView;
        zoomPane.getChildren().add(newImageView);
    }

    public double getCurrentZoom() {
        return currentZoom;
    }
}