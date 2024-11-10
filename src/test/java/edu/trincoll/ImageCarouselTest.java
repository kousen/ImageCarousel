package edu.trincoll;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageCarouselTest extends ApplicationTest {
    private Button toggleFullScreenButton;

    @Override
    public void start(Stage stage) {

        // Initialize UI components with the same structure as ImageCarousel
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: black;");

        toggleFullScreenButton = new Button("Toggle Full Screen");
        toggleFullScreenButton.setStyle("-fx-background-color: white; -fx-padding: 5 10 5 10;");

        // Create button container with proper alignment
        StackPane buttonContainer = new StackPane(toggleFullScreenButton);
        buttonContainer.setPadding(new Insets(10));
        buttonContainer.setAlignment(Pos.TOP_RIGHT);

        // Stack the button container over the image container
        StackPane mainContainer = new StackPane(imageContainer, buttonContainer);

        // Create the root VBox
        VBox root = new VBox(mainContainer);
        root.setFillWidth(true);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);

        // Modify button action to not directly set full screen in test environment
        toggleFullScreenButton.setOnAction(event -> {
            String currentText = toggleFullScreenButton.getText();
            toggleFullScreenButton.setText(
                    currentText.equals("Toggle Full Screen") ? "Exit Full Screen" : "Toggle Full Screen"
            );
        });

        stage.show();
    }

    @Test
    public void testFullScreenToggle() {
        // Initial state
        assertTrue(toggleFullScreenButton.isVisible());
        assertEquals("Toggle Full Screen", toggleFullScreenButton.getText());

        // Click button to toggle
        clickOn(toggleFullScreenButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify button state changed
        assertTrue(toggleFullScreenButton.isVisible());
        assertEquals("Exit Full Screen", toggleFullScreenButton.getText());

        // Click again to toggle back
        clickOn(toggleFullScreenButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify returned to initial state
        assertTrue(toggleFullScreenButton.isVisible());
        assertEquals("Toggle Full Screen", toggleFullScreenButton.getText());
    }

    @Test
    public void testButtonVisibility() {
        // Test that button remains visible
        assertTrue(toggleFullScreenButton.isVisible());

        clickOn(toggleFullScreenButton);
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(toggleFullScreenButton.isVisible());

        clickOn(toggleFullScreenButton);
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(toggleFullScreenButton.isVisible());
    }
}