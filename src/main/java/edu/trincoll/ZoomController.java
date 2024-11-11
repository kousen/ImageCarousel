package edu.trincoll;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
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
        // Handle scroll wheel with modifier key
        scrollPane.addEventFilter(ScrollEvent.ANY, event -> {
            if (event.isMetaDown() || event.isControlDown()) {  // Meta is Command on Mac
                event.consume();
                double zoomFactor = event.getDeltaY() > 0 ? ZOOM_FACTOR : 1 / ZOOM_FACTOR;
                zoom(zoomFactor, new Point2D(event.getX(), event.getY()));
            }
        });

        // Handle keyboard shortcuts
        EventHandler<KeyEvent> zoomKeyHandler = event -> {
            if (event.isMetaDown() || event.isControlDown()) {
                KeyCode code = event.getCode();

                // Handle zoom in: Plus or Equals key
                if (code == KeyCode.ADD || code == KeyCode.EQUALS) {
                    event.consume();
                    zoom(ZOOM_FACTOR, getCenterPoint());
                }
                // Handle zoom out: Minus key
                else if (code == KeyCode.SUBTRACT || code == KeyCode.MINUS) {
                    event.consume();
                    zoom(1 / ZOOM_FACTOR, getCenterPoint());
                }
            }
        };

        // Add the handler to all relevant components
        scrollPane.addEventFilter(KeyEvent.KEY_PRESSED, zoomKeyHandler);
        zoomPane.addEventFilter(KeyEvent.KEY_PRESSED, zoomKeyHandler);
        currentImageView.addEventFilter(KeyEvent.KEY_PRESSED, zoomKeyHandler);

        // Reset zoom on double-click
        zoomPane.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                resetZoom();
            }
        });

        // Add visual feedback for zoom mode
        scrollPane.addEventHandler(KeyEvent.KEY_PRESSED,
                e -> {
                    if (e.isMetaDown() || e.isControlDown()) {
                        scrollPane.setCursor(Cursor.CROSSHAIR);
                    }
                });

        scrollPane.addEventHandler(KeyEvent.KEY_RELEASED,
                e -> {
                    if (!e.isMetaDown() && !e.isControlDown()) {
                        scrollPane.setCursor(Cursor.DEFAULT);
                    }
                });
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

    private Point2D getCenterPoint() {
        return new Point2D(
                scrollPane.getViewportBounds().getWidth() / 2,
                scrollPane.getViewportBounds().getHeight() / 2
        );
    }

    private void zoom(double factor, Point2D pivot) {
        double newZoom = currentZoom * factor;

        if (newZoom >= MIN_ZOOM && newZoom <= MAX_ZOOM) {
            currentZoom = newZoom;

            double mouseX = pivot.getX();
            double mouseY = pivot.getY();

            double relativeX = (mouseX - currentImageView.getTranslateX()) / currentImageView.getScaleX();
            double relativeY = (mouseY - currentImageView.getTranslateY()) / currentImageView.getScaleY();

            currentImageView.setScaleX(currentZoom);
            currentImageView.setScaleY(currentZoom);

            double newX = mouseX - (relativeX * currentZoom);
            double newY = mouseY - (relativeY * currentZoom);

            currentImageView.setTranslateX(newX);
            currentImageView.setTranslateY(newY);

            System.out.printf("Zoom: %.0f%%%n", currentZoom * 100);
        }
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

        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        if (viewportWidth > 0 && viewportHeight > 0) {
            double scaleX = viewportWidth / newImageView.getImage().getWidth();
            double scaleY = viewportHeight / newImageView.getImage().getHeight();
            double scale = Math.min(scaleX, scaleY);

            newImageView.setFitWidth(newImageView.getImage().getWidth() * scale);
            newImageView.setFitHeight(newImageView.getImage().getHeight() * scale);
        }
    }
}