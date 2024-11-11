package edu.trincoll;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ThumbnailViewTest extends ApplicationTest {
    private ThumbnailView thumbnailView;
    private List<Image> testImages;

    @Override
    public void start(Stage stage) {
        thumbnailView = new ThumbnailView();
        Scene scene = new Scene(thumbnailView.getScrollPane(), 800, 600);
        stage.setScene(scene);
        stage.show();
        WaitForAsyncUtils.waitForFxEvents();
    }

    @BeforeEach
    public void setUp() {
        System.out.println("Starting setUp...");

        Path resourcesPath = Paths.get("src/main/resources");
        ImageLoader imageLoader = new ImageLoader(resourcesPath);
        final List<Image> initialImages = imageLoader.loadImages();

        System.out.println("Found " + initialImages.size() + " images");

        if (initialImages.size() < 2) {
            fail("Need at least 2 images in resources directory for testing");
        }

        final CountDownLatch imagesLoaded = new CountDownLatch(2);

        final List<String> imageUrls = initialImages.subList(0, 2).stream()
                .map(Image::getUrl)
                .toList();

        interact(() -> {
            testImages = new ArrayList<>();

            for (String url : imageUrls) {
                Image newImage = new Image(url, true);
                testImages.add(newImage);

                newImage.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == 1.0 && !newImage.isError()) {
                        System.out.println("Image loaded: " + newImage.getUrl());
                        imagesLoaded.countDown();
                    }
                });

                if (newImage.getProgress() == 1.0 && !newImage.isError()) {
                    System.out.println("Image already loaded: " + newImage.getUrl());
                    imagesLoaded.countDown();
                }
            }
        });

        try {
            System.out.println("Waiting for images to load...");
            boolean completed = imagesLoaded.await(5, TimeUnit.SECONDS);
            System.out.println("Image loading completed: " + completed);

            assertTrue(completed, "Timeout waiting for images to load");

            testImages.forEach(image -> {
                assertFalse(image.isError(), "Image should not have errors: " + image.getUrl());
                assertEquals(1.0, image.getProgress(), "Image should be fully loaded: " + image.getUrl());
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Test interrupted while waiting for images to load");
        }

        System.out.println("setUp completed successfully");
    }

    @Test
    public void testThumbnailSelection() {
        // Set images and verify initial state
        interact(() -> {
            thumbnailView.setImages(testImages, 0);
            WaitForAsyncUtils.waitForFxEvents();
        });

        // Verify thumbnails exist
        Set<Node> thumbnails = lookup(".stack-pane").queryAll();
        assertTrue(thumbnails.size() >= 2,
                "Expected at least 2 thumbnails but found " + thumbnails.size());

        // Click the first thumbnail and verify selection
        Node firstThumbnail = thumbnailView.getScrollPane().getContent().lookup(".stack-pane");
        assertNotNull(firstThumbnail, "First thumbnail should exist");

        clickOn(firstThumbnail);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(0, thumbnailView.getSelectedIndex(),
                "Expected selected index to match clicked thumbnail");
    }

    @Test
    public void testUpdateSelection() {
        interact(() -> {
            thumbnailView.setImages(testImages, 0);
            WaitForAsyncUtils.waitForFxEvents();
        });

        interact(() -> {
            thumbnailView.updateSelection(1);
            WaitForAsyncUtils.waitForFxEvents();
        });

        assertEquals(1, thumbnailView.getSelectedIndex(),
                "Expected selected index to update to 1");
    }
}