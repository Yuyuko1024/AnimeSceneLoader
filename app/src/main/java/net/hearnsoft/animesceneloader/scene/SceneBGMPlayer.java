package net.hearnsoft.animesceneloader.scene;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

public class SceneBGMPlayer {
    private static final String TAG = "SceneBGMPlayer";

    private Context context;
    private MediaPlayer mediaPlayer;
    private String filePath;
    private Uri fileUri;
    private float volume = 1.0f;
    private boolean isLooping = true;

    // 无参构造函数
    public SceneBGMPlayer(Context context) {
        this.context = context;
        initMediaPlayer();
    }

    // 文件路径构造函数
    public SceneBGMPlayer(Context context, String filePath) {
        this.context = context;
        this.filePath = filePath;
        initMediaPlayer();
        setDataSource();
    }

    // Uri构造函数
    public SceneBGMPlayer(Context context, Uri uri) {
        this.context = context;
        this.fileUri = uri;
        initMediaPlayer();
        setDataSource();
    }

    private void initMediaPlayer() {
        if (mediaPlayer != null) {
            release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(isLooping);
        mediaPlayer.setVolume(volume, volume);

        // 设置错误监听器
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
            return false;
        });
    }

    private void setDataSource() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                if (fileUri != null) {
                    mediaPlayer.setDataSource(context, fileUri);
                } else if (filePath != null) {
                    mediaPlayer.setDataSource(filePath);
                }
                mediaPlayer.prepare();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error setting data source", e);
        }
    }

    // 设置音频文件（String路径）
    public void setAudioFile(String filePath) {
        this.filePath = filePath;
        this.fileUri = null;
        setDataSource();
    }

    // 设置音频文件（Uri）
    public void setAudioFile(Uri uri) {
        this.fileUri = uri;
        this.filePath = null;
        setDataSource();
    }

    // 设置音量
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(this.volume, this.volume);
        }
    }

    // 设置循环播放
    public void setLooping(boolean looping) {
        this.isLooping = looping;
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(looping);
        }
    }

    // 播放
    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.start();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error playing audio", e);
            }
        }
    }

    // 暂停
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    // 停止
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                Log.e(TAG, "Error preparing media player after stop", e);
            }
        }
    }

    // 释放资源
    public void release() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // 获取当前播放位置
    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    // 获取音频总时长
    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    // 是否正在播放
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
}
