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
    private android.widget.Button btnDatePicker;
    private ChipGroup chipGroupMealType;

    private String currentSort = "newest";
    private Integer currentMealId = null;
    private Calendar selectedDate = Calendar.getInstance();
    private DailyMeals mealsOfDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);

        apiService = ApiClient.getClient().create(ApiService.class);

        rvReviews = findViewById(R.id.rvReviews);
        btnSort = findViewById(R.id.btnSort);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        chipGroupMealType = findViewById(R.id.chipGroupMealType);

        adapter = new ReviewAdapter();
        rvReviews.setAdapter(adapter);

        // Intent handle
        if (getIntent().hasExtra("meal_id")) {
            currentMealId = getIntent().getIntExtra("meal_id", -1);
            if (currentMealId == -1)
                currentMealId = null;
        }

        updateDateButton();
        setupFilters();
        setupBottomNav();

        // Initial fetch: all reviews
        fetchReviews();
        // Also fetch meals for the current date to know meal_ids
        fetchMealsOfDate();
    }

    private List<ReviewResponse> allReviewsFromApi = new ArrayList<>();

    private void setupFilters() {
        btnSort.setOnClickListener(v -> showSortMenu());

        btnDatePicker.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("날짜 선택")
                    .setSelection(selectedDate.getTimeInMillis())
                    .setTheme(R.style.CustomMaterialCalendar)
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                selectedDate.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                updateDateButton();
                fetchMealsOfDate();
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        chipGroupMealType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            updateCurrentMealIdByChip();
        });
    }

    private void showSortMenu() {
        android.view.ContextThemeWrapper contextWrapper = new android.view.ContextThemeWrapper(this, R.style.SortPopupMenuStyle);
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(contextWrapper, btnSort);
        popup.getMenu().add(0, 0, 0, "최신순");
        popup.getMenu().add(0, 1, 1, "평점 높은 순");
        popup.getMenu().add(0, 2, 2, "평점 낮은 순");

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

    private void updateDateButton() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        btnDatePicker.setText(sdf.format(selectedDate.getTime()));
    }

    private void fetchMealsOfDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(selectedDate.getTime());

        apiService.getToday(dateStr).enqueue(new Callback<DailyMeals>() {
            @Override
            public void onResponse(Call<DailyMeals> call, Response<DailyMeals> response) {
                if (response.isSuccessful()) {
                    mealsOfDate = response.body();

                    // Update adapter mapping
                    if (mealsOfDate != null) {
                        Map<Integer, String> mapping = new HashMap<>();
                        if (mealsOfDate.breakfast != null)
                            mapping.put(mealsOfDate.breakfast.meal_id, "조식");
                        if (mealsOfDate.lunch != null)
                            mapping.put(mealsOfDate.lunch.meal_id, "중식");
                        if (mealsOfDate.dinner != null)
                            mapping.put(mealsOfDate.dinner.meal_id, "석식");
                        adapter.setMealTypeMapping(mapping);
                    }

                    updateCurrentMealIdByChip();
                }
            }

            @Override
            public void onFailure(Call<DailyMeals> call, Throwable t) {
            }
        });
    }

    private void updateCurrentMealIdByChip() {
        int checkedId = chipGroupMealType.getCheckedChipId();
        if (checkedId == R.id.chipAll) {
            currentMealId = null;
        } else if (checkedId == R.id.chipBreakfast) {
            currentMealId = (mealsOfDate != null && mealsOfDate.breakfast != null) ? mealsOfDate.breakfast.meal_id : -1;
        } else if (checkedId == R.id.chipLunch) {
            currentMealId = (mealsOfDate != null && mealsOfDate.lunch != null) ? mealsOfDate.lunch.meal_id : -1;
        } else if (checkedId == R.id.chipDinner) {
            currentMealId = (mealsOfDate != null && mealsOfDate.dinner != null) ? mealsOfDate.dinner.meal_id : -1;
        }

        if (currentMealId != null && currentMealId == -1) {
            Toast.makeText(this, "해당 식단 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            allReviewsFromApi = new ArrayList<>();
            updateDisplayList();
        } else {
            fetchReviews();
        }
    }

    private void fetchReviews() {
        // Fetch with newest by default, then sort locally
        apiService.listReviews(currentMealId, null, "newest").enqueue(new Callback<ReviewList>() {
            @Override
            public void onResponse(Call<ReviewList> call, Response<ReviewList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allReviewsFromApi = response.body().reviews;
                    updateDisplayList();
                } else {
                    Toast.makeText(ReviewListActivity.this, "리뷰를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReviewList> call, Throwable t) {
                Toast.makeText(ReviewListActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDisplayList() {
        List<ReviewResponse> displayList = new ArrayList<>(allReviewsFromApi);

        // 1. Filtering (if "All" selected for specific date)
        if (currentMealId == null && mealsOfDate != null) {
            List<Integer> validMealIds = new ArrayList<>();
            if (mealsOfDate.breakfast != null)
                validMealIds.add(mealsOfDate.breakfast.meal_id);
            if (mealsOfDate.lunch != null)
                validMealIds.add(mealsOfDate.lunch.meal_id);
            if (mealsOfDate.dinner != null)
                validMealIds.add(mealsOfDate.dinner.meal_id);

            List<ReviewResponse> filtered = new ArrayList<>();
            for (ReviewResponse r : displayList) {
                if (validMealIds.contains(r.meal_id)) {
                    filtered.add(r);
                }
            }
            displayList = filtered;
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
            } else if (id == R.id.nav_stats || id == R.id.nav_mypage) {
                Toast.makeText(this, "준비 중인 기능입니다.", Toast.LENGTH_SHORT).show();
                return false;
            }
            return false;
        });
    }
}
