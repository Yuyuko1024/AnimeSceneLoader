package net.hearnsoft.animesceneloader.model;

import java.util.List;

public class Scene {
    private String name;
    private String description;
    private List<Background> background;
    private List<SceneElement> elements;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Background> getBackground() {
        return background;
    }

    public void setBackground(List<Background> background) {
        this.background = background;
    }

    public List<SceneElement> getElements() {
        return elements;
    }

    public void setElements(List<SceneElement> elements) {
        this.elements = elements;
    }

    // Getters and setters...

    public static class Background {
        private String type;  // "image" or "video"
        private String name;
        private String path;
        private String effect;
        private Duration duration;

        // Getters and setters...

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getEffect() {
            return effect;
        }

        public void setEffect(String effect) {
            this.effect = effect;
        }

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }
    }
}
