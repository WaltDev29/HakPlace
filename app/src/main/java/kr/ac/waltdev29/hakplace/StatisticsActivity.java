package kr.ac.waltdev29.hakplace;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import kr.ac.waltdev29.hakplace.api.ApiClient;
import kr.ac.waltdev29.hakplace.api.ApiService;
import kr.ac.waltdev29.hakplace.api.models.DailyMeals;
import kr.ac.waltdev29.hakplace.api.models.FoodRatingList;
import kr.ac.waltdev29.hakplace.api.models.FoodRatingResponse;
import kr.ac.waltdev29.hakplace.api.models.MealSchema;
import kr.ac.waltdev29.hakplace.api.models.MonthlyGraphData;
import kr.ac.waltdev29.hakplace.api.models.StatisticList;
import kr.ac.waltdev29.hakplace.api.models.StatisticResponse;
import kr.ac.waltdev29.hakplace.api.models.WeeklyAIComment;
import kr.ac.waltdev29.hakplace.api.models.WeeklyGraphData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsActivity extends AppCompatActivity {

    private ApiService apiService;
    private LineChart lineChart;
    private TextView tvPeriodRange, tvAvgRating;
    private RatingBar ratingBar;
    private LinearLayout containerTopFoods;
    private TextView tvAiAnalysis, tvTrendAnalysis, tvBestMeal, tvImprovementPoints;
    private MaterialButtonToggleGroup toggleGroupPeriod;

    private boolean isWeekly = true;
    private Calendar currentCalendar = Calendar.getInstance();
    private List<WeeklyGraphData> currentWeeklyData;
    private List<MonthlyGraphData> currentMonthlyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        apiService = ApiClient.getClient().create(ApiService.class);

        lineChart = findViewById(R.id.lineChart);
        tvPeriodRange = findViewById(R.id.tvPeriodRange);
        tvAvgRating = findViewById(R.id.tvAvgRating);
        ratingBar = findViewById(R.id.ratingBar);
        containerTopFoods = findViewById(R.id.containerTopFoods);
        tvAiAnalysis = findViewById(R.id.tvAiAnalysis);
        tvTrendAnalysis = findViewById(R.id.tvTrendAnalysis);
        tvBestMeal = findViewById(R.id.tvBestMeal);
        tvImprovementPoints = findViewById(R.id.tvImprovementPoints);
        toggleGroupPeriod = findViewById(R.id.toggleGroupPeriod);

        setupChart();
        setupToggle();
        setupBottomNav();
        setupPeriodButtons();

        loadData();
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setExtraBottomOffset(5f);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#40493D"));
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(false);
        xAxis.setYOffset(15f);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisLeft().setAxisMaximum(5f);
        lineChart.getAxisLeft().setGranularity(1f);
        lineChart.getAxisLeft().setTextColor(Color.parseColor("#40493D"));
        lineChart.getLegend().setEnabled(false);
    }

    private void setupToggle() {
        toggleGroupPeriod.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isWeekly = (checkedId == R.id.btnWeekly);
                loadData();
            }
        });
    }

    private void setupPeriodButtons() {
        findViewById(R.id.btnPrevPeriod).setOnClickListener(v -> {
            if (isWeekly) {
                currentCalendar.add(Calendar.WEEK_OF_YEAR, -1);
            } else {
                currentCalendar.add(Calendar.MONTH, -1);
            }
            loadData();
        });

        findViewById(R.id.btnNextPeriod).setOnClickListener(v -> {
            if (isWeekly) {
                currentCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            } else {
                currentCalendar.add(Calendar.MONTH, 1);
            }
            loadData();
        });
    }

    private void loadData() {
        updatePeriodDisplay();
        fetchGraphData();
        fetchFoodRatings();
        fetchAiComment();
    }

    private void updatePeriodDisplay() {
        SimpleDateFormat sdf;
        if (isWeekly) {
            sdf = new SimpleDateFormat("MM월 dd일", Locale.KOREA);
            Calendar start = (Calendar) currentCalendar.clone();
            start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DATE, 6);
            tvPeriodRange.setText(sdf.format(start.getTime()) + " ~ " + sdf.format(end.getTime()));
        } else {
            sdf = new SimpleDateFormat("yyyy년 MM월", Locale.KOREA);
            tvPeriodRange.setText(sdf.format(currentCalendar.getTime()));
        }
    }

    private void fetchGraphData() {
        if (isWeekly) {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentCalendar.getTime());
            apiService.getWeeklyGraphData(date).enqueue(new Callback<List<WeeklyGraphData>>() {
                @Override
                public void onResponse(Call<List<WeeklyGraphData>> call, Response<List<WeeklyGraphData>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentWeeklyData = response.body();
                        updateWeeklyChart(currentWeeklyData);
                    }
                }

                @Override
                public void onFailure(Call<List<WeeklyGraphData>> call, Throwable t) {
                    Toast.makeText(StatisticsActivity.this, getString(R.string.error_graph_load), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentCalendar.getTime());
            apiService.getMonthlyGraphData(month).enqueue(new Callback<List<MonthlyGraphData>>() {
                @Override
                public void onResponse(Call<List<MonthlyGraphData>> call, Response<List<MonthlyGraphData>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentMonthlyData = response.body();
                        updateMonthlyChart(currentMonthlyData);
                    }
                }

                @Override
                public void onFailure(Call<List<MonthlyGraphData>> call, Throwable t) {
                    Toast.makeText(StatisticsActivity.this, getString(R.string.error_graph_load), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateWeeklyChart(List<WeeklyGraphData> data) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        double total = 0;
        int count = 0;

        for (int i = 0; i < data.size(); i++) {
            WeeklyGraphData item = data.get(i);
            float val = item.avg_rating != null ? item.avg_rating.floatValue() : 0f;
            entries.add(new Entry(i, val));

            String dateLabel = item.label;
            if (item.date != null && item.date.length() >= 10) {
                String dayPart = item.date.substring(8, 10);
                dateLabel += " " + dayPart;
            }
            labels.add(dateLabel);

            if (item.avg_rating != null) {
                total += item.avg_rating;
                count++;
            }
        }

        renderChart(entries, labels);
        updateAvgRating(count > 0 ? total / count : 0.0);
    }

    private void updateMonthlyChart(List<MonthlyGraphData> data) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        double total = 0;
        int count = 0;

        for (int i = 0; i < data.size(); i++) {
            MonthlyGraphData item = data.get(i);
            float val = item.avg_rating != null ? item.avg_rating.floatValue() : 0f;
            entries.add(new Entry(i, val));
            labels.add(item.label);
            if (item.avg_rating != null) {
                total += item.avg_rating;
                count++;
            }
        }

        renderChart(entries, labels);
        updateAvgRating(count > 0 ? total / count : 0.0);
    }

    private void renderChart(ArrayList<Entry> entries, ArrayList<String> labels) {
        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.stat_rating_label));
        dataSet.setColor(Color.parseColor("#2E7D32"));
        dataSet.setCircleColor(Color.parseColor("#2E7D32"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(6f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(15f);
        dataSet.setDrawFilled(false);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1f", value);
            }
        });

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(labels.size() - 0.5f);

        lineChart.invalidate();
    }

    private void updateAvgRating(double avg) {
        tvAvgRating.setText(String.format(Locale.getDefault(), "%.1f", avg));
        ratingBar.setRating((float) avg);
    }

    private void fetchFoodRatings() {
        apiService.getFoodRatings().enqueue(new Callback<FoodRatingList>() {
            @Override
            public void onResponse(Call<FoodRatingList> call, Response<FoodRatingList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FoodRatingResponse> foods = response.body().foods;
                    Collections.sort(foods, (f1, f2) -> Double.compare(f2.avg_rating, f1.avg_rating));
                    updateTopFoods(foods.subList(0, Math.min(3, foods.size())));
                }
            }

            @Override
            public void onFailure(Call<FoodRatingList> call, Throwable t) {
            }
        });
    }

    private void updateTopFoods(List<FoodRatingResponse> foods) {
        containerTopFoods.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < foods.size(); i++) {
            FoodRatingResponse food = foods.get(i);
            View view = inflater.inflate(R.layout.item_top_food, containerTopFoods, false);
            ((TextView) view.findViewById(R.id.tvRank)).setText(String.valueOf(i + 1));
            ((TextView) view.findViewById(R.id.tvFoodName)).setText(food.name);
            ((TextView) view.findViewById(R.id.tvFoodRating))
                    .setText(String.format(Locale.getDefault(), "%.1f", food.avg_rating));
            containerTopFoods.addView(view);
        }
    }

    private void fetchAiComment() {
        String periodType = isWeekly ? "WEEKLY" : "MONTHLY";
        apiService.getStatistics(periodType).enqueue(new Callback<StatisticList>() {
            @Override
            public void onResponse(Call<StatisticList> call, Response<StatisticList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<StatisticResponse> stats = response.body().stats;
                    String targetValue;
                    if (isWeekly) {
                        Calendar cal = (Calendar) currentCalendar.clone();
                        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                        // 요일 계산 (일요일=1, 월요일=2, ...)
                        // 월요일을 주의 시작으로 맞춤
                        if (dayOfWeek == Calendar.SUNDAY) {
                            cal.add(Calendar.DATE, -6);
                        } else {
                            cal.add(Calendar.DATE, Calendar.MONDAY - dayOfWeek);
                        }
                        targetValue = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
                    } else {
                        targetValue = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
                                .format(currentCalendar.getTime());
                    }

                    for (StatisticResponse stat : stats) {
                        if (stat.period_value.equals(targetValue)) {
                            displayAiComment(stat.ai_comment);
                            return;
                        }
                    }
                    // If no exact match, show empty or placeholder
                    resetAiComment();
                }
            }

            @Override
            public void onFailure(Call<StatisticList> call, Throwable t) {
            }
        });
    }

    private void displayAiComment(Object commentObj) {
        if (commentObj == null) {
            resetAiComment();
            return;
        }

        WeeklyAIComment comment;
        if (commentObj instanceof String) {
            comment = new Gson().fromJson((String) commentObj, WeeklyAIComment.class);
        } else {
            // If already parsed by Gson as a Map or object
            String json = new Gson().toJson(commentObj);
            comment = new Gson().fromJson(json, WeeklyAIComment.class);
        }

        tvAiAnalysis.setText(comment.ai_analysis != null ? comment.ai_analysis : getString(R.string.no_data));
        tvTrendAnalysis.setText(comment.trend_analysis != null ? comment.trend_analysis : "-");
        
        String bestMealStr = "-";
        // Calculate best meal from graph data
        if (isWeekly && currentWeeklyData != null && !currentWeeklyData.isEmpty()) {
            WeeklyGraphData bestDay = null;
            double maxRating = -1;
            for (WeeklyGraphData d : currentWeeklyData) {
                if (d.avg_rating != null && d.avg_rating > maxRating) {
                    maxRating = d.avg_rating;
                    bestDay = d;
                }
            }
            if (bestDay != null && maxRating > 0) {
                fetchAndDisplayBestMealDetails(bestDay.date, bestDay.label, maxRating);
                return; // tvBestMeal will be updated in callback
            }
        } else if (!isWeekly && currentMonthlyData != null && !currentMonthlyData.isEmpty()) {
            MonthlyGraphData bestWeek = null;
            double maxRating = -1;
            for (MonthlyGraphData d : currentMonthlyData) {
                if (d.avg_rating != null && d.avg_rating > maxRating) {
                    maxRating = d.avg_rating;
                    bestWeek = d;
                }
            }
            if (bestWeek != null && maxRating > 0) {
                bestMealStr = bestWeek.label + " (" + String.format(Locale.getDefault(), "%.1f", maxRating) + getString(R.string.unit_points) + ")";
            }
        }
        tvBestMeal.setText(bestMealStr);

        tvImprovementPoints.setText(comment.key_feedback != null ? comment.key_feedback : "-");
    }

    private void fetchAndDisplayBestMealDetails(String date, String label, double dayAvg) {
        tvBestMeal.setText(label + " (" + String.format(Locale.getDefault(), "%.1f", dayAvg) + getString(R.string.unit_points) + ")");

        apiService.getToday(date).enqueue(new Callback<DailyMeals>() {
            @Override
            public void onResponse(Call<DailyMeals> call, Response<DailyMeals> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DailyMeals dm = response.body();
                    MealSchema bestMeal = null;
                    double maxRating = -1;

                    // Find the best meal among B/L/D of that day
                    if (dm.breakfast != null && dm.breakfast.avg_rating > maxRating) {
                        maxRating = dm.breakfast.avg_rating;
                        bestMeal = dm.breakfast;
                    }
                    if (dm.lunch != null && dm.lunch.avg_rating > maxRating) {
                        maxRating = dm.lunch.avg_rating;
                        bestMeal = dm.lunch;
                    }
                    if (dm.dinner != null && dm.dinner.avg_rating > maxRating) {
                        maxRating = dm.dinner.avg_rating;
                        bestMeal = dm.dinner;
                    }

                    if (bestMeal != null) {
                        try {
                            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            SimpleDateFormat out = new SimpleDateFormat("M/d (E)", Locale.KOREA);
                            String dateLabel = out.format(in.parse(date));

                            StringBuilder sb = new StringBuilder();
                            sb.append(dateLabel).append(" ").append(bestMeal.meal_type)
                                    .append(" [").append(String.format(Locale.getDefault(), "%.1f", bestMeal.avg_rating))
                                    .append(getString(R.string.unit_points)).append("]\n");

                            if (bestMeal.foods != null) {
                                for (int i = 0; i < bestMeal.foods.size(); i++) {
                                    sb.append(bestMeal.foods.get(i));
                                    if (i < bestMeal.foods.size() - 1) sb.append(", ");
                                }
                            }
                            tvBestMeal.setText(sb.toString());
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<DailyMeals> call, Throwable t) {
            }
        });
    }

    private void resetAiComment() {
        tvAiAnalysis.setText(getString(R.string.no_ai_analysis));
        tvTrendAnalysis.setText("-");
        tvBestMeal.setText("-");
        tvImprovementPoints.setText("-");
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_stats);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_stats) {
                return true;
            } else if (id == R.id.nav_today) {
                startActivity(new Intent(this, MenuTodayActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_weekly) {
                startActivity(new Intent(this, MenuWeekActivity.class));
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
            }
            return false;
        });
    }
}
