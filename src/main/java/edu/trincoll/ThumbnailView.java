package edu.trincoll;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.function.Consumer;

public class ThumbnailView {
    private static final double THUMB_SIZE = 120;
    private static final double SPACING = 10;

    private final ScrollPane scrollPane;
    private final FlowPane flowPane;
    private Consumer<Integer> onThumbnailSelected;

    public ThumbnailView() {
        flowPane = new FlowPane();
        flowPane.setHgap(SPACING);
        flowPane.setVgap(SPACING);
        flowPane.setPadding(new Insets(SPACING));
        flowPane.setPrefWrapLength(THUMB_SIZE * 5 + SPACING * 4); // Show 5 thumbnails per row by default

        scrollPane = new ScrollPane(flowPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: black;");
        flowPane.setStyle("-fx-background: black;");
    }

    public void setImages(List<Image> images, int currentIndex) {
        flowPane.getChildren().clear();

        for (int i = 0; i < images.size(); i++) {
            Image image = images.get(i);
            ImageView thumbView = createThumbnail(image);

            // Highlight current image
            StackPane thumbContainer = new StackPane(thumbView);
            if (i == currentIndex) {
                thumbContainer.setStyle("-fx-border-color: white; -fx-border-width: 2;");
            }

            final int index = i;
            thumbContainer.setOnMouseClicked(e -> {
                if (onThumbnailSelected != null) {
                    onThumbnailSelected.accept(index);
                }
            });

            flowPane.getChildren().add(thumbContainer);
        }
    }

    private ImageView createThumbnail(Image image) {
        ImageView thumbView = new ImageView(image);
        thumbView.setFitWidth(THUMB_SIZE);
        thumbView.setFitHeight(THUMB_SIZE);
        thumbView.setPreserveRatio(true);
        return thumbView;
    }

    public void setOnThumbnailSelected(Consumer<Integer> handler) {
        this.onThumbnailSelected = handler;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public void scrollToThumbnail(int index) {
        if (index >= 0 && index < flowPane.getChildren().size()) {
            StackPane thumb = (StackPane) flowPane.getChildren().get(index);
            scrollPane.setVvalue(thumb.getBoundsInParent().getMinY() /
                                 (flowPane.getBoundsInParent().getHeight() - scrollPane.getViewportBounds().getHeight()));
        }
    }

    public void updateSelection(int newIndex) {
        for (int i = 0; i < flowPane.getChildren().size(); i++) {
            StackPane thumbContainer = (StackPane) flowPane.getChildren().get(i);
            if (i == newIndex) {
                thumbContainer.setStyle("-fx-border-color: white; -fx-border-width: 2;");
            } else {
                thumbContainer.setStyle(null);
            }
        }
        scrollToThumbnail(newIndex);
    }
}