package edu.trincoll;

import javafx.scene.control.Slider;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SettingsDialogTest extends ApplicationTest {
    private SettingsDialog settingsDialog;

    @Override
    public void start(Stage stage) {
        settingsDialog = new SettingsDialog(new Settings(3.0, TransitionType.FADE));
        settingsDialog.initOwner(stage);
        settingsDialog.show();
    }

    @Test
    public void testChangeSettings() {
        Slider speedSlider = (Slider) settingsDialog.getDialogPane().lookup(".slider");
        speedSlider.setValue(5.0);
        clickOn("Save"); // Click the save button
        Settings result = settingsDialog.getResult();
        assertNotNull(result, "Expected settings result to be non-null");
        assertEquals(5.0, result.rotationSpeed(), "Expected rotation speed to be updated");
    }
}
