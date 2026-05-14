package kr.ac.waltdev29.hakplace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import kr.ac.waltdev29.hakplace.utils.DialogHelper;

import androidx.appcompat.app.AppCompatActivity;
import kr.ac.waltdev29.hakplace.api.ApiClient;
import kr.ac.waltdev29.hakplace.api.ApiService;
import kr.ac.waltdev29.hakplace.api.models.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etStudentId, etPassword;
    private CheckBox cbAutoLogin;
    private Button btnLogin, btnGoSignup;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = ApiClient.getClient().create(ApiService.class);

        etStudentId = findViewById(R.id.etStudentId);
        etPassword = findViewById(R.id.etPassword);
        cbAutoLogin = findViewById(R.id.cbAutoLogin);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoSignup = findViewById(R.id.btnGoSignup);
        ImageButton btnTogglePassword = findViewById(R.id.btnTogglePassword);

        btnLogin.setOnClickListener(v -> attemptLogin());

        btnTogglePassword.setOnClickListener(v -> {
            if (etPassword.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_visibility); // Placeholder: should be visibility_off
            } else {
                etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_visibility);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        btnGoSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

    }

    private void attemptLogin() {
        String studentId = etStudentId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (studentId.isEmpty() || password.isEmpty()) {
            DialogHelper.showNotificationDialog(this, "입력 오류", "학번과 비밀번호를 입력해주세요.", null);
            return;
        }

        btnLogin.setEnabled(false);
        apiService.login(studentId, password).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    boolean isAutoLogin = cbAutoLogin.isChecked();
                    saveToken(response.body().getAccessToken(), isAutoLogin);
                    
                    Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MenuTodayActivity.class));
                    finish();
                } else {
                    String errorMsg = "로그인 실패: 정보를 확인해주세요.";
                    if (response.errorBody() != null) {
                        errorMsg = kr.ac.waltdev29.hakplace.api.ErrorUtils.parseError(response.errorBody());
                    }
                    DialogHelper.showNotificationDialog(LoginActivity.this, "로그인 실패", errorMsg, null);
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                btnLogin.setEnabled(true);
                DialogHelper.showNotificationDialog(LoginActivity.this, "연결 실패", "서버 연결에 실패했습니다: " + t.getMessage(), null);
            }
        });
    }

    private void saveToken(String token, boolean autoLogin) {
        SharedPreferences prefs = getSharedPreferences("hakplace_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("access_token", token);
        editor.putBoolean("auto_login", autoLogin);
        editor.apply();
    }

}
