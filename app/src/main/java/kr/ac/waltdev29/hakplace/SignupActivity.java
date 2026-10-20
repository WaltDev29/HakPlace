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
        btnCancel = findViewById(R.id.btnSignupCancel);
        btnBack = findViewById(R.id.btnBack);

        etBirthDate.setOnClickListener(v -> showDatePicker());

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        btnSignup.setOnClickListener(v -> attemptSignup());
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.birth_date_label))
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
            DialogHelper.showNotificationDialog(this, getString(R.string.input_error), getString(R.string.validation_all_fields), null);
            return;
        }

        if (!studentId.matches("^\\d{10}$")) {
            DialogHelper.showNotificationDialog(this, getString(R.string.input_error), getString(R.string.validation_student_id), null);
            return;
        }

        if (!password.equals(confirmPassword)) {
            DialogHelper.showNotificationDialog(this, getString(R.string.input_error), getString(R.string.validation_password_mismatch), null);
            return;
        }

        // 영문과 숫자를 반드시 포함하여 8자 이상인지 정규식 검증
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";
        if (!password.matches(passwordPattern)) {
            DialogHelper.showNotificationDialog(this, getString(R.string.input_error), getString(R.string.validation_password), null);
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
                    Toast.makeText(SignupActivity.this, getString(R.string.signup_success), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = getString(R.string.signup_fail_exist);
                    if (response.errorBody() != null) {
                        errorMsg = kr.ac.waltdev29.hakplace.api.ErrorUtils.parseError(response.errorBody());
                    }
                    DialogHelper.showNotificationDialog(SignupActivity.this, getString(R.string.signup_fail), errorMsg, null);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSignup.setEnabled(true);
                DialogHelper.showNotificationDialog(SignupActivity.this, getString(R.string.connection_failed), String.format(getString(R.string.connection_failed_msg), t.getMessage()), null);
            }
        });

    }
}
