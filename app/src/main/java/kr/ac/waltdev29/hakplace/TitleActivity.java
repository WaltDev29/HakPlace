package kr.ac.waltdev29.hakplace;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import kr.ac.waltdev29.hakplace.utils.MealNotificationScheduler;
import kr.ac.waltdev29.hakplace.utils.NotificationHelper;
import kr.ac.waltdev29.hakplace.utils.TokenManager;

public class TitleActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1000;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                checkAuthAndNavigate();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        // 알림 채널 생성 및 기본 식단 알림 스케줄러(08:00, 11:30, 17:30) 등록
        NotificationHelper.createNotificationChannel(this);
        MealNotificationScheduler.scheduleAll(this);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(TitleActivity.this, Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    checkAuthAndNavigate();
                }
            }
        }, SPLASH_DELAY);
    }

    private void checkAuthAndNavigate() {
        TokenManager tokenManager = TokenManager.getInstance(this);
        String token = tokenManager.getAccessToken();
        boolean isAutoLogin = tokenManager.isAutoLogin();

        if (token != null && !token.isEmpty() && isAutoLogin) {
            // Validate token
            kr.ac.waltdev29.hakplace.api.ApiService apiService = kr.ac.waltdev29.hakplace.api.ApiClient.getClient()
                    .create(kr.ac.waltdev29.hakplace.api.ApiService.class);
            apiService.getMe("Bearer " + token)
                    .enqueue(new retrofit2.Callback<kr.ac.waltdev29.hakplace.api.models.UserInfo>() {
                        @Override
                        public void onResponse(retrofit2.Call<kr.ac.waltdev29.hakplace.api.models.UserInfo> call,
                                retrofit2.Response<kr.ac.waltdev29.hakplace.api.models.UserInfo> response) {
                            if (response.isSuccessful()) {
                                // Token is valid
                                startActivity(new Intent(TitleActivity.this, MenuTodayActivity.class));
                            } else {
                                // Token is invalid or expired
                                clearToken();
                                startActivity(new Intent(TitleActivity.this, LoginActivity.class));
                            }
                            finish();
                        }

                        @Override
                        public void onFailure(retrofit2.Call<kr.ac.waltdev29.hakplace.api.models.UserInfo> call,
                                Throwable t) {
                            // Network error - stay on splash or go to login?
                            // Usually, if offline, we might want to allow offline mode, but for simplicity,
                            // go to login.
                            startActivity(new Intent(TitleActivity.this, LoginActivity.class));
                            finish();
                        }
                    });
        } else {
            startActivity(new Intent(TitleActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void clearToken() {
        TokenManager.getInstance(this).clearAuth();
    }
}
