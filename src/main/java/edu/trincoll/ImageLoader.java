package edu.trincoll;

import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImageLoader {
    private final Path resourcesPath;
    private final Map<Image, String> imageFilenames = new HashMap<>();

    public ImageLoader(Path resourcesPath) {
        this.resourcesPath = resourcesPath.toAbsolutePath();
    }

    public List<Image> loadImages() {
        try (Stream<Path> paths = Files.walk(resourcesPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedImage)
                    .map(this::loadImage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error scanning resources directory: " + e.getMessage());
            return List.of();
        }
    }

    private boolean isSupportedImage(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        boolean isSupported = name.endsWith(".jpg") ||
                              name.endsWith(".jpeg") ||
                              name.endsWith(".png") ||
                              name.endsWith(".gif");
        if (!isSupported) {
            System.out.println("Skipping unsupported file type: " + name);
        }
        return isSupported;
    }

    private Image loadImage(Path path) {
        try (var input = Files.newInputStream(path)) {
            Image img = new Image(input);
            if (!img.isError() && img.getWidth() > 0) {
                String fileUri = path.toUri().toString();
                Image finalImg = new Image(fileUri, true);
                imageFilenames.put(finalImg, path.getFileName().toString());
                return finalImg;
            }
            System.err.println("Initial load failed for " + path.getFileName() + " (width=" + img.getWidth() + ")");
        } catch (Exception e) {
            System.err.println("Error loading " + path.getFileName() + ": " + e.getMessage());
        }
        return null;
    }

    public String getImageFilename(Image image) {
        return imageFilenames.get(image);
    }
}