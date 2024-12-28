package net.hearnsoft.animesceneloader.model;

public enum AnimationType {
    FADE_IN("fade_in"),
    FADE_OUT("fade_out"),
    MOVE("move"),
    SCALE("scale"),
    ROTATE("rotate"),
    COMPOSITE("composite"),
    CUSTOM_PATH("custom_path"),
    MARQUEE("marquee");

    private final String value;

    AnimationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AnimationType fromString(String text) {
        for (AnimationType type : AnimationType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown animation type: " + text);
    }
}
