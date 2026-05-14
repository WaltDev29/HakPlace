package kr.ac.waltdev29.hakplace;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import kr.ac.waltdev29.hakplace.api.ApiClient;
import kr.ac.waltdev29.hakplace.api.ApiService;
import kr.ac.waltdev29.hakplace.api.ErrorUtils;
import kr.ac.waltdev29.hakplace.api.models.ReviewCreate;
import kr.ac.waltdev29.hakplace.api.models.ReviewResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewWriteActivity extends AppCompatActivity {

    private int mealId;
    private String mealType;
    private String date;
    
    private TextView tvMealInfo, tvRatingValue, tvCharCount;
    private EditText etReviewComment;
    private LinearLayout llStars;
    private View btnSubmit, btnClose;
    
    private int currentRating = 0;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_write);

        mealId = getIntent().getIntExtra("meal_id", -1);
        mealType = getIntent().getStringExtra("meal_type");
        date = getIntent().getStringExtra("date");

        if (mealId == -1) {
            Toast.makeText(this, getString(R.string.msg_wrong_access), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvMealInfo = findViewById(R.id.tvMealInfo);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        tvCharCount = findViewById(R.id.tvCharCount);
        etReviewComment = findViewById(R.id.etReviewComment);
        llStars = findViewById(R.id.llStars);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnClose = findViewById(R.id.btnClose);

        tvMealInfo.setText(mealType + " " + date);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> finish());

        // Star click listeners
        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            llStars.getChildAt(i).setOnClickListener(v -> setRating(rating));
        }

        // Character count listener
        etReviewComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCharCount.setText(s.length() + "/200");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void setRating(int rating) {
        currentRating = rating;
        for (int i = 0; i < 5; i++) {
            ImageView star = (ImageView) llStars.getChildAt(i);
            if (i < rating) {
                star.setImageResource(R.drawable.ic_star);
            } else {
                star.setImageResource(R.drawable.ic_star_border);
            }
        }
        tvRatingValue.setText(String.format(Locale.getDefault(), "%.1f / 5.0", (double) rating));
    }

    private void submitReview() {
        if (currentRating == 0) {
            Toast.makeText(this, getString(R.string.msg_select_rating), Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = etReviewComment.getText().toString().trim();
        
        ReviewCreate review = new ReviewCreate();
        review.meal_id = mealId;
        review.rating = (double) currentRating;
        if (!comment.isEmpty()) {
            review.review_comment = comment;
        }

        SharedPreferences prefs = getSharedPreferences("hakplace_prefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        if (token == null) {
            Toast.makeText(this, getString(R.string.msg_login_required), Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        apiService.createReview("Bearer " + token, review).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                btnSubmit.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ReviewWriteActivity.this, getString(R.string.msg_review_registered), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String error = ErrorUtils.parseError(response.errorBody());
                    Toast.makeText(ReviewWriteActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                btnSubmit.setEnabled(true);
                Toast.makeText(ReviewWriteActivity.this, getString(R.string.msg_network_error) + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
