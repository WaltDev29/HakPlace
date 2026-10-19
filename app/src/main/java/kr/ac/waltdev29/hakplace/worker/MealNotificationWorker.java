package kr.ac.waltdev29.hakplace.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import kr.ac.waltdev29.hakplace.api.ApiClient;
import kr.ac.waltdev29.hakplace.api.ApiService;
import kr.ac.waltdev29.hakplace.api.models.DailyMeals;
import kr.ac.waltdev29.hakplace.utils.MealNotificationScheduler;
import kr.ac.waltdev29.hakplace.utils.NotificationHelper;
import retrofit2.Response;

/**
 * MealNotificationWorker는 예약된 시간(08:00, 11:30, 17:30)에 안드로이드 OS에 의해 깨어나
 * 최신 식단 데이터를 네트워크로 조회하고 알림을 띄운 뒤, 다음 날 일정을 스스로 재예약하는 백그라운드 워커입니다.
 */
public class MealNotificationWorker extends Worker {
    private static final String TAG = "MealNotificationWorker";

    public static final String KEY_TYPE = "key_notification_type";
    public static final String KEY_TARGET_HOUR = "key_target_hour";
    public static final String KEY_TARGET_MINUTE = "key_target_minute";
    public static final String KEY_WORK_NAME = "key_work_name";

    public MealNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        String type = getInputData().getString(KEY_TYPE);
        int targetHour = getInputData().getInt(KEY_TARGET_HOUR, 8);
        int targetMinute = getInputData().getInt(KEY_TARGET_MINUTE, 0);
        String workName = getInputData().getString(KEY_WORK_NAME);

        if (type == null) {
            type = MealNotificationScheduler.TYPE_DAILY_ALL;
        }
        if (workName == null) {
            workName = MealNotificationScheduler.WORK_NAME_DAILY_ALL;
        }

        try {
            // 1. 해당 알림 유형에 대한 사용자 설정 활성화 여부 확인
            boolean isEnabled = false;
            if (MealNotificationScheduler.TYPE_DAILY_ALL.equals(type)) {
                isEnabled = MealNotificationScheduler.isDailyAllEnabled(context);
            } else if (MealNotificationScheduler.TYPE_LUNCH.equals(type)) {
                isEnabled = MealNotificationScheduler.isLunchEnabled(context);
            } else if (MealNotificationScheduler.TYPE_DINNER.equals(type)) {
                isEnabled = MealNotificationScheduler.isDinnerEnabled(context);
            }

            // 2. 활성화 상태 및 시스템 알림 권한이 허용된 경우에만 API 호출 및 알림 발행
            if (isEnabled && NotificationHelper.areNotificationsEnabled(context)) {
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                ApiService apiService = ApiClient.getClient().create(ApiService.class);
                Response<DailyMeals> response = apiService.getToday(today).execute();

                if (response.isSuccessful() && response.body() != null) {
                    DailyMeals dailyMeals = response.body();

                    if (MealNotificationScheduler.TYPE_DAILY_ALL.equals(type)) {
                        NotificationHelper.showAllMealsNotification(context, dailyMeals);
                    } else if (MealNotificationScheduler.TYPE_LUNCH.equals(type)) {
                        NotificationHelper.showLunchNotification(context, dailyMeals.lunch);
                    } else if (MealNotificationScheduler.TYPE_DINNER.equals(type)) {
                        NotificationHelper.showDinnerNotification(context, dailyMeals.dinner);
                    }
                    Log.i(TAG, "학식 알림 발행 완료 (" + type + ")");
                } else {
                    Log.w(TAG, "학식 정보 조회 응답 실패: " + response.code());
                }
            } else {
                Log.i(TAG, "알림 설정이 꺼져 있거나 시스템 알림 권한이 없어 알림을 건너뜁니다 (" + type + ")");
            }

            // 3. 다음 날 같은 시각으로 작업 자가 재예약 (Self-Scheduling)
            if (isEnabled) {
                MealNotificationScheduler.scheduleWork(context, workName, type, targetHour, targetMinute);
            }

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "MealNotificationWorker 작업 중 예외 발생", e);

            // 네트워크 일시 오류인 경우 최대 2회까지 지수 백오프로 재시도
            if (getRunAttemptCount() < 2) {
                return Result.retry();
            }

            // 재시도 횟수 초과 시 내일 일정 예약 후 종료
            MealNotificationScheduler.scheduleWork(context, workName, type, targetHour, targetMinute);
            return Result.failure();
        }
    }
}
