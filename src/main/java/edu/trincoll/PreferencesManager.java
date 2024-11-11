package edu.trincoll;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

public class PreferencesManager {
    private static final String ROTATION_SPEED_KEY = "rotationSpeed";
    private static final String TRANSITION_TYPE_KEY = "transitionType";
    private static final String LAST_DIRECTORY_KEY = "lastDirectory";
    private final Preferences prefs;

    public PreferencesManager() {
        prefs = Preferences.userNodeForPackage(ImageCarousel.class);
    }

    public void saveSettings(Settings settings) {
        prefs.putDouble(ROTATION_SPEED_KEY, settings.rotationSpeed());
        prefs.put(TRANSITION_TYPE_KEY, settings.transitionType().name());
    }

    public Settings loadSettings() {
        double speed = prefs.getDouble(ROTATION_SPEED_KEY, 3.0);
        String transitionName = prefs.get(TRANSITION_TYPE_KEY, TransitionType.FADE.name());
        return new Settings(speed, TransitionType.valueOf(transitionName));
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