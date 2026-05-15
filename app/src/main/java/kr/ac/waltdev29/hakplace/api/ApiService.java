package kr.ac.waltdev29.hakplace.api;

import kr.ac.waltdev29.hakplace.api.models.DailyMeals;
import kr.ac.waltdev29.hakplace.api.models.ReviewCreate;
import kr.ac.waltdev29.hakplace.api.models.ReviewList;
import kr.ac.waltdev29.hakplace.api.models.ReviewResponse;
import kr.ac.waltdev29.hakplace.api.models.StatisticList;
import kr.ac.waltdev29.hakplace.api.models.StatisticResponse;
import kr.ac.waltdev29.hakplace.api.models.Token;
import kr.ac.waltdev29.hakplace.api.models.UserInfo;
import kr.ac.waltdev29.hakplace.api.models.UserSignup;
import kr.ac.waltdev29.hakplace.api.models.WeeklyMeals;
import kr.ac.waltdev29.hakplace.api.models.WeeklyGraphData;
import kr.ac.waltdev29.hakplace.api.models.MonthlyGraphData;
import kr.ac.waltdev29.hakplace.api.models.FoodRatingList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Header;

public interface ApiService {

    @FormUrlEncoded
    @POST("/auth/login")
    Call<Token> login(@Field("username") String username, @Field("password") String password);

    @POST("/auth/signup")
    Call<ResponseBody> signup(@Body UserSignup signup);

    @GET("/users/me")
    Call<UserInfo> getMe(@Header("Authorization") String bearer);

    @GET("/meals/today")
    Call<DailyMeals> getToday(@Query("target_date") String targetDate);

    @GET("/meals/weekly")
    Call<WeeklyMeals> getWeekly();

    @POST("/reviews/")
    Call<ReviewResponse> createReview(@Header("Authorization") String bearer, @Body ReviewCreate review);

    @GET("/reviews/")
    Call<ReviewList> listReviews(
            @Query("meal_id") Integer mealId,
            @Query("student_id") String studentId,
            @Query("sort_by") String sortBy,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );

    @GET("/statistics/")
    Call<StatisticList> getStatistics(@Query("period_type") String periodType);

    @GET("/statistics/weekly")
    Call<List<WeeklyGraphData>> getWeeklyGraphData(@Query("date") String date);

    @GET("/statistics/monthly")
    Call<List<MonthlyGraphData>> getMonthlyGraphData(@Query("month") String month);

    @GET("/statistics/foods")
    Call<FoodRatingList> getFoodRatings();

    @GET("/statistics/{stat_id}")
    Call<StatisticResponse> getStatistic(@Path("stat_id") int statId);
}
