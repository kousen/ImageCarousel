package edu.trincoll;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageCarouselNavigationTest extends ApplicationTest {
    private ImageCarousel carousel;

    @Override
    public void start(Stage stage) {
        carousel = new ImageCarousel();
        carousel.start(stage);

        // Ensure we're on the FX thread and wait for any async operations
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testNextImageNavigation() {
        // Load images on FX thread and wait for completion
        interact(() -> {
            carousel.loadImages(Paths.get("src/main/resources"));
            WaitForAsyncUtils.waitForFxEvents();
        });

        // Verify images were loaded
        assertTrue(carousel.getImages() != null && !carousel.getImages().isEmpty(),
                "Images should be loaded before testing navigation");

        int initialIndex = carousel.getCurrentIndex();

        // Perform click on FX thread
        clickOn(carousel.getNextButton());
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(initialIndex + 1, carousel.getCurrentIndex(),
                "Expected current index to increment by 1");
    }

    @Test
    public void testPreviousImageNavigationWrapsAround() {
        // Load images on FX thread and wait for completion
        interact(() -> {
            carousel.loadImages(Paths.get("src/main/resources"));
            WaitForAsyncUtils.waitForFxEvents();
        });

        // Verify images were loaded
        assertTrue(carousel.getImages() != null && !carousel.getImages().isEmpty(),
                "Images should be loaded before testing navigation");

        // Set to first image on FX thread
        interact(() -> carousel.setImage(carousel.getImages().getFirst()));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn(carousel.getPreviousButton());
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(carousel.getImages().size() - 1, carousel.getCurrentIndex(),
                "Expected wrap-around to last image");
    }
}