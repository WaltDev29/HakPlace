package kr.ac.waltdev29.hakplace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import android.content.SharedPreferences;
import kr.ac.waltdev29.hakplace.api.ApiClient;
import kr.ac.waltdev29.hakplace.api.ApiService;
import kr.ac.waltdev29.hakplace.api.models.DailyMeals;
import kr.ac.waltdev29.hakplace.api.models.MealSchema;
import kr.ac.waltdev29.hakplace.api.models.WeeklyMeals;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import kr.ac.waltdev29.hakplace.utils.DialogHelper;

public class MenuWeekActivity extends AppCompatActivity {

    private TextView tvWeekRange;
    private LinearLayout[] dayButtons = new LinearLayout[5];
    private TextView[] dayLabels = new TextView[5];
    private TextView[] dayDates = new TextView[5];
    private View cardBreakfast, cardLunch, cardDinner;

    private ApiService apiService;
    private List<DailyMeals> weekMeals = new ArrayList<>();
    private int selectedDayIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_week);

        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupMealCards();
        setupBottomNav();
        fetchWeeklyMeals();
        fetchUserInfo();

        cardBreakfast.setOnClickListener(v -> {
            if (selectedDayIndex >= 0 && selectedDayIndex < weekMeals.size()) {
                showMealDetailPopup("조식", weekMeals.get(selectedDayIndex).breakfast,
                        weekMeals.get(selectedDayIndex).date);
            }
        });
        cardLunch.setOnClickListener(v -> {
            if (selectedDayIndex >= 0 && selectedDayIndex < weekMeals.size()) {
                showMealDetailPopup("중식", weekMeals.get(selectedDayIndex).lunch, weekMeals.get(selectedDayIndex).date);
            }
        });
        cardDinner.setOnClickListener(v -> {
            if (selectedDayIndex >= 0 && selectedDayIndex < weekMeals.size()) {
                showMealDetailPopup("석식", weekMeals.get(selectedDayIndex).dinner, weekMeals.get(selectedDayIndex).date);
            }
        });
    }

    private void initViews() {
        tvWeekRange = findViewById(R.id.tvWeekRange);

        dayButtons[0] = findViewById(R.id.btnMon);
        dayButtons[1] = findViewById(R.id.btnTue);
        dayButtons[2] = findViewById(R.id.btnWed);
        dayButtons[3] = findViewById(R.id.btnThu);
        dayButtons[4] = findViewById(R.id.btnFri);

        dayLabels[0] = findViewById(R.id.tvMonLabel);
        dayLabels[1] = findViewById(R.id.tvTueLabel);
        dayLabels[2] = findViewById(R.id.tvWedLabel);
        dayLabels[3] = findViewById(R.id.tvThuLabel);
        dayLabels[4] = findViewById(R.id.tvFriLabel);

        dayDates[0] = findViewById(R.id.tvMonDate);
        dayDates[1] = findViewById(R.id.tvTueDate);
        dayDates[2] = findViewById(R.id.tvWedDate);
        dayDates[3] = findViewById(R.id.tvThuDate);
        dayDates[4] = findViewById(R.id.tvFriDate);

        cardBreakfast = findViewById(R.id.cardBreakfast);
        cardLunch = findViewById(R.id.cardLunch);
        cardDinner = findViewById(R.id.cardDinner);

        for (int i = 0; i < 5; i++) {
            final int index = i;
            dayButtons[i].setOnClickListener(v -> selectDay(index));
        }
    }

    private void setupMealCards() {
        updateCardHeader(cardBreakfast, "조식", R.drawable.ic_sun_blue, "08:00 ~ 09:30");
        updateCardHeader(cardLunch, "중식", R.drawable.ic_sun, "11:30 ~ 13:30");
        updateCardHeader(cardDinner, "석식", R.drawable.ic_dinner, "17:00 ~ 18:30");
    }

    private void updateCardHeader(View card, String title, int iconRes, String time) {
        ((TextView) card.findViewById(R.id.tvMealType)).setText(title);
        ((ImageView) card.findViewById(R.id.ivMealIcon)).setImageResource(iconRes);
        ((TextView) card.findViewById(R.id.tvMealTime)).setText(time);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_weekly);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_weekly) {
                return true;
            } else if (id == R.id.nav_today) {
                startActivity(new Intent(this, MenuTodayActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_stats) {
                startActivity(new Intent(this, StatisticsActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_review) {
                startActivity(new Intent(this, ReviewListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_mypage) {
                startActivity(new Intent(this, MyPageActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_stats) {
                Toast.makeText(this, getString(R.string.msg_preparing), Toast.LENGTH_SHORT).show();
                return false;
            }
            return false;
        });
    }

    private void fetchWeeklyMeals() {
        apiService.getWeekly().enqueue(new Callback<WeeklyMeals>() {
            @Override
            public void onResponse(Call<WeeklyMeals> call, Response<WeeklyMeals> response) {
                if (response.isSuccessful() && response.body() != null) {
                    weekMeals = response.body().week_meals;
                    updateDateSelector();
                    autoSelectCurrentDay();
                } else {
                    Toast.makeText(MenuWeekActivity.this, getString(R.string.no_meal_info), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeeklyMeals> call, Throwable t) {
                Toast.makeText(MenuWeekActivity.this, getString(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserInfo() {
        SharedPreferences prefs = getSharedPreferences("hakplace_prefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token == null) return;

        apiService.getMe("Bearer " + token).enqueue(new Callback<kr.ac.waltdev29.hakplace.api.models.UserInfo>() {
            @Override
            public void onResponse(Call<kr.ac.waltdev29.hakplace.api.models.UserInfo> call, Response<kr.ac.waltdev29.hakplace.api.models.UserInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("student_id", response.body().student_id);
                    editor.apply();
                }
            }

            @Override
            public void onFailure(Call<kr.ac.waltdev29.hakplace.api.models.UserInfo> call, Throwable t) {
            }
        });
    }

    private void updateDateSelector() {
        if (weekMeals.isEmpty())
            return;

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("M/d", Locale.getDefault());

            // Set Week Range Header
            Date firstDate = inputFormat.parse(weekMeals.get(0).date);
            Date lastDate = inputFormat.parse(weekMeals.get(weekMeals.size() - 1).date);

            SimpleDateFormat rangeFormat = new SimpleDateFormat("M월 d일", Locale.KOREAN);
            tvWeekRange.setText(rangeFormat.format(firstDate) + " ~ " + rangeFormat.format(lastDate));

            for (int i = 0; i < weekMeals.size() && i < 5; i++) {
                Date date = inputFormat.parse(weekMeals.get(i).date);
                dayDates[i].setText(outputFormat.format(date));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void autoSelectCurrentDay() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int todayIndex = 0; // Default to Monday

        for (int i = 0; i < weekMeals.size(); i++) {
            if (weekMeals.get(i).date.equals(today)) {
                todayIndex = i;
                break;
            }
        }

        // If today is weekend, index might be off, but the API usually returns Mon-Fri.
        // If we can't find today, we check if today is Saturday or Sunday.
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            todayIndex = 0; // Show Monday
        }

        selectDay(todayIndex);
    }

    private void selectDay(int index) {
        if (index < 0 || index >= weekMeals.size())
            return;
        if (selectedDayIndex == index)
            return;

        selectedDayIndex = index;

        // Update Buttons UI
        for (int i = 0; i < 5; i++) {
            if (i == index) {
                dayButtons[i].setBackgroundResource(R.drawable.bg_day_active);
                dayLabels[i].setTextColor(ContextCompat.getColor(this, R.color.white));
                dayDates[i].setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                dayButtons[i].setBackgroundResource(R.drawable.bg_day_inactive);
                dayLabels[i].setTextColor(ContextCompat.getColor(this, R.color.on_surface));
                dayDates[i].setTextColor(ContextCompat.getColor(this, R.color.on_surface_variant));
            }
        }

        // Update Meals
        displayMeals(weekMeals.get(index));
    }

    private void displayMeals(DailyMeals dailyMeals) {
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

            int marginTopBottom = (int) (4 * getResources().getDisplayMetrics().density);

            for (String food : meal.foods) {
                TextView tv = new TextView(this);
                tv.setText("• " + food);
                tv.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
                tv.setTextSize(14);
                tv.setMaxLines(1);
                tv.setEllipsize(android.text.TextUtils.TruncateAt.END);

                android.widget.GridLayout.LayoutParams params = new android.widget.GridLayout.LayoutParams();
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

    private void showMealDetailPopup(String mealType, MealSchema meal, String date) {
        if (meal == null) {
            Toast.makeText(this, getString(R.string.no_meal_info), Toast.LENGTH_SHORT).show();
            return;
        }

        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                this);
        View view = getLayoutInflater().inflate(R.layout.dialog_meal_detail, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvPopupTitle);
        TextView tvRating = view.findViewById(R.id.tvPopupRating);
        TextView tvReviewCount = view.findViewById(R.id.tvPopupReviewCount);
        View btnClose = view.findViewById(R.id.btnClosePopup);
        View btnWrite = view.findViewById(R.id.btnWriteReview);
        View btnView = view.findViewById(R.id.btnViewReviews);
        LinearLayout llStars = view.findViewById(R.id.llStars);

        // Format: 중식 5/20 (월)
        String tempDateStr;
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("M/d (E)", Locale.KOREAN);
            tempDateStr = outputFormat.format(inputFormat.parse(date));
        } catch (Exception e) {
            tempDateStr = date;
        }
        final String dateStr = tempDateStr;
        tvTitle.setText(mealType + " " + dateStr);

        tvRating.setText(String.format(Locale.getDefault(), "%.1f", meal.avg_rating));
        tvReviewCount.setText(
                String.format(Locale.getDefault(), getString(R.string.review_count_format), meal.review_count));

        // Update Stars
        for (int i = 0; i < 5; i++) {
            ImageView star = (ImageView) llStars.getChildAt(i);
            if (meal.avg_rating >= i + 0.8) {
                star.setImageResource(R.drawable.ic_star);
            } else {
                star.setImageResource(R.drawable.ic_star_border);
            }
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnWrite.setOnClickListener(v -> {
            // 1. 시간 및 날짜 제한 체크
            if (!isReviewable(mealType, date)) {
                String startTime = "";
                if (mealType.equals("조식")) startTime = "07:30";
                else if (mealType.equals("중식")) startTime = "11:30";
                else if (mealType.equals("석식")) startTime = "17:30";

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                String today = sdf.format(new Date());

                String title = "리뷰 작성 제한";
                String msg;
                if (!today.equals(date)) {
                    msg = "당일 식단만 리뷰를 작성할 수 있습니다.";
                } else {
                    title = mealType + " 리뷰 시간 안내";
                    msg = mealType + " 리뷰는 " + startTime + "부터 작성 가능합니다.";
                }
                DialogHelper.showNotificationDialog(this, title, msg, null);
                return;
            }

            // 2. 중복 작성 체크 (API 호출)
            SharedPreferences prefs = getSharedPreferences("hakplace_prefs", MODE_PRIVATE);
            String studentId = prefs.getString("student_id", null);

            if (studentId == null) {
                fetchUserInfo();
                DialogHelper.showNotificationDialog(this, "정보 확인 중", "사용자 정보를 동기화 중입니다. 잠시 후 다시 시도해주세요.", null);
                return;
            }

            btnWrite.setEnabled(false);
            apiService.listReviews(meal.meal_id, studentId, null, null, null).enqueue(new Callback<kr.ac.waltdev29.hakplace.api.models.ReviewList>() {
                @Override
                public void onResponse(Call<kr.ac.waltdev29.hakplace.api.models.ReviewList> call, Response<kr.ac.waltdev29.hakplace.api.models.ReviewList> response) {
                    btnWrite.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().reviews != null && !response.body().reviews.isEmpty()) {
                            DialogHelper.showNotificationDialog(MenuWeekActivity.this, "중복 작성 제한", "이미 이 식단에 리뷰를 작성하셨습니다.", null);
                        } else {
                            android.content.Intent intent = new android.content.Intent(MenuWeekActivity.this, ReviewWriteActivity.class);
                            intent.putExtra("meal_id", meal.meal_id);
                            intent.putExtra("meal_type", mealType);
                            intent.putExtra("date", dateStr);
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    } else {
                        Toast.makeText(MenuWeekActivity.this, "상태 확인 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<kr.ac.waltdev29.hakplace.api.models.ReviewList> call, Throwable t) {
                    btnWrite.setEnabled(true);
                    Toast.makeText(MenuWeekActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                }
            });
        });
        btnView.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReviewListActivity.class);
            intent.putExtra("meal_id", meal.meal_id);
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();
    }

    private boolean isReviewable(String mealType, String mealDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String today = sdf.format(new Date());

        if (!today.equals(mealDate)) {
            return false;
        }

        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int currentTime = hour * 100 + minute;

        if (mealType.equals("조식")) {
            return currentTime >= 730;
        } else if (mealType.equals("중식")) {
            return currentTime >= 1130;
        } else if (mealType.equals("석식")) {
            return currentTime >= 1730;
        }
        return false;
    }
}
