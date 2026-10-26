package kr.ac.waltdev29.hakplace;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;

import kr.ac.waltdev29.hakplace.utils.StatusBarHelper;

public class HakPlaceApplication extends Application {

    private WeakReference<Activity> currentActivityRef;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                StatusBarHelper.applyStatusBarColor(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                StatusBarHelper.applyStatusBarColor(activity);
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                currentActivityRef = new WeakReference<>(activity);
                StatusBarHelper.applyStatusBarColor(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                if (currentActivityRef != null && currentActivityRef.get() == activity) {
                    currentActivityRef = null;
                }
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {}
        });

        // 실시간 절전 모드 변경 이벤트(배터리 세이버 켜기/끄기) 감지
        try {
            IntentFilter filter = new IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (currentActivityRef != null) {
                        Activity currentActivity = currentActivityRef.get();
                        if (currentActivity != null && !currentActivity.isFinishing()) {
                            StatusBarHelper.applyStatusBarColor(currentActivity);
                        }
                    }
                }
            };
            ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        } catch (Exception ignored) {
        }
    }
}
