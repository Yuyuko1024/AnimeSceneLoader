package net.hearnsoft.animesceneloader.model;

import java.util.List;
import java.util.Map;

public class SceneElement {
    private String type;
    private String name;
    private String path;
    private int zIndex;
    private List<Animation> scenes;

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

    public int getzIndex() {
        return zIndex;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public List<Animation> getScenes() {
        return scenes;
    }

    public void setScenes(List<Animation> scenes) {
        this.scenes = scenes;
    }

    public static class Animation {
        private String type;  // fade_in, move, scale, rotate, composite, custom_path, marquee
        private Duration duration;
        private String easing;
        private Map<String, Object> params;  // 存储不同动画类型的特定参数

        // 常用动画参数
        private Point from;
        private Point to;
        private float fromAlpha;
        private float toAlpha;
        private float fromAngle;
        private float toAngle;
        private List<Point> pathPoints;
        private String pathType;
        private float speed;
        private float marqueeMargin;
        private boolean marqueeRepeat;
        private List<Animation> animations;  // 用于composite类型
        // Getters and setters...

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }

        public String getEasing() {
            return easing;
        }

        public void setEasing(String easing) {
            this.easing = easing;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public void setParams(Map<String, Object> params) {
            this.params = params;
        }

        public Point getFrom() {
            return from;
        }

        public void setFrom(Point from) {
            this.from = from;
        }

        public Point getTo() {
            return to;
        }

        public void setTo(Point to) {
            this.to = to;
        }

        public float getFromAlpha() {
            return fromAlpha;
        }

        public void setFromAlpha(float fromAlpha) {
            this.fromAlpha = fromAlpha;
        }

        public float getToAlpha() {
            return toAlpha;
        }

        public void setToAlpha(float toAlpha) {
            this.toAlpha = toAlpha;
        }

        public float getFromAngle() {
            return fromAngle;
        }

        public void setFromAngle(float fromAngle) {
            this.fromAngle = fromAngle;
        }

        public float getToAngle() {
            return toAngle;
        }

        public void setToAngle(float toAngle) {
            this.toAngle = toAngle;
        }

        public List<Point> getPathPoints() {
            return pathPoints;
        }

        public void setPathPoints(List<Point> pathPoints) {
            this.pathPoints = pathPoints;
        }

        public String getPathType() {
            return pathType;
        }

        public void setPathType(String pathType) {
            this.pathType = pathType;
        }

        public float getSpeed() {
            return speed;
        }

        public void setSpeed(float speed) {
            this.speed = speed;
        }

        public float getMarqueeMargin() {
            return marqueeMargin;
        }

        public void setMarqueeMargin(float marqueeMargin) {
            this.marqueeMargin = marqueeMargin;
        }

        public boolean isMarqueeRepeat() {
            return marqueeRepeat;
        }

        public void setMarqueeRepeat(boolean marqueeRepeat) {
            this.marqueeRepeat = marqueeRepeat;
        }

        public List<Animation> getAnimations() {
            return animations;
        }

        public void setAnimations(List<Animation> animations) {
            this.animations = animations;
        }
    }
}
