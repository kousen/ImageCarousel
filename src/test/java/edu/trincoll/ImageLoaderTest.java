package edu.trincoll;

import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ImageLoaderTest extends ApplicationTest {

    @Test
    public void testLoadImages() {
        Path testResources = Paths.get("src/main/resources");
        ImageLoader imageLoader = new ImageLoader(testResources);

        List<Image> images = imageLoader.loadImages();
        assertNotNull(images);
        assertFalse(images.isEmpty());
        assertEquals(3, images.size());
    }
}