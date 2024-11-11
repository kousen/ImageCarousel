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

        // Create the transition type combo
        transitionCombo = new ComboBox<>();
        transitionCombo.getItems().addAll(TransitionType.values());
        transitionCombo.setValue(currentSettings.transitionType());
        transitionCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(TransitionType type) {
                return type != null ? type.getDisplayName() : "";
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
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Rotation Speed (seconds):"), 0, 0);
        grid.add(speedSlider, 1, 0);
        grid.add(new Label("Transition Effect:"), 0, 1);
        grid.add(transitionCombo, 1, 1);

        getDialogPane().setContent(grid);

        // Convert the result when save button is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Settings(speedSlider.getValue(), transitionCombo.getValue());
            }
            return null;
        });
    }
}