package net.hearnsoft.animesceneloader.model;

public class Point {
    private String x;  // 改为String类型
    private String y;  // 改为String类型

    public String getXString() {
        return x;
    }

    public String getYString() {
        return y;
    }

    public float getX() {
        try {
            return Float.parseFloat(x);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public float getY() {
        try {
            return Float.parseFloat(y);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
