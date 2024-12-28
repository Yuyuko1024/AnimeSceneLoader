package net.hearnsoft.animesceneloader.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import androidx.annotation.NonNull;

import net.hearnsoft.animesceneloader.databinding.DialogLoadingBinding;

public class LoadingDialog extends Dialog {
    private DialogLoadingBinding binding;

    public LoadingDialog(@NonNull Context context) {
        super(context);

        // 请求无标题窗口
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 初始化绑定
        binding = DialogLoadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置对话框属性
        setCancelable(false);

        // 初始化进度条
        binding.loadingProgress.setMax(100);
        binding.loadingProgress.setProgress(0);
    }

    @Override
    public void show() {
        super.show();
    }

    public void setProgress(int progress) {
        if (binding != null) {
            if (progress >= 100) {
                binding.loadingProgress.setIndeterminate(true);
            } else {
                binding.loadingProgress.setIndeterminate(false);
                binding.loadingProgress.setProgress(progress);
            }
        }
    }

    public void setMessage(String message) {
        if (binding != null && binding.loadingMessage != null) {
            binding.loadingMessage.setText(message);
        }
    }
}
