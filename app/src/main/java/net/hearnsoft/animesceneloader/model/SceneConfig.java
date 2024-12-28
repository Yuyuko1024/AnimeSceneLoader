package net.hearnsoft.animesceneloader.model;

import java.util.List;

public class SceneConfig {
    private String name;
    private String version;
    private String description;
    private String author;
    private ResourcePaths resources;
    private SceneSettings sceneConfig;
    private List<Scene> scenes;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public ResourcePaths getResources() {
        return resources;
    }

    public void setResources(ResourcePaths resources) {
        this.resources = resources;
    }

    public SceneSettings getSceneConfig() {
        return sceneConfig;
    }

    public void setSceneConfig(SceneSettings sceneConfig) {
        this.sceneConfig = sceneConfig;
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    public void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
    }

    public static class ResourcePaths {
        private String bgm;
        private String scene_background_dir;
        private String character_dir;
        private String effects_json_dir;
        private String effects_sound_dir;

        // Getters and setters...

        public String getBgm() {
            return bgm;
        }

        public void setBgm(String bgm) {
            this.bgm = bgm;
        }

        public String getScene_background_dir() {
            return scene_background_dir;
        }

        public void setScene_background_dir(String scene_background_dir) {
            this.scene_background_dir = scene_background_dir;
        }

        public String getCharacter_dir() {
            return character_dir;
        }

        public void setCharacter_dir(String character_dir) {
            this.character_dir = character_dir;
        }

        public String getEffects_json_dir() {
            return effects_json_dir;
        }

        public void setEffects_json_dir(String effects_json_dir) {
            this.effects_json_dir = effects_json_dir;
        }

        public String getEffects_sound_dir() {
            return effects_sound_dir;
        }

        public void setEffects_sound_dir(String effects_sound_dir) {
            this.effects_sound_dir = effects_sound_dir;
        }
    }

    public static class SceneSettings {
        private int fps;
        private float bgm_volume;
        private boolean scene_duration_sync_with_bgm;
        private long scene_duration;

        // Getters and setters...

        public int getFps() {
            return fps;
        }

        public void setFps(int fps) {
            this.fps = fps;
        }

        public float getBgm_volume() {
            return bgm_volume;
        }

        public void setBgm_volume(float bgm_volume) {
            this.bgm_volume = bgm_volume;
        }

        public boolean isScene_duration_sync_with_bgm() {
            return scene_duration_sync_with_bgm;
        }

        public void setScene_duration_sync_with_bgm(boolean scene_duration_sync_with_bgm) {
            this.scene_duration_sync_with_bgm = scene_duration_sync_with_bgm;
        }

        public long getScene_duration() {
            return scene_duration;
        }

        public void setScene_duration(long scene_duration) {
            this.scene_duration = scene_duration;
        }
    }
}
