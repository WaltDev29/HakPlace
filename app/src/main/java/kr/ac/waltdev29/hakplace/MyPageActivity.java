package kr.ac.waltdev29.hakplace;

import android.content.Intent;
import android.content.SharedPreferences;
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
import retrofit2.Response;

public class MyPageActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserDetail;
    private View btnMyReviews, btnFaq, btnLogout;
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        apiService = ApiClient.getClient().create(ApiService.class);

        SharedPreferences prefs = getSharedPreferences("hakplace_prefs", MODE_PRIVATE);
        token = prefs.getString("access_token", null);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserDetail = findViewById(R.id.tvUserDetail);
        btnMyReviews = findViewById(R.id.btnMyReviews);
        btnFaq = findViewById(R.id.btnFaq);
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
            Toast.makeText(this, "준비 중인 기능입니다.", Toast.LENGTH_SHORT).show();
        });

        btnFaq.setOnClickListener(v -> {
            Toast.makeText(this, "준비 중인 기능입니다.", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            DialogHelper.showConfirmDialog(this, "로그아웃", "정말 로그아웃 하시겠습니까?", new DialogHelper.OnDialogListener() {
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

    private void fetchUserProfile() {
        apiService.getMe("Bearer " + token).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserInfo userInfo = response.body();
                    tvUserName.setText(userInfo.name);
                    tvUserDetail.setText(userInfo.student_id);
                } else {
                    Toast.makeText(MyPageActivity.this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Toast.makeText(MyPageActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("hakplace_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("access_token");
        editor.putBoolean("auto_login", false);
        editor.apply();

        Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "준비 중인 기능입니다.", Toast.LENGTH_SHORT).show();
                return false;
            }
            return false;
        });
    }
}
