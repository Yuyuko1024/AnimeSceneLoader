package net.hearnsoft.animesceneloader;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.hearnsoft.animesceneloader.databinding.ActivityMainBinding;
import net.hearnsoft.animesceneloader.model.Scene;
import net.hearnsoft.animesceneloader.model.SceneConfig;
import net.hearnsoft.animesceneloader.render.SceneRenderer;
import net.hearnsoft.animesceneloader.scene.SceneBGMPlayer;
import net.hearnsoft.animesceneloader.ui.LoadingDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity
        implements SurfaceHolder.Callback {

    private ActivityMainBinding binding;
    private static final int PICK_SCENE_PACK_REQUEST = 1;
    private File sceneCacheDir;
    private LoadingDialog progressDialog;
    private SceneBGMPlayer bgmPlayer;
    private File currentSceneDir; // 添加当前场景目录的引用
    private SceneRenderer sceneRenderer;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 全屏显示
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // 兼容EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化场景缓存目录
        sceneCacheDir = new File(getFilesDir(), "scene_cache");
        if (!sceneCacheDir.exists()) {
            sceneCacheDir.mkdirs();
        }

        binding.sceneView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        SurfaceHolder holder = binding.sceneView.getHolder();
        holder.addCallback(this);

        progressDialog = new LoadingDialog(this);
        bgmPlayer = new SceneBGMPlayer(this);
        sceneRenderer = new SceneRenderer(this, holder);
        // 初始化Gson
        gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();


        binding.loadScenePack.setOnClickListener(v -> {
            // Load the scene pack
            openFilePicker();
        });

        binding.startScene.setOnClickListener(v -> {
            // Start the scene
            startScene();
        });
        binding.stopScene.setOnClickListener(v -> {
            // Stop the scene
            stopScene();
        });

        // 设置帧率和时间回调
        sceneRenderer.setFPSCallback(fps ->
                runOnUiThread(() -> binding.fpsText.setText(String.format("FPS: %d", fps)))
        );

        sceneRenderer.setTimeCallback((currentTime, totalTime) ->
                runOnUiThread(() -> {
                    String timeText;
                    if (totalTime > 0) {
                        // 显示当前时间和总时间（秒）
                        timeText = String.format("Time: %.1f/%.1f s",
                                currentTime / 1000f,
                                totalTime / 1000f);
                    } else {
                        // 只显示当前时间
                        timeText = String.format("Time: %.1f s",
                                currentTime / 1000f);
                    }
                    binding.timeText.setText(timeText);
                })
        );
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip"); // 设置只显示 ZIP 文件

        // 可选：添加MIME类型
        String[] mimeTypes = {"application/zip", "application/x-zip", "application/x-zip-compressed"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        try {
            startActivityForResult(intent, PICK_SCENE_PACK_REQUEST);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No file picker found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_SCENE_PACK_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    // 获取文件名
                    String fileName = getFileName(uri);
                    // 显示选中的文件名
                    Toast.makeText(this, "Selected: " + fileName, Toast.LENGTH_SHORT).show();

                    // TODO: 处理选中的场景包文件
                    handleScenePackFile(uri);
                }
            }
        }
    }

    private void startScene() {
        if (currentSceneDir == null || !currentSceneDir.exists()) {
            Toast.makeText(this, "Please load a scene pack first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 读取场景配置文件
            File configFile = new File(currentSceneDir, "scene.json");
            if (!configFile.exists()) {
                Toast.makeText(this, "Scene configuration not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // 使用Gson解析配置文件
            String jsonContent = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);
            SceneConfig sceneConfig = gson.fromJson(jsonContent, SceneConfig.class);

            // 设置渲染器的资源基础目录
            sceneRenderer.setBaseResourceDir(currentSceneDir);

            // 设置FPS
            int fps = sceneConfig.getSceneConfig().getFps();
            sceneRenderer.setFps(fps);

            // 处理BGM
            if (sceneConfig.getResources() != null && sceneConfig.getResources().getBgm() != null) {
                String bgmPath = sceneConfig.getResources().getBgm();
                File bgmFile = new File(currentSceneDir, bgmPath);

                if (bgmFile.exists()) {
                    // 设置BGM音量
                    float volume = sceneConfig.getSceneConfig().getBgm_volume();
                    bgmPlayer.setVolume(volume);

                    // 设置BGM文件
                    bgmPlayer.setAudioFile(bgmFile.getAbsolutePath());

                    // 获取场景配置
                    boolean syncWithBgm = sceneConfig.getSceneConfig().isScene_duration_sync_with_bgm();
                    long sceneDuration;

                    // 设置场景持续时间
                    if (syncWithBgm) {
                        // 使用BGM长度作为场景时长
                        sceneDuration = bgmPlayer.getDuration();
                    } else {
                        // 使用配置的场景时长
                        sceneDuration = sceneConfig.getSceneConfig().getScene_duration();
                    }

                    // 设置场景持续时间
                    sceneRenderer.setSceneDuration(sceneDuration);

                    // BGM默认不循环
                    bgmPlayer.setLooping(false);
                    bgmPlayer.play();
                } else {
                    Log.e("Scene", "BGM file not found: " + bgmPath);
                }
            }

            // 处理场景元素
            if (sceneConfig.getScenes() != null && !sceneConfig.getScenes().isEmpty()) {
                Scene firstScene = sceneConfig.getScenes().get(0);

                if (firstScene.getElements() != null) {
                    sceneRenderer.setElements(firstScene.getElements());
                    sceneRenderer.start();
                }

                // TODO: 处理背景元素
                if (firstScene.getBackground() != null && !firstScene.getBackground().isEmpty()) {
                    // 处理背景元素的逻辑
                    Scene.Background background = firstScene.getBackground().get(0);

                    if (background.getType().equals("image")) {
                        // 处理图片背景元素的逻辑
                        String bgPath = background.getPath();
                        File bgFile = new File(currentSceneDir, bgPath);

                        if (bgFile.exists()) {
                            /// 设置背景到渲染器
                            sceneRenderer.setBackground(bgFile.getAbsolutePath());

                            // 如果背景有动画效果
                            if (background.getEffect() != null) {
                                sceneRenderer.setBackgroundEffect(background.getEffect());
                            }

                            // 如果背景有持续时间
                            if (background.getDuration() != null) {
                                sceneRenderer.setBackgroundDuration(
                                        background.getDuration().getFrom(),
                                        background.getDuration().getTo()
                                );
                            }
                        } else {
                            Log.e("Scene", "Background file not found: " + bgPath);
                        }
                    } else if ("video".equals(background.getType())) {
                        // TODO: 实现视频背景支持
                        Log.w("Scene", "Video background not yet supported");
                    }
                } else {
                    Log.w("Scene", "No background elements found");
                    sceneRenderer.setDefaultBackground();
                }
            }

            Toast.makeText(this, "Scene started", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("Scene", "Error starting scene", e);
            Toast.makeText(this, "Failed to start scene: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopScene() {
        // 停止并释放BGM播放器
        if (bgmPlayer != null) {
            bgmPlayer.release();
            bgmPlayer = new SceneBGMPlayer(this); // 重新初始化播放器以备后用
        }

        // 停止渲染器
        if (sceneRenderer != null) {
            sceneRenderer.stop();
            sceneRenderer.release();
        }

        // TODO: 停止其他场景元素

        // 清空显示
        binding.fpsText.setText("FPS: --");
        binding.timeText.setText("Time: --/-- s");

        Toast.makeText(this, "Scene stopped", Toast.LENGTH_SHORT).show();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void handleScenePackFile(Uri uri) {
        String fileName = getFileName(uri);
        if (fileName == null) {
            Toast.makeText(this, "Invalid file", Toast.LENGTH_SHORT).show();
            return;
        }

        // 清理旧场景
        cleanupAllScenes();

        // 显示进度对话框
        runOnUiThread(() -> {
            progressDialog.setMessage("Processing scene pack...");
            progressDialog.setProgress(0);
            progressDialog.show();
        });

        // 创建新的场景目录
        currentSceneDir = new File(sceneCacheDir, System.currentTimeMillis() + "_" + fileName.replace(".zip", ""));
        if (!currentSceneDir.exists()) {
            currentSceneDir.mkdirs();
        }

        // 在后台线程中处理文件
        new Thread(() -> {
            try {
                // 复制文件到临时目录
                File tempZip = new File(sceneCacheDir, "temp_" + fileName);
                updateProgress("Copying file...", 20);
                copyFileToInternal(uri, tempZip);

                // 验证文件完整性
                updateProgress("Verifying file...", 40);
                if (!verifyZipFile(tempZip)) {
                    throw new IOException("Invalid or corrupted zip file");
                }

                // 解压文件
                updateProgress("Extracting files...", 60);
                unzipFile(tempZip, currentSceneDir);

                // 删除临时zip文件
                tempZip.delete();

                // 验证场景配置
                updateProgress("Validating scene configuration...", 80);
                File sceneConfig = new File(currentSceneDir, "scene.json");
                if (!sceneConfig.exists() || !verifySceneConfig(sceneConfig)) {
                    throw new IOException("Invalid scene configuration");
                }

                // 完成
                updateProgress("Scene pack loaded successfully!", 100);
                Thread.sleep(500); // 短暂显示完成消息

                // 成功处理完成
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Scene pack loaded successfully", Toast.LENGTH_SHORT).show();
                    // TODO: 初始化场景
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this,
                            "Failed to process scene pack: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void copyFileToInternal(Uri sourceUri, File destFile) throws IOException {
        try (InputStream is = getContentResolver().openInputStream(sourceUri);
             OutputStream os = new FileOutputStream(destFile)) {

            if (is == null) throw new IOException("Failed to open input stream");

            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        }
    }

    private void unzipFile(File zipFile, File targetDirectory) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry;
            byte[] buffer = new byte[8192];

            while ((zipEntry = zis.getNextEntry()) != null) {
                File newFile = new File(targetDirectory, zipEntry.getName());

                // 安全检查：确保解压的文件路径在目标目录内
                String canonicalDestinationPath = targetDirectory.getCanonicalPath();
                String canonicalNewFilePath = newFile.getCanonicalPath();
                if (!canonicalNewFilePath.startsWith(canonicalDestinationPath)) {
                    throw new SecurityException("Zip slip vulnerability detected");
                }

                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    // 创建父目录
                    new File(newFile.getParent()).mkdirs();

                    // 写入文件
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private void updateProgress(String message, int progress) {
        runOnUiThread(() -> {
            progressDialog.setMessage(message);
            progressDialog.setProgress(progress);
        });
    }

    private void cleanupAllScenes() {
        if (sceneCacheDir.exists()) {
            File[] files = sceneCacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteRecursively(file);
                }
            }
        }
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    private boolean verifyZipFile(File zipFile) {
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                // 跳过目录条目
                if (entry.isDirectory()) {
                    continue;
                }

                // 只验证文件条目
                try (InputStream is = zip.getInputStream(entry)) {
                    byte[] buffer = new byte[1024];
                    int read = is.read(buffer);
                    if (read == -1) {
                        Log.e("Zip", "Failed to read zip entry: " + entry.getName());
                        return false;
                    }
                }
            }

            // 验证必要的目录结构是否存在
            boolean hasBgmDir = false;
            boolean hasSceneBackgroundDir = false;
            boolean hasCharacterDir = false;
            boolean hasEffectsJsonDir = false;
            boolean hasEffectsSoundDir = false;
            boolean hasSceneConfig = false;

            entries = zip.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith("bgm/")) hasBgmDir = true;
                if (name.startsWith("scene_background/")) hasSceneBackgroundDir = true;
                if (name.startsWith("character/")) hasCharacterDir = true;
                if (name.startsWith("effects_json/")) hasEffectsJsonDir = true;
                if (name.startsWith("effects_sound/")) hasEffectsSoundDir = true;
                if (name.equals("scene.json")) hasSceneConfig = true;
            }

            // 检查必要的目录和文件是否都存在
            if (!hasSceneConfig) {
                Log.e("Zip", "Missing scene.json");
                return false;
            }
            if (!hasBgmDir || !hasSceneBackgroundDir || !hasCharacterDir ||
                    !hasEffectsJsonDir || !hasEffectsSoundDir) {
                Log.e("Zip", "Missing required directories");
                return false;
            }

            return true;
        } catch (Exception e) {
            Log.e("Zip", "Error verifying zip file", e);
            return false;
        }
    }

    private boolean verifySceneConfig(File configFile) {
        try {
            JSONObject config = new JSONObject(new String(
                    Files.readAllBytes(configFile.toPath()),
                    StandardCharsets.UTF_8
            ));

            // 验证必要的字段
            return config.has("name") &&
                    config.has("version") &&
                    config.has("resources") &&
                    config.has("scene_config") &&
                    config.has("scenes");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 确保在Activity销毁时释放资源
        if (bgmPlayer != null) {
            bgmPlayer.release();
            bgmPlayer = null;
        }
        // 移除回调
        if (sceneRenderer != null) {
            sceneRenderer.setFPSCallback(null);
            sceneRenderer.setTimeCallback(null);
        }
        if (sceneRenderer != null) {
            sceneRenderer.release();
            sceneRenderer = null;
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        sceneRenderer.onSurfaceChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}