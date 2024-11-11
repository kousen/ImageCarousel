package edu.trincoll;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

public class PreferencesManager {
    private static final String LAST_DIRECTORY_KEY = "lastDirectory";
    private final Preferences prefs;

    public PreferencesManager() {
        prefs = Preferences.userNodeForPackage(ImageCarousel.class);
    }

    public Path getLastDirectory() {
        String defaultPath = Paths.get("src/main/resources").toAbsolutePath().toString();
        String savedPath = prefs.get(LAST_DIRECTORY_KEY, defaultPath);
        return Paths.get(savedPath);
    }

    public void saveLastDirectory(Path directory) {
        prefs.put(LAST_DIRECTORY_KEY, directory.toAbsolutePath().toString());
    }
}