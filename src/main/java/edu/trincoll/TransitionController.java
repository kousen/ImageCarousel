package edu.trincoll;

import javafx.animation.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class TransitionController {
    private static final Duration TRANSITION_DURATION = Duration.millis(500);
    private final StackPane container;
    private Timeline timeline;

    public TransitionController(StackPane container) {
        this.container = container;
    }

    public void transitionToNewImage(ImageView oldView, ImageView newView) {
        // Stop any ongoing transitions
        if (timeline != null) {
            timeline.stop();
        }

        // Clear any existing transforms
        if (oldView != null) {
            oldView.setTranslateX(0);
            oldView.setOpacity(1.0);
            oldView.setScaleX(1.0);
            oldView.setScaleY(1.0);
        }

        newView.setTranslateX(0);
        newView.setOpacity(1.0);
        newView.setScaleX(1.0);
        newView.setScaleY(1.0);

        // Add new view to container but keep it invisible
        newView.setOpacity(0);
        container.getChildren().add(newView);

        // Create fade transition
        FadeTransition fadeOut = new FadeTransition(TRANSITION_DURATION, oldView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(TRANSITION_DURATION, newView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // Create scale transition for additional effect
        ScaleTransition scaleOut = new ScaleTransition(TRANSITION_DURATION, oldView);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.95);
        scaleOut.setToY(0.95);

        ScaleTransition scaleIn = new ScaleTransition(TRANSITION_DURATION, newView);
        scaleIn.setFromX(1.05);
        scaleIn.setFromY(1.05);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        // Combine transitions
        ParallelTransition transition = new ParallelTransition(
                fadeOut, fadeIn, scaleOut, scaleIn
        );

        // Clean up after transition
        transition.setOnFinished(e -> {
            container.getChildren().remove(oldView);
            // Reset any transformations
            newView.setTranslateX(0);
            newView.setScaleX(1.0);
            newView.setScaleY(1.0);
        });

        transition.play();
    }
}