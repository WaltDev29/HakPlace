package kr.ac.waltdev29.hakplace.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * 기기의 절전 모드(Power Save Mode) 또는 다크 모드 상태를 감지하여
 * 상태표시줄(Status Bar) 배경색과 아이콘/텍스트 색상을 설정하는 헬퍼 클래스.
 */
public class StatusBarHelper {

    private static final int DARK_STATUS_BAR_COLOR = Color.parseColor("#121212");
    private static final int LIGHT_STATUS_BAR_COLOR = Color.WHITE;

    public static void applyStatusBarColor(@NonNull Activity activity) {
        Window window = activity.getWindow();
        if (window == null) return;

        boolean isPowerSave = false;
        try {
            PowerManager powerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                isPowerSave = powerManager.isPowerSaveMode();
            }
        } catch (Exception ignored) {
        }

        int nightModeFlags = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isNightMode = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES);

        // 절전 모드이거나 다크 모드일 때 어두운 상태표시줄 적용
        boolean isDark = isPowerSave || isNightMode;

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(isDark ? DARK_STATUS_BAR_COLOR : LIGHT_STATUS_BAR_COLOR);

        android.view.View decorView = window.getDecorView();
        if (decorView != null) {
            decorView.post(() -> {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                window.setStatusBarColor(isDark ? DARK_STATUS_BAR_COLOR : LIGHT_STATUS_BAR_COLOR);
                WindowInsetsControllerCompat insetsController =
                        WindowCompat.getInsetsController(window, decorView);
                if (insetsController != null) {
                    insetsController.setAppearanceLightStatusBars(!isDark);
                }
            });
        }
    }
}
