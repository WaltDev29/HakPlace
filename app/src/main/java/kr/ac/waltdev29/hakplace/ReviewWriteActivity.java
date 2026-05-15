package kr.ac.waltdev29.hakplace;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;

import kr.ac.waltdev29.hakplace.api.ApiClient;
import kr.ac.waltdev29.hakplace.api.ApiService;
import kr.ac.waltdev29.hakplace.api.ErrorUtils;
import kr.ac.waltdev29.hakplace.api.models.ReviewCreate;
import kr.ac.waltdev29.hakplace.api.models.ReviewResponse;
import kr.ac.waltdev29.hakplace.utils.DialogHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewWriteActivity extends AppCompatActivity {

    private int mealId;
    private String mealType;
    private String date;
    
    private TextView tvMealInfo, tvRatingValue, tvCharCount;
    private EditText etReviewComment;
    private RatingBar ratingBar;
    private View btnSubmit, btnClose;
    private View btnUploadPhoto;
    private ImageView ivSelectedPhoto, icCamera;
    
    private float currentRating = 1.0f;
    private String photoBase64 = null;
    private ApiService apiService;
    
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    processSelectedImage(uri);
                }
            });

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
        ratingBar = findViewById(R.id.ratingBar);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnClose = findViewById(R.id.btnClose);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        ivSelectedPhoto = findViewById(R.id.ivSelectedPhoto);
        icCamera = findViewById(R.id.icCamera);

        tvMealInfo.setText(mealType + " " + date);
        tvRatingValue.setText("1.0 / 5.0");
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> finish());
        
        btnUploadPhoto.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        // Rating bar change listener
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser) {
                if (rating < 1.0f) {
                    bar.setRating(1.0f);
                    setRating(1.0f);
                } else {
                    setRating(rating);
                }
            }
        });

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

        btnSubmit.setOnClickListener(v -> {
            DialogHelper.showConfirmDialog(this, getString(R.string.submit_review), getString(R.string.msg_confirm_review_submit), new DialogHelper.OnDialogListener() {
                @Override
                public void onConfirm() {
                    submitReview();
                }

                @Override
                public void onCancel() {
                    // Do nothing
                }
            });
        });
    }

    private void setRating(float rating) {
        currentRating = rating;
        tvRatingValue.setText(String.format(Locale.getDefault(), "%.1f / 5.0", rating));
    }

    private void submitReview() {
        if (currentRating < 1.0f) {
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
        if (photoBase64 != null) {
            review.photo_base64 = photoBase64;
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

    private void processSelectedImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (bitmap != null) {
                // Resize for performance (max 1024px)
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                float scale = Math.min(1024f / width, 1024f / height);
                if (scale < 1.0f) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(width * scale), Math.round(height * scale), true);
                }

                // Show preview
                ivSelectedPhoto.setImageBitmap(bitmap);
                ivSelectedPhoto.setVisibility(View.VISIBLE);
                icCamera.setVisibility(View.GONE);

                // Convert to Base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // 70% quality
                byte[] bytes = baos.toByteArray();
                photoBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
            }
        } catch (Exception e) {
            Toast.makeText(this, "이미지를 처리하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
