package edu.trincoll;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class TransitionController {
    private static final Duration TRANSITION_DURATION = Duration.millis(500);
    private final StackPane container;
    private ParallelTransition currentTransition;

    public TransitionController(StackPane container) {
        this.container = container;
    }

    public void transition(ImageView oldView, ImageView newView, TransitionType type) {
        // Stop any ongoing transition
        if (currentTransition != null) {
            currentTransition.stop();
        }

        // Reset any existing transforms
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

        switch (type) {
            case NONE -> container.getChildren().setAll(newView);
            case FADE -> performFadeTransition(oldView, newView);
            case SLIDE_LEFT -> performSlideTransition(oldView, newView, -1);
            case SLIDE_RIGHT -> performSlideTransition(oldView, newView, 1);
        }
    }

    private void performFadeTransition(ImageView oldView, ImageView newView) {
        newView.setOpacity(0);
        container.getChildren().add(newView);

        FadeTransition fadeOut = new FadeTransition(TRANSITION_DURATION, oldView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(TRANSITION_DURATION, newView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        currentTransition = new ParallelTransition(fadeOut, fadeIn);
        currentTransition.setOnFinished(e -> container.getChildren().remove(oldView));
        currentTransition.play();
    }

    private void performSlideTransition(ImageView oldView, ImageView newView, double direction) {
        double width = container.getWidth();
        newView.setTranslateX(width * direction);
        container.getChildren().add(newView);

        TranslateTransition slideOut = new TranslateTransition(TRANSITION_DURATION, oldView);
        slideOut.setToX(-width * direction);

        TranslateTransition slideIn = new TranslateTransition(TRANSITION_DURATION, newView);
        slideIn.setToX(0);

        currentTransition = new ParallelTransition(slideOut, slideIn);
        currentTransition.setOnFinished(e -> {
            container.getChildren().remove(oldView);
            oldView.setTranslateX(0);
        });
        currentTransition.play();
    }
}