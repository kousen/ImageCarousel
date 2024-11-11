package edu.trincoll;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZoomControllerTest extends ApplicationTest {
    private ZoomController zoomController;

    @Override
    public void start(Stage stage) {
        ImageView initialImageView = new ImageView();
        zoomController = new ZoomController(initialImageView);
        Scene scene = new Scene(zoomController.getScrollPane(), 800, 600);
        stage.setScene(scene);
        stage.show();
        stage.requestFocus();
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testZoomInOut() {
        interact(() -> {
            press(KeyCode.CONTROL).press(KeyCode.EQUALS).press(KeyCode.SHIFT)
                    .release(KeyCode.SHIFT).release(KeyCode.EQUALS).release(KeyCode.CONTROL);
            WaitForAsyncUtils.waitForFxEvents();
        });

        assertTrue(zoomController.getCurrentZoom() > 1.0,
                "Expected zoom level greater than 1.0 after zooming in");

        interact(() -> {
            press(KeyCode.CONTROL).press(KeyCode.MINUS)
                    .release(KeyCode.MINUS).release(KeyCode.CONTROL);
            WaitForAsyncUtils.waitForFxEvents();
        });

        assertEquals(1.0, zoomController.getCurrentZoom(),
                "Expected zoom level to reset to 1.0 after zooming out");
    }

    @Test
    public void testResetZoom() {
        interact(() -> {
            zoomController.zoom(2.0, new Point2D(100, 100));
            WaitForAsyncUtils.waitForFxEvents();
            assertEquals(2.0, zoomController.getCurrentZoom(),
                    "Expected zoom to be set to 2.0");

            zoomController.resetZoom();
            WaitForAsyncUtils.waitForFxEvents();
        });

        assertEquals(1.0, zoomController.getCurrentZoom(),
                "Expected zoom level to reset to 1.0");
    }
}