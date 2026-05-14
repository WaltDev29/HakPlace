package kr.ac.waltdev29.hakplace;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import kr.ac.waltdev29.hakplace.api.ApiClient;
import kr.ac.waltdev29.hakplace.api.ApiService;
import kr.ac.waltdev29.hakplace.api.models.DailyMeals;
import kr.ac.waltdev29.hakplace.api.models.MealSchema;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuTodayActivity extends AppCompatActivity {

    private TextView tvDateHeader;
    private View cardBreakfast, cardLunch, cardDinner;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_today);

        apiService = ApiClient.getClient().create(ApiService.class);

        tvDateHeader = findViewById(R.id.tvDateHeader);
        cardBreakfast = findViewById(R.id.cardBreakfast);
        cardLunch = findViewById(R.id.cardLunch);
        cardDinner = findViewById(R.id.cardDinner);

        setupMealCards();
        setupBottomNav();

        fetchTodayMeals();
    }

    private void setupMealCards() {
        // Breakfast Setup
        updateCardHeader(cardBreakfast, "조식", R.drawable.ic_sun, "08:00 ~ 09:30");
        // Lunch Setup
        updateCardHeader(cardLunch, "중식", R.drawable.ic_lunch, "11:30 ~ 13:30");
        // Dinner Setup
        updateCardHeader(cardDinner, "석식", R.drawable.ic_dinner, "17:00 ~ 18:30");
    }

    private void updateCardHeader(View card, String title, int iconRes, String time) {
        ((TextView) card.findViewById(R.id.tvMealType)).setText(title);
        ((ImageView) card.findViewById(R.id.ivMealIcon)).setImageResource(iconRes);
        ((TextView) card.findViewById(R.id.tvMealTime)).setText(time);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_today) {
                return true;
            } else if (id == R.id.nav_weekly || id == R.id.nav_stats || id == R.id.nav_review
                    || id == R.id.nav_mypage) {
                Toast.makeText(this, "준비 중인 기능입니다.", Toast.LENGTH_SHORT).show();
                return false;
            }
            return false;
        });
    }

    private void fetchTodayMeals() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        // For testing/demo, if today's data is empty, the API might return 404 or
        // empty.
        // We'll just pass the date.

        apiService.getToday(today).enqueue(new Callback<DailyMeals>() {
            @Override
            public void onResponse(Call<DailyMeals> call, Response<DailyMeals> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayMeals(response.body());
                } else {
                    Toast.makeText(MenuTodayActivity.this, "식단 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    setEmptyState();
                }
            }

            @Override
            public void onFailure(Call<DailyMeals> call, Throwable t) {
                Toast.makeText(MenuTodayActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                setEmptyState();
            }

        });
    }

    private void displayMeals(DailyMeals dailyMeals) {
        // Update Date Header
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("M월 d일 (E)", Locale.KOREAN);
            Date date = inputFormat.parse(dailyMeals.date);
            tvDateHeader.setText(outputFormat.format(date));
        } catch (Exception e) {
            tvDateHeader.setText(dailyMeals.date);
        }

        updateMealInfo(cardBreakfast, dailyMeals.breakfast);
        updateMealInfo(cardLunch, dailyMeals.lunch);
        updateMealInfo(cardDinner, dailyMeals.dinner);
    }

    private void updateMealInfo(View card, MealSchema meal) {
        android.widget.GridLayout glFoods = card.findViewById(R.id.glFoods);
        TextView tvEmptyMeal = card.findViewById(R.id.tvEmptyMeal);
        TextView tvRating = card.findViewById(R.id.tvRating);
        TextView tvReviewCount = card.findViewById(R.id.tvReviewCount);

        glFoods.removeAllViews();

        if (meal != null && meal.foods != null && !meal.foods.isEmpty()) {
            glFoods.setVisibility(View.VISIBLE);
            tvEmptyMeal.setVisibility(View.GONE);

            // 기기 해상도에 맞게 상하 여백 계산 (4dp)
            int marginTopBottom = (int) (4 * getResources().getDisplayMetrics().density);

            for (String food : meal.foods) {
                TextView tv = new TextView(this);
                tv.setText("• " + food);
                tv.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.on_surface));
                tv.setTextSize(14);
                tv.setMaxLines(1);
                tv.setEllipsize(android.text.TextUtils.TruncateAt.END);

                android.widget.GridLayout.LayoutParams params = new android.widget.GridLayout.LayoutParams();
                // 너비를 0으로 하고 가중치 1을 주어 정확히 50%씩 차지하게 합니다.
                params.width = 0;
                params.height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT;
                params.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f);
                params.setMargins(8, marginTopBottom, 8, marginTopBottom);
                tv.setLayoutParams(params);

                glFoods.addView(tv);
            }

            tvRating.setText(String.format(Locale.getDefault(), "%.1f", meal.avg_rating));
            tvReviewCount.setText("(" + meal.review_count + ")");
        } else {
            glFoods.setVisibility(View.GONE);
            tvEmptyMeal.setVisibility(View.VISIBLE);
            tvRating.setText("0.0");
            tvReviewCount.setText("(0)");
        }
    }

    private void setEmptyState() {
        tvDateHeader.setText("정보 없음");
        updateMealInfo(cardBreakfast, null);
        updateMealInfo(cardLunch, null);
        updateMealInfo(cardDinner, null);
    }
}