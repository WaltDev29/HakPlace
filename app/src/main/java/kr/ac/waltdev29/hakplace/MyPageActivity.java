package kr.ac.waltdev29.hakplace;

import android.content.Intent;
import kr.ac.waltdev29.hakplace.utils.TokenManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import kr.ac.waltdev29.hakplace.utils.DialogHelper;

import kr.ac.waltdev29.hakplace.api.ApiClient;
import kr.ac.waltdev29.hakplace.api.ApiService;
import kr.ac.waltdev29.hakplace.api.models.UserInfo;
import retrofit2.Call;
import retrofit2.Callback;
import kr.ac.waltdev29.hakplace.utils.NotificationHelper;
import kr.ac.waltdev29.hakplace.utils.MealNotificationScheduler;
import retrofit2.Response;

public class MyPageActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserDetail;
    private View btnMyReviews, btnNotificationSettings, btnAppInfo, btnLogout;
    private ApiService apiService;
    private String token;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        apiService = ApiClient.getClient().create(ApiService.class);

        token = TokenManager.getInstance(this).getAccessToken();

        tvUserName = findViewById(R.id.tvUserName);
        tvUserDetail = findViewById(R.id.tvUserDetail);
        btnMyReviews = findViewById(R.id.btnMyReviews);
        btnNotificationSettings = findViewById(R.id.btnNotificationSettings);
        btnAppInfo = findViewById(R.id.btnAppInfo);
        btnLogout = findViewById(R.id.btnLogout);

        setupBottomNav();
        setupClickListeners();

        if (token != null) {
            fetchUserProfile();
        } else {
            // Not logged in, go to Login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void setupClickListeners() {
        btnMyReviews.setOnClickListener(v -> {
            if (studentId == null) {
                Toast.makeText(this, getString(R.string.user_info_loading), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ReviewListActivity.class);
            intent.putExtra("is_my_reviews", true);
            intent.putExtra("student_id", studentId);
            startActivity(intent);
        });

        btnNotificationSettings.setOnClickListener(v -> showNotificationSettingsDialog());

        btnAppInfo.setOnClickListener(v -> {
            DialogHelper.showNotificationDialog(
                    this,
                    R.drawable.ic_help,
                    getString(R.string.mypage_app_info),
                    getString(R.string.mypage_app_info_msg, BuildConfig.VERSION_NAME),
                    null
            );
        });

        btnLogout.setOnClickListener(v -> {
            DialogHelper.showConfirmDialog(this, getString(R.string.logout), getString(R.string.logout_confirm_msg), new DialogHelper.OnDialogListener() {
                @Override
                public void onConfirm() {
                    logout();
                }

                @Override
                public void onCancel() {
                    // Do nothing
                }
            });
        });
    }

    private void showNotificationSettingsDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_notification_settings);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        View layoutSystemAlert = dialog.findViewById(R.id.layoutSystemAlert);
        View btnGoSystemSettings = dialog.findViewById(R.id.btnGoSystemSettings);
        androidx.appcompat.widget.SwitchCompat switchDailyAll = dialog.findViewById(R.id.switchDailyAll);
        androidx.appcompat.widget.SwitchCompat switchLunch = dialog.findViewById(R.id.switchLunch);
        androidx.appcompat.widget.SwitchCompat switchDinner = dialog.findViewById(R.id.switchDinner);
        View btnCloseSettings = dialog.findViewById(R.id.btnCloseSettings);

        // 시스템 알림 활성화 여부 확인
        boolean isSystemEnabled = NotificationHelper.areNotificationsEnabled(this);
        layoutSystemAlert.setVisibility(isSystemEnabled ? View.GONE : View.VISIBLE);

        btnGoSystemSettings.setOnClickListener(v -> {
            NotificationHelper.openNotificationSettings(this);
        });

        // 현재 저장된 스케줄 설정값 반영
        switchDailyAll.setChecked(MealNotificationScheduler.isDailyAllEnabled(this));
        switchLunch.setChecked(MealNotificationScheduler.isLunchEnabled(this));
        switchDinner.setChecked(MealNotificationScheduler.isDinnerEnabled(this));

        // 토글 변경 시 시스템 권한 체크 및 스케줄러 업데이트
        switchDailyAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !NotificationHelper.areNotificationsEnabled(this)) {
                buttonView.setChecked(false);
                promptEnableSystemNotification();
                return;
            }
            MealNotificationScheduler.setDailyAllEnabled(this, isChecked);
        });

        switchLunch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !NotificationHelper.areNotificationsEnabled(this)) {
                buttonView.setChecked(false);
                promptEnableSystemNotification();
                return;
            }
            MealNotificationScheduler.setLunchEnabled(this, isChecked);
        });

        switchDinner.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !NotificationHelper.areNotificationsEnabled(this)) {
                buttonView.setChecked(false);
                promptEnableSystemNotification();
                return;
            }
            MealNotificationScheduler.setDinnerEnabled(this, isChecked);
        });

        btnCloseSettings.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void promptEnableSystemNotification() {
        DialogHelper.showConfirmDialog(
                this,
                getString(R.string.notification_permission_required_title),
                getString(R.string.notification_permission_required_msg),
                new DialogHelper.OnDialogListener() {
                    @Override
                    public void onConfirm() {
                        NotificationHelper.openNotificationSettings(MyPageActivity.this);
                    }

                    @Override
                    public void onCancel() {
                    }
                }
        );
    }

    private void fetchUserProfile() {
        apiService.getMe("Bearer " + token).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserInfo userInfo = response.body();
                    studentId = userInfo.student_id;
                    tvUserName.setText(userInfo.name);
                    tvUserDetail.setText(studentId);
                } else {
                    Toast.makeText(MyPageActivity.this, getString(R.string.user_info_error), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Toast.makeText(MyPageActivity.this, getString(R.string.msg_network_error) + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        TokenManager.getInstance(this).clearAll();

        Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_mypage);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_mypage) {
                return true;
            } else if (id == R.id.nav_today) {
                startActivity(new Intent(this, MenuTodayActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_weekly) {
                startActivity(new Intent(this, MenuWeekActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_review) {
                startActivity(new Intent(this, ReviewListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_stats) {
                startActivity(new Intent(this, StatisticsActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}
