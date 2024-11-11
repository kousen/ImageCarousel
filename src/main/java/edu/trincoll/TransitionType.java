package edu.trincoll;

public enum TransitionType {
    NONE("None"),
    FADE("Fade"),
    SLIDE_LEFT("Slide Left"),
    SLIDE_RIGHT("Slide Right");

    private final String displayName;

    TransitionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
