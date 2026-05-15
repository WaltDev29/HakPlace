package kr.ac.waltdev29.hakplace;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.util.Pair;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import kr.ac.waltdev29.hakplace.api.ApiClient;
import kr.ac.waltdev29.hakplace.api.ApiService;
import com.google.android.material.datepicker.MaterialDatePicker;
import kr.ac.waltdev29.hakplace.api.models.DailyMeals;
import kr.ac.waltdev29.hakplace.api.models.ReviewList;
import kr.ac.waltdev29.hakplace.api.models.ReviewResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewListActivity extends AppCompatActivity {

    private RecyclerView rvReviews;
    private ReviewAdapter adapter;
    private ApiService apiService;
    private android.widget.Button btnSort;
    private android.widget.Button btnDateRange;
    private android.widget.Button btnAllPeriod;
    private ChipGroup chipGroupMealType;

    private String currentSort = "newest";
    private Integer currentMealId = null; // Used for Intent-based specific meal filtering
    private String currentMealTypeFilter = null; // Used for chip filtering (조식, 중식, 석식)
    private boolean isMyReviewsMode = false;
    private String studentIdFilter = null;
    private android.widget.ImageView ivHeaderIcon;
    private android.widget.TextView tvHeaderTitle;
    private android.widget.ImageButton btnBack;
    private String currentStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    private String currentEndDate = currentStartDate;
    private Calendar selectedDate = Calendar.getInstance(); // Default to today

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);

        apiService = ApiClient.getClient().create(ApiService.class);

        rvReviews = findViewById(R.id.rvReviews);
        btnSort = findViewById(R.id.btnSort);
        btnDateRange = findViewById(R.id.btnDateRange);
        btnAllPeriod = findViewById(R.id.btnAllPeriod);
        chipGroupMealType = findViewById(R.id.chipGroupMealType);
        ivHeaderIcon = findViewById(R.id.ivHeaderIcon);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        btnBack = findViewById(R.id.btnBack);

        adapter = new ReviewAdapter();
        rvReviews.setAdapter(adapter);

        // Intent handle
        if (getIntent().hasExtra("meal_id")) {
            currentMealId = getIntent().getIntExtra("meal_id", -1);
            if (currentMealId == -1) {
                currentMealId = null;
            } else {
                currentStartDate = null;
                currentEndDate = null;
            }
        }

        if (getIntent().hasExtra("start_date")) {
            currentStartDate = getIntent().getStringExtra("start_date");
        }
        if (getIntent().hasExtra("end_date")) {
            currentEndDate = getIntent().getStringExtra("end_date");
        }
        if (getIntent().hasExtra("meal_type")) {
            currentMealTypeFilter = getIntent().getStringExtra("meal_type");
        }

        if (getIntent().getBooleanExtra("is_my_reviews", false)) {
            isMyReviewsMode = true;
            studentIdFilter = getIntent().getStringExtra("student_id");
            setupMyReviewsUI();
            currentStartDate = null;
            currentEndDate = null;
        }

        updateDateDisplay();
        setupFilters();
        setupBottomNav();

        // Initial check for meal type chips based on intent
        if (currentMealTypeFilter != null) {
            if ("조식".equals(currentMealTypeFilter))
                chipGroupMealType.check(R.id.chipBreakfast);
            else if ("중식".equals(currentMealTypeFilter))
                chipGroupMealType.check(R.id.chipLunch);
            else if ("석식".equals(currentMealTypeFilter))
                chipGroupMealType.check(R.id.chipDinner);
        }

        // Initial fetch: all reviews for today or filtered range
        fetchReviews();
    }

    private List<ReviewResponse> allReviewsFromApi = new ArrayList<>();

    private void setupFilters() {
        btnSort.setOnClickListener(v -> showSortMenu());

        btnDateRange.setOnClickListener(v -> {
            MaterialDatePicker<androidx.core.util.Pair<Long, Long>> dateRangePicker = MaterialDatePicker.Builder
                    .dateRangePicker()
                    .setTitleText(getString(R.string.period_select))
                    .setTheme(R.style.CustomMaterialCalendar)
                    .build();

            dateRangePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdfApi = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat sdfDisplay = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

                Calendar startCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                startCal.setTimeInMillis(selection.first);
                Calendar endCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                endCal.setTimeInMillis(selection.second);

                currentStartDate = sdfApi.format(startCal.getTime());
                currentEndDate = sdfApi.format(endCal.getTime());

                btnDateRange
                        .setText(sdfDisplay.format(startCal.getTime()) + " ~ " + sdfDisplay.format(endCal.getTime()));

                // Reset meal type filter when date range changes
                chipGroupMealType.check(R.id.chipAll);
                currentMealTypeFilter = null;
                currentMealId = null;

                fetchReviews();
            });

            dateRangePicker.show(getSupportFragmentManager(), "DATE_RANGE_PICKER");
        });

        btnAllPeriod.setOnClickListener(v -> {
            currentStartDate = null;
            currentEndDate = null;
            btnDateRange.setText(getString(R.string.all_period_select));

            // Reset meal type selection
            chipGroupMealType.check(R.id.chipAll);
            currentMealTypeFilter = null;
            currentMealId = null;

            fetchReviews();
        });

        chipGroupMealType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            updateMealTypeFilter();
        });
    }

    private void showSortMenu() {
        android.view.ContextThemeWrapper contextWrapper = new android.view.ContextThemeWrapper(this,
                R.style.SortPopupMenuStyle);
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(contextWrapper, btnSort);
        popup.getMenu().add(0, 0, 0, getString(R.string.sort_newest));
        popup.getMenu().add(0, 1, 1, getString(R.string.sort_rating_high));
        popup.getMenu().add(0, 2, 2, getString(R.string.sort_rating_low));

        popup.setOnMenuItemClickListener(item -> {
            btnSort.setText(item.getTitle());
            switch (item.getItemId()) {
                case 0:
                    currentSort = "newest";
                    break;
                case 1:
                    currentSort = "rating_desc";
                    break;
                case 2:
                    currentSort = "rating_asc";
                    break;
            }
            updateDisplayList();
            return true;
        });
        popup.show();
    }

    private void updateDateDisplay() {
        if (currentStartDate == null || currentEndDate == null) {
            btnDateRange.setText(getString(R.string.all_period_select));
        } else {
            try {
                SimpleDateFormat sdfApi = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat sdfDisplay = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                Date start = sdfApi.parse(currentStartDate);
                Date end = sdfApi.parse(currentEndDate);
                btnDateRange.setText(sdfDisplay.format(start) + " ~ " + sdfDisplay.format(end));
            } catch (Exception e) {
                btnDateRange.setText(currentStartDate + " ~ " + currentEndDate);
            }
        }
    }

    private void updateMealTypeFilter() {
        int checkedId = chipGroupMealType.getCheckedChipId();
        if (checkedId == R.id.chipAll) {
            currentMealTypeFilter = null;
        } else if (checkedId == R.id.chipBreakfast) {
            currentMealTypeFilter = "조식";
        } else if (checkedId == R.id.chipLunch) {
            currentMealTypeFilter = "중식";
        } else if (checkedId == R.id.chipDinner) {
            currentMealTypeFilter = "석식";
        }

        // Update display list from cached data
        updateDisplayList();
    }

    private void setupMyReviewsUI() {
        tvHeaderTitle.setText(getString(R.string.my_reviews));
        ivHeaderIcon.setImageResource(R.drawable.ic_edit_document);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.bottomNav).setVisibility(View.GONE);
    }

    private void fetchReviews() {
        // Fetch ALL reviews for the date range, then filter locally by meal type
        apiService.listReviews(null, studentIdFilter, "newest", currentStartDate, currentEndDate)
                .enqueue(new Callback<ReviewList>() {
                    @Override
                    public void onResponse(Call<ReviewList> call, Response<ReviewList> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            allReviewsFromApi = response.body().reviews;
                            updateDisplayList();
                        } else {
                            String errorMsg = getString(R.string.error_review_load);
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg += " (" + response.code() + ": " + response.errorBody().string() + ")";
                                }
                            } catch (Exception ignored) {
                            }
                            Toast.makeText(ReviewListActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            android.util.Log.e("ReviewList",
                                    "API Error: " + response.code() + " " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ReviewList> call, Throwable t) {
                        Toast.makeText(ReviewListActivity.this,
                                getString(R.string.msg_network_error) + ": " + t.getMessage(), Toast.LENGTH_LONG)
                                .show();
                        android.util.Log.e("ReviewList", "Network Failure", t);
                    }
                });
    }

    private void updateDisplayList() {
        List<ReviewResponse> displayList = new ArrayList<>();

        // 1. Filtering by Meal Type (Cross-date range supported!)
        for (ReviewResponse r : allReviewsFromApi) {
            if (currentMealTypeFilter == null || currentMealTypeFilter.equals(r.meal_type)) {
                // Also check if currentMealId was set (e.g. from Intent)
                if (currentMealId == null || r.meal_id == currentMealId.intValue()) {
                    displayList.add(r);
                }
            }
        }

        // 2. Sorting
        Collections.sort(displayList, (r1, r2) -> {
            if ("rating_desc".equals(currentSort)) {
                return Double.compare(r2.rating, r1.rating);
            } else if ("rating_asc".equals(currentSort)) {
                return Double.compare(r1.rating, r2.rating);
            } else {
                // newest: compare created_at strings (desc)
                return r2.created_at.compareTo(r1.created_at);
            }
        });

        adapter.setReviews(displayList);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_review);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_review) {
                return true;
            } else if (id == R.id.nav_today) {
                startActivity(new Intent(this, MenuTodayActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_weekly) {
                startActivity(new Intent(this, MenuWeekActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_mypage) {
                startActivity(new Intent(this, MyPageActivity.class));
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
