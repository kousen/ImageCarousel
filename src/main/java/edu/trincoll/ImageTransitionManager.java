package edu.trincoll;

import javafx.animation.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ImageTransitionManager {
    private static final double TRANSITION_DURATION = 500; // milliseconds
    private final StackPane container;

    public ImageTransitionManager(StackPane container) {
        this.container = container;
    }

    public void transition(ImageView oldView, ImageView newView, TransitionType type) {
        switch (type) {
            case NONE:
                container.getChildren().setAll(newView);
                break;

            case FADE:
                newView.setOpacity(0);
                container.getChildren().add(newView);

                FadeTransition fadeOut = new FadeTransition(Duration.millis(TRANSITION_DURATION), oldView);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(TRANSITION_DURATION), newView);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                ParallelTransition parallel = new ParallelTransition(fadeOut, fadeIn);
                parallel.setOnFinished(e -> container.getChildren().remove(oldView));
                parallel.play();
                break;

            case SLIDE_LEFT:
                performSlideTransition(oldView, newView, -1);
                break;

            case SLIDE_RIGHT:
                performSlideTransition(oldView, newView, 1);
                break;
        }
    }

    private void performSlideTransition(ImageView oldView, ImageView newView, double direction) {
        double width = container.getWidth();
        newView.setTranslateX(width * direction);
        container.getChildren().add(newView);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(TRANSITION_DURATION), oldView);
        slideOut.setToX(-width * direction);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(TRANSITION_DURATION), newView);
        slideIn.setToX(0);

        ParallelTransition parallel = new ParallelTransition(slideOut, slideIn);
        parallel.setOnFinished(e -> {
            container.getChildren().remove(oldView);
            oldView.setTranslateX(0);
        });
        parallel.play();
    }
}