package kr.ac.waltdev29.hakplace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class TitleActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAuthAndNavigate();
            }
        }, SPLASH_DELAY);
    }

    private void checkAuthAndNavigate() {
        SharedPreferences prefs = getSharedPreferences("hakplace_prefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        boolean isAutoLogin = prefs.getBoolean("auto_login", false);

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
        SharedPreferences prefs = getSharedPreferences("hakplace_prefs", MODE_PRIVATE);
        prefs.edit().remove("access_token").remove("auto_login").apply();
    }
}
