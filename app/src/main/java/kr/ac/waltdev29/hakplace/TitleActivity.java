package kr.ac.waltdev29.hakplace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;


public class TitleActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;
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
            startActivity(new Intent(TitleActivity.this, MenuTodayActivity.class));

        } else {

            startActivity(new Intent(TitleActivity.this, LoginActivity.class));
        }

        finish();
    }
}
