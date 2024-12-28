package net.hearnsoft.animesceneloader.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

import net.hearnsoft.animesceneloader.model.SceneElement;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SceneRenderer {
    private static final String TAG = "SceneRenderer";
    private static final int DEFAULT_FPS = 60;  // 默认帧率

    private final SurfaceHolder holder;
    private final Context context;
    private boolean isRunning;
    private RenderThread renderThread;
    private long startTime;
    private long sceneDuration;  // 添加场景时长
    private List<SceneElement> elements;
    private Map<String, Bitmap> resourceCache;
    private File baseResourceDir;  // 添加基础资源目录

    private Bitmap backgroundBitmap;
    private String backgroundEffect;
    private long backgroundStartTime;
    private long backgroundEndTime;

    private FPSCallback fpsCallback;
    private TimeCallback timeCallback;
    private long frameCount;
    private long lastFPSUpdateTime;
    private static final long FPS_UPDATE_INTERVAL = 500; // 每500ms更新一次FPS

    private int targetFps;  // 目标帧率
    private long frameIntervalNanos;  // 使用纳秒级的帧间隔以提高精度

    // 添加回调接口
    public interface FPSCallback {
        void onFPSUpdate(int fps);
    }

    public interface TimeCallback {
        void onTimeUpdate(long currentTime, long totalTime);
    }

    public void setFPSCallback(FPSCallback callback) {
        this.fpsCallback = callback;
    }

    public void setTimeCallback(TimeCallback callback) {
        this.timeCallback = callback;
    }

    public SceneRenderer(Context context, SurfaceHolder holder) {
        this.context = context;
        this.holder = holder;
        this.resourceCache = new HashMap<>();
        this.elements = new ArrayList<>();
        this.sceneDuration = 0;  // 默认时长为0
        setFps(DEFAULT_FPS);  // 设置默认帧率
    }

    public void setBaseResourceDir(File resourceDir) {
        this.baseResourceDir = resourceDir;
    }

    public void start() {
        if (renderThread != null) {
            stop();
        }
        isRunning = true;
        startTime = System.currentTimeMillis();
        renderThread = new RenderThread();
        renderThread.start();
    }

    public void stop() {
        isRunning = false;
        if (renderThread != null) {
            try {
                renderThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error stopping render thread", e);
            }
            renderThread = null;
        }
        // 清理渲染状态
        clearRenderState();
    }

    private class RenderThread extends Thread {
        private Bitmap bufferBitmap;
        private Canvas bufferCanvas;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Matrix matrix = new Matrix();

        @Override
        public void run() {
            bufferBitmap = Bitmap.createBitmap(holder.getSurfaceFrame().width(), holder.getSurfaceFrame().height(), Bitmap.Config.ARGB_8888);
            bufferCanvas = new Canvas(bufferBitmap);

            long lastFrameNanos = System.nanoTime();
            lastFPSUpdateTime = System.currentTimeMillis();
            frameCount = 0;

            while (isRunning) {
                long currentNanos = System.nanoTime();
                long elapsedNanos = currentNanos - lastFrameNanos;

                if (elapsedNanos >= frameIntervalNanos) {
                    try {
                        bufferCanvas.drawColor(Color.WHITE);

                        long currentTimeMillis = System.currentTimeMillis();
                        long sceneTime = currentTimeMillis - startTime;

                        frameCount++;
                        if (currentTimeMillis - lastFPSUpdateTime >= FPS_UPDATE_INTERVAL) {
                            float fps = (frameCount * 1000f) / (currentTimeMillis - lastFPSUpdateTime);
                            if (fpsCallback != null) {
                                fpsCallback.onFPSUpdate(Math.round(fps));
                            }
                            frameCount = 0;
                            lastFPSUpdateTime = currentTimeMillis;
                        }

                        if (timeCallback != null) {
                            timeCallback.onTimeUpdate(sceneTime, sceneDuration);
                        }

                        if (sceneDuration > 0 && sceneTime >= sceneDuration) {
                            isRunning = false;
                            continue;
                        }

                        renderBackground(bufferCanvas, sceneTime, matrix, paint);
                        for (SceneElement element : elements) {
                            renderElement(bufferCanvas, element, sceneTime, matrix, paint);
                        }

                        Canvas canvas = holder.lockCanvas();
                        if (canvas != null) {
                            canvas.drawBitmap(bufferBitmap, 0, 0, null);
                            holder.unlockCanvasAndPost(canvas);
                        }

                        lastFrameNanos = currentNanos;
                    } catch (Exception e) {
                        Log.e(TAG, "Error in render loop", e);
                    }

                    long endNanos = System.nanoTime();
                    long remainingNanos = frameIntervalNanos - (endNanos - currentNanos);
                    if (remainingNanos > 0) {
                        long sleepMillis = remainingNanos / 1_000_000;
                        int sleepNanos = (int) (remainingNanos % 1_000_000);
                        try {
                            Thread.sleep(sleepMillis, sleepNanos);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }
    }

    private void renderElement(Canvas canvas, SceneElement element, long currentTime, Matrix matrix, Paint paint) {
        Bitmap bitmap = getElementBitmap(element);
        if (bitmap == null) return;

        SceneElement.Animation currentAnimation = null;
        for (SceneElement.Animation animation : element.getScenes()) {
            if (currentTime >= animation.getDuration().getFrom() &&
                    currentTime <= animation.getDuration().getTo()) {
                currentAnimation = animation;
                break;
            }
        }

        if (currentAnimation != null) {
            float progress = (float) (currentTime - currentAnimation.getDuration().getFrom()) /
                    (currentAnimation.getDuration().getTo() - currentAnimation.getDuration().getFrom());

            applyAnimation(canvas, bitmap, currentAnimation, progress, matrix, paint);
        }
    }

    private void renderBackground(Canvas canvas, long currentTime, Matrix matrix, Paint paint) {
        if (backgroundBitmap == null) {
            canvas.drawColor(Color.WHITE);
            return;
        }

        paint.setFilterBitmap(true);

        float scale = Math.max(
                (float) canvas.getWidth() / backgroundBitmap.getWidth(),
                (float) canvas.getHeight() / backgroundBitmap.getHeight()
        );
        matrix.setScale(scale, scale);

        float dx = (canvas.getWidth() - backgroundBitmap.getWidth() * scale) * 0.5f;
        float dy = (canvas.getHeight() - backgroundBitmap.getHeight() * scale) * 0.5f;
        matrix.postTranslate(dx, dy);

        if (backgroundEffect != null && currentTime >= backgroundStartTime && currentTime <= backgroundEndTime) {
            float progress = (float) (currentTime - backgroundStartTime) / (backgroundEndTime - backgroundStartTime);
            applyBackgroundEffect(canvas, matrix, paint, progress);
        } else {
            canvas.drawBitmap(backgroundBitmap, matrix, paint);
        }
    }

    private void applyAnimation(Canvas canvas, Bitmap bitmap,
                                SceneElement.Animation animation, float progress, Matrix matrix, Paint paint) {
        matrix.reset();
        paint.setAlpha(255);  // 重置透明度

        // 获取bitmap的中心点，用于变换
        float centerX = bitmap.getWidth() / 2f;
        float centerY = bitmap.getHeight() / 2f;

        switch (animation.getType()) {
            case "move":
                applyMoveAnimation(matrix, animation, progress);
                break;
            case "scale":
                applyScaleAnimation(matrix, animation, progress);
                break;
            case "rotate":
                applyRotateAnimation(matrix, animation, progress);
                break;
            case "fade_in":
            case "fade_out":
                applyFadeAnimation(paint, animation, progress);
                break;
            case "throw":
                applyThrowAnimation(matrix, animation, progress, centerX, centerY);
                break;
            case "bounce":
                applyBounceAnimation(matrix, animation, progress, centerX, centerY);
                break;
            case "swing":
                applySwingAnimation(matrix, animation, progress, centerX, centerY);
                break;
            case "spring":
                applySpringAnimation(matrix, animation, progress, centerX, centerY);
                break;
        }

        canvas.drawBitmap(bitmap, matrix, paint);
    }

    private void applyMoveAnimation(Matrix matrix, SceneElement.Animation animation, float progress) {
        float x = interpolate(animation.getFrom().getX(), animation.getTo().getX(), progress);
        float y = interpolate(animation.getFrom().getY(), animation.getTo().getY(), progress);
        matrix.postTranslate(x, y);
    }

    private void applyScaleAnimation(Matrix matrix, SceneElement.Animation animation, float progress) {
        float scaleX = interpolate(animation.getFrom().getX(), animation.getTo().getX(), progress);
        float scaleY = interpolate(animation.getFrom().getY(), animation.getTo().getY(), progress);
        matrix.postScale(scaleX, scaleY);
    }

    private void applyRotateAnimation(Matrix matrix, SceneElement.Animation animation, float progress) {
        float angle = interpolate(animation.getFromAngle(), animation.getToAngle(), progress);
        matrix.postRotate(angle);
    }

    private void applyFadeAnimation(Paint paint, SceneElement.Animation animation, float progress) {
        float alpha = interpolate(animation.getFromAlpha(), animation.getToAlpha(), progress);
        paint.setAlpha((int) (alpha * 255));
    }

    private void applyThrowAnimation(Matrix matrix, SceneElement.Animation animation, float progress, float centerX, float centerY) {
        // 抛物线效果
        float startX = animation.getFrom().getX();
        float startY = animation.getFrom().getY();
        float endX = animation.getTo().getX();
        float endY = animation.getTo().getY();

        // 计算抛物线路径
        float gravity = 980f;  // 重力加速度，可调整
        float time = progress * 2.0f;  // 将进度映射到更合适的时间范围
        float vx = (endX - startX) / 2.0f;  // 水平速度
        float vy = -gravity * 0.5f;  // 初始垂直速度

        float x = startX + vx * time;
        float y = startY + vy * time + 0.5f * gravity * time * time;

        matrix.postTranslate(x - centerX, y - centerY);
    }

    private void applyBounceAnimation(Matrix matrix, SceneElement.Animation animation, float progress, float centerX, float centerY) {
        float bounceX = animation.getFrom().getX();
        float bounceY = animation.getFrom().getY();
        float amplitude = 100f;  // 弹跳高度
        float frequency = 3f;    // 弹跳频率
        float decay = 0.8f;      // 衰减系数

        float bounce = (float) (amplitude * (float) Math.exp(-decay * progress) *
                Math.abs(Math.sin(frequency * Math.PI * progress)));

        matrix.postTranslate(bounceX - centerX, bounceY - bounce - centerY);
    }

    private void applySwingAnimation(Matrix matrix, SceneElement.Animation animation, float progress, float centerX, float centerY) {
        float pivotX = animation.getFrom().getX();
        float pivotY = animation.getFrom().getY();
        float swingAngle = 30f;  // 最大摆动角度
        float swingFreq = 2f;    // 摆动频率
        float damping = 0.5f;    // 阻尼系数

        float finalAngle = swingAngle * (float) Math.exp(-damping * progress) *
                (float) Math.sin(swingFreq * Math.PI * progress);

        matrix.postTranslate(pivotX - centerX, pivotY - centerY);
        matrix.postRotate(finalAngle, pivotX, pivotY);
    }

    private void applySpringAnimation(Matrix matrix, SceneElement.Animation animation, float progress, float centerX, float centerY) {
        float targetX = animation.getTo().getX();
        float targetY = animation.getTo().getY();
        float springK = 8f;      // 弹簧系数
        float dampingRatio = 0.4f; // 阻尼比

        float dx = targetX - animation.getFrom().getX();
        float dy = targetY - animation.getFrom().getY();
        float springProgress = 1 - (float) (Math.exp(-springK * progress) *
                Math.cos(dampingRatio * springK * progress));

        float springX = animation.getFrom().getX() + dx * springProgress;
        float springY = animation.getFrom().getY() + dy * springProgress;

        matrix.postTranslate(springX - centerX, springY - centerY);
    }

    private void applyBackgroundEffect(Canvas canvas, Matrix matrix, Paint paint, float progress) {
        switch (backgroundEffect) {
            case "fade_in":
                paint.setAlpha((int) (progress * 255));
                break;
            case "fade_out":
                paint.setAlpha((int) ((1 - progress) * 255));
                break;
            case "zoom_in":
                float scale = 1 + progress * 0.2f;  // 放大到1.2倍
                matrix.postScale(scale, scale, canvas.getWidth() / 2f, canvas.getHeight() / 2f);
                break;
            // 可以添加更多效果...
        }
        canvas.drawBitmap(backgroundBitmap, matrix, paint);
    }

    public void setSceneDuration(long duration) {
        this.sceneDuration = duration;
    }

    public void setBackground(String path) {
        if (backgroundBitmap != null) {
            backgroundBitmap.recycle();
        }
        backgroundBitmap = BitmapFactory.decodeFile(path);
    }

    public void setBackgroundEffect(String effect) {
        this.backgroundEffect = effect;
    }

    public void setBackgroundDuration(long start, long end) {
        this.backgroundStartTime = start;
        this.backgroundEndTime = end;
    }

    public void setDefaultBackground() {
        if (backgroundBitmap != null) {
            backgroundBitmap.recycle();
            backgroundBitmap = null;
        }
        backgroundEffect = null;
    }

    public void setFps(int fps) {
        if (fps <= 0) {
            fps = DEFAULT_FPS;
            Log.w(TAG, "Invalid fps value, using default: " + DEFAULT_FPS);
        }
        this.targetFps = fps;
        this.frameIntervalNanos = 1_000_000_000L / fps;  // 转换为纳秒
    }

    private float interpolate(float start, float end, float progress) {
        // 简单的线性插值，可以根据easing类型扩展
        return start + (end - start) * progress;
    }

    private float bezierInterpolate(float p0, float p1, float p2, float t) {
        float oneMinusT = 1 - t;
        return oneMinusT * oneMinusT * p0 + 2 * oneMinusT * t * p1 + t * t * p2;
    }

    private Bitmap getElementBitmap(SceneElement element) {
        Bitmap bitmap = resourceCache.get(element.getPath());
        if (bitmap == null) {
            try {
                File file = new File(baseResourceDir, element.getPath());
                if (!file.exists()) {
                    Log.e(TAG, "Resource file not found: " + file.getAbsolutePath());
                    return null;
                }
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap == null) {
                    Log.e(TAG, "Failed to decode bitmap: " + file.getAbsolutePath());
                    return null;
                }
                resourceCache.put(element.getPath(), bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Error loading bitmap: " + element.getPath(), e);
                return null;
            }
        }
        return bitmap;
    }

    public void setElements(List<SceneElement> elements) {
        this.elements = elements;
    }

    private void clearRenderState() {
        // 清理背景相关资源
        if (backgroundBitmap != null) {
            backgroundBitmap.recycle();
            backgroundBitmap = null;
        }
        backgroundEffect = null;
        backgroundStartTime = 0;
        backgroundEndTime = 0;

        // 清理元素缓存
        for (Bitmap bitmap : resourceCache.values()) {
            bitmap.recycle();
        }
        resourceCache.clear();

        // 重置其他状态
        elements.clear();
        sceneDuration = 0;
        startTime = 0;

        // 重置帧率相关状态
        targetFps = DEFAULT_FPS;
        frameIntervalNanos = 1_000_000_000L / targetFps;
        frameCount = 0;
        lastFPSUpdateTime = 0;
    }

    public void release() {
        stop();
        // 清理资源缓存
        for (Bitmap bitmap : resourceCache.values()) {
            bitmap.recycle();
        }
        resourceCache.clear();
        clearRenderState();
    }
}
