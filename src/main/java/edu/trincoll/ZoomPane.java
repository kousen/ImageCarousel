package edu.trincoll;

import javafx.geometry.Point2D;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class ZoomPane extends ScrollPane {
    private double scaleValue = 1.0;
    private final StackPane content;
    private static final double MIN_SCALE = 0.5;
    private static final double MAX_SCALE = 5.0;
    private static final double SCALE_DELTA = 1.1;
    private Point2D dragAnchor;

    public ZoomPane(StackPane content) {
        this.content = content;

        // Initial setup
        setPannable(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.NEVER);
        setContent(content);

        // Handle scroll to zoom
        setOnScroll(event -> {
            event.consume();

            if (event.getDeltaY() == 0) return;

            double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA : 1/SCALE_DELTA;

            scaleValue = Math.min(MAX_SCALE, Math.max(MIN_SCALE, scaleValue * scaleFactor));

            content.setScaleX(scaleValue);
            content.setScaleY(scaleValue);

            // Adjust viewport to zoom around mouse position
            double mouseX = event.getX();
            double mouseY = event.getY();
            double viewportWidth = getViewportBounds().getWidth();
            double viewportHeight = getViewportBounds().getHeight();

            double contentWidth = content.getBoundsInLocal().getWidth() * scaleValue;
            double contentHeight = content.getBoundsInLocal().getHeight() * scaleValue;

            double hvalue = (mouseX / viewportWidth) * (contentWidth - viewportWidth);
            double vvalue = (mouseY / viewportHeight) * (contentHeight - viewportHeight);

            setHvalue(hvalue);
            setVvalue(vvalue);
        });

        // Handle mouse drag for panning
        setOnMousePressed(event -> dragAnchor = new Point2D(event.getX(), event.getY()));

        setOnMouseDragged(event -> {
            if (dragAnchor != null && scaleValue > 1.0) {
                double xDelta = event.getX() - dragAnchor.getX();
                double yDelta = event.getY() - dragAnchor.getY();

                setHvalue(getHvalue() - xDelta);
                setVvalue(getVvalue() - yDelta);

                dragAnchor = new Point2D(event.getX(), event.getY());
            }
        });

        // Reset zoom on double-click
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                resetZoom();
            }
        });
    }

    public void resetZoom() {
        scaleValue = 1.0;
        content.setScaleX(scaleValue);
        content.setScaleY(scaleValue);
        setHvalue(0);
        setVvalue(0);
    }

    public ImageView getCurrentImageView() {
        return content.getChildren().stream()
                .filter(node -> node instanceof ImageView)
                .map(node -> (ImageView) node)
                .findFirst()
                .orElse(null);
    }
}