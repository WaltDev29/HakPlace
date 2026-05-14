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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import kr.ac.waltdev29.hakplace.api.ApiClient;
import kr.ac.waltdev29.hakplace.api.ApiService;
import kr.ac.waltdev29.hakplace.api.models.DailyMeals;
import kr.ac.waltdev29.hakplace.api.models.ReviewList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewListActivity extends AppCompatActivity {

    private RecyclerView rvReviews;
    private ReviewAdapter adapter;
    private ApiService apiService;
    private Spinner spinnerSort;
    private Button btnDatePicker;
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
        spinnerSort = findViewById(R.id.spinnerSort);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        chipGroupMealType = findViewById(R.id.chipGroupMealType);

        adapter = new ReviewAdapter();
        rvReviews.setAdapter(adapter);

        // Intent handle
        if (getIntent().hasExtra("meal_id")) {
            currentMealId = getIntent().getIntExtra("meal_id", -1);
            if (currentMealId == -1) currentMealId = null;
        }

        updateDateButton();
        setupFilters();
        setupBottomNav();

        // Initial fetch: all reviews
        fetchReviews();
        // Also fetch meals for the current date to know meal_ids
        fetchMealsOfDate();
    }

    private void setupFilters() {
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: currentSort = "newest"; break;
                    case 1: currentSort = "rating_high"; break; // Check API supported values
                    case 2: currentSort = "rating_low"; break;
                }
                fetchReviews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnDatePicker.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                updateDateButton();
                fetchMealsOfDate();
            }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
        });

        chipGroupMealType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipAll) {
                currentMealId = null;
            } else if (id == R.id.chipBreakfast) {
                currentMealId = (mealsOfDate != null && mealsOfDate.breakfast != null) ? mealsOfDate.breakfast.meal_id : -1;
            } else if (id == R.id.chipLunch) {
                currentMealId = (mealsOfDate != null && mealsOfDate.lunch != null) ? mealsOfDate.lunch.meal_id : -1;
            } else if (id == R.id.chipDinner) {
                currentMealId = (mealsOfDate != null && mealsOfDate.dinner != null) ? mealsOfDate.dinner.meal_id : -1;
            }
            
            if (currentMealId != null && currentMealId == -1) {
                Toast.makeText(this, "해당 식단 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                adapter.setReviews(new java.util.ArrayList<>());
            } else {
                fetchReviews();
            }
        });
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
                    // If a specific meal type was already selected, update currentMealId and refresh
                    updateCurrentMealIdByChip();
                }
            }

            @Override
            public void onFailure(Call<DailyMeals> call, Throwable t) {}
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
        
        if (currentMealId != null) {
             fetchReviews();
        }
    }

    private void fetchReviews() {
        apiService.listReviews(currentMealId, null, currentSort).enqueue(new Callback<ReviewList>() {
            @Override
            public void onResponse(Call<ReviewList> call, Response<ReviewList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setReviews(response.body().reviews);
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
