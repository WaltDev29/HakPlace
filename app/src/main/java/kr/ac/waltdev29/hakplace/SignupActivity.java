package kr.ac.waltdev29.hakplace;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import kr.ac.waltdev29.hakplace.api.ApiClient;
import kr.ac.waltdev29.hakplace.api.ApiService;
import kr.ac.waltdev29.hakplace.api.models.UserSignup;
import com.google.android.material.datepicker.MaterialDatePicker;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import kr.ac.waltdev29.hakplace.utils.DialogHelper;

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etStudentId, etBirthDate, etPhone, etPassword, etConfirmPassword;
    private android.widget.RadioGroup rgGender;
    private Button btnSignup, btnCancel;
    private ImageButton btnBack;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        apiService = ApiClient.getClient().create(ApiService.class);

        etName = findViewById(R.id.etName);
        etStudentId = findViewById(R.id.etStudentId);
        etBirthDate = findViewById(R.id.etBirthDate);
        etPhone = findViewById(R.id.etPhone);
        rgGender = findViewById(R.id.rgGender);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);

        etBirthDate.setOnClickListener(v -> showDatePicker());

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        btnSignup.setOnClickListener(v -> attemptSignup());
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("생년월일 선택")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setTheme(R.style.CustomMaterialCalendar)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));
            etBirthDate.setText(date);
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void attemptSignup() {
        String name = etName.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        int checkedGenderId = rgGender.getCheckedRadioButtonId();
        String gender = checkedGenderId == R.id.rbMale ? "M" : (checkedGenderId == R.id.rbFemale ? "F" : "");

        if (name.isEmpty() || studentId.isEmpty() || birthDate.isEmpty() || phone.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty() || gender.isEmpty()) {
            DialogHelper.showNotificationDialog(this, "입력 오류", "모든 정보를 입력해 주세요.", null);
            return;
        }

        if (studentId.length() != 10) {
            DialogHelper.showNotificationDialog(this, "입력 오류", "학번 10자리를 정확히 입력해 주세요.", null);
            return;
        }

        if (!password.equals(confirmPassword)) {
            DialogHelper.showNotificationDialog(this, "입력 오류", "비밀번호가 일치하지 않습니다.", null);
            return;
        }

        if (password.length() < 8) {
            DialogHelper.showNotificationDialog(this, "입력 오류", "비밀번호는 영문, 숫자 포함 8자 이상이어야 합니다.", null);
            return;
        }

        btnSignup.setEnabled(false);

        UserSignup signupData = new UserSignup();
        signupData.name = name;
        signupData.student_id = studentId;
        signupData.phone_number = phone;
        signupData.birth_date = birthDate;
        signupData.gender = gender;
        signupData.password = password;

        apiService.signup(signupData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnSignup.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, "회원가입 성공! 로그인해주세요.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "회원가입 실패: 이미 존재하는 학번이거나 잘못된 요청입니다.";
                    if (response.errorBody() != null) {
                        errorMsg = kr.ac.waltdev29.hakplace.api.ErrorUtils.parseError(response.errorBody());
                    }
                    DialogHelper.showNotificationDialog(SignupActivity.this, "가입 실패", errorMsg, null);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSignup.setEnabled(true);
                DialogHelper.showNotificationDialog(SignupActivity.this, "연결 실패", "서버 연결에 실패했습니다: " + t.getMessage(), null);
            }
        });

    }
}
