package net.hearnsoft.animesceneloader.model;

public enum EasingType {
    LINEAR("linear"),
    EASE_IN("ease_in"),
    EASE_OUT("ease_out"),
    EASE_IN_OUT("ease_in_out");

    private final String value;

    EasingType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EasingType fromString(String text) {
        for (EasingType type : EasingType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        return LINEAR;  // 默认返回线性
    }
}
