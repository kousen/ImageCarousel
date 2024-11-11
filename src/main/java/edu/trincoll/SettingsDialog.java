package edu.trincoll;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

public class SettingsDialog extends Dialog<Settings> {
    private final Slider speedSlider;
    private final ComboBox<TransitionType> transitionCombo;

    public SettingsDialog(Settings currentSettings) {
        setTitle("Carousel Settings");
        setHeaderText("Adjust carousel display settings");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the speed slider
        speedSlider = new Slider(1, 10, currentSettings.rotationSpeed());
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(1);
        speedSlider.setBlockIncrement(1);
        speedSlider.setSnapToTicks(true);

        // Create the transition type combo with better labels
        transitionCombo = new ComboBox<>();
        transitionCombo.getItems().addAll(TransitionType.values());
        transitionCombo.setValue(currentSettings.transitionType());
        transitionCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(TransitionType type) {
                if (type == null) return "";
                return switch (type) {
                    case NONE -> "No Transition";
                    case FADE -> "Fade Effect";
                    case SLIDE_LEFT -> "Slide Left";
                    case SLIDE_RIGHT -> "Slide Right";
                };
            }

            @Override
            public TransitionType fromString(String string) {
                return TransitionType.valueOf(string);
            }
        });

        // Create the layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        // Add labels and controls with better descriptions
        grid.add(new Label("Rotation Speed:"), 0, 0);
        grid.add(new Label("(seconds between images)"), 1, 0);
        grid.add(speedSlider, 0, 1, 2, 1); // Span 2 columns

        grid.add(new Label("Transition Effect:"), 0, 2);
        grid.add(transitionCombo, 0, 3, 2, 1); // Span 2 columns

        getDialogPane().setContent(grid);

        // Convert the result when save button is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Settings(speedSlider.getValue(), transitionCombo.getValue());
            }
            return null;
        });

        // Set minimum dialog width for better appearance
        getDialogPane().setMinWidth(300);
    }
}