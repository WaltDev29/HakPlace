package kr.ac.waltdev29.hakplace.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import kr.ac.waltdev29.hakplace.api.ApiClient;
import kr.ac.waltdev29.hakplace.api.ApiService;
import kr.ac.waltdev29.hakplace.api.models.DailyMeals;
import kr.ac.waltdev29.hakplace.utils.MealNotificationScheduler;
import kr.ac.waltdev29.hakplace.utils.NotificationHelper;
import retrofit2.Response;

/**
 * MealNotificationReceiver는 AlarmManager로부터 정해진 시각(08:00, 11:30, 17:30)에 브로드캐스트를 수신하여
 * 평일 여부 및 시간 오차를 검증한 후 학식 알림을 표시하고, 다음 평일 알람을 재예약합니다.
 */
public class MealNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "MealNotifReceiver";
    private static final long MAX_STALE_TOLERANCE_MS = TimeUnit.MINUTES.toMillis(45); // 45분 이상 지연 시 스킵
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String type = intent.getStringExtra(MealNotificationScheduler.EXTRA_TYPE);
        int targetHour = intent.getIntExtra(MealNotificationScheduler.EXTRA_TARGET_HOUR, 8);
        int targetMinute = intent.getIntExtra(MealNotificationScheduler.EXTRA_TARGET_MINUTE, 0);
        int requestCode = intent.getIntExtra(MealNotificationScheduler.EXTRA_REQUEST_CODE, MealNotificationScheduler.REQ_CODE_DAILY_ALL);

        if (type == null) {
            type = MealNotificationScheduler.TYPE_DAILY_ALL;
        }

        Log.i(TAG, "알림 트리거 수신: type=" + type + ", target=" + targetHour + ":" + targetMinute);

        // 1. 주말(토, 일) 검사: 주말에는 알림을 표시하지 않음
        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            Log.i(TAG, "주말(토/일)이므로 알림을 건너뜁니다. 다음 평일로 재예약합니다.");
            MealNotificationScheduler.scheduleMealAlarm(context, requestCode, type, targetHour, targetMinute);
            return;
        }

        // 2. 시간 오차(Stale) 검사: 기기가 꺼져있다가 켜지는 등의 이유로 목표 시각보다 45분 이상 지났으면 알림 스킵
        Calendar targetToday = Calendar.getInstance();
        targetToday.set(Calendar.HOUR_OF_DAY, targetHour);
        targetToday.set(Calendar.MINUTE, targetMinute);
        targetToday.set(Calendar.SECOND, 0);
        targetToday.set(Calendar.MILLISECOND, 0);

        long timeDiff = Math.abs(now.getTimeInMillis() - targetToday.getTimeInMillis());
        if (timeDiff > MAX_STALE_TOLERANCE_MS) {
            Log.w(TAG, "목표 시각과의 차이가 큽니다(" + (timeDiff / 1000 / 60) + "분). 지연 알림을 건너뛰고 다음 평일로 재예약합니다.");
            MealNotificationScheduler.scheduleMealAlarm(context, requestCode, type, targetHour, targetMinute);
            return;
        }

        // 3. 사용자 설정 활성화 여부 확인
        boolean isEnabled = false;
        if (MealNotificationScheduler.TYPE_DAILY_ALL.equals(type)) {
            isEnabled = MealNotificationScheduler.isDailyAllEnabled(context);
        } else if (MealNotificationScheduler.TYPE_LUNCH.equals(type)) {
            isEnabled = MealNotificationScheduler.isLunchEnabled(context);
        } else if (MealNotificationScheduler.TYPE_DINNER.equals(type)) {
            isEnabled = MealNotificationScheduler.isDinnerEnabled(context);
        }

        if (!isEnabled || !NotificationHelper.areNotificationsEnabled(context)) {
            Log.i(TAG, "알림 설정이 비활성화되어 있거나 권한이 없어 생략합니다. 다음 평일 일정 재예약.");
            MealNotificationScheduler.scheduleMealAlarm(context, requestCode, type, targetHour, targetMinute);
            return;
        }

        // 4. 비동기 백그라운드 스레드에서 API 호출 및 알림 발행 (goAsync 사용)
        final PendingResult pendingResult = goAsync();
        final String finalType = type;
        final int finalReqCode = requestCode;
        final int finalHour = targetHour;
        final int finalMinute = targetMinute;

        EXECUTOR.execute(() -> {
            try {
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                ApiService apiService = ApiClient.getClient().create(ApiService.class);
                Response<DailyMeals> response = apiService.getToday(today).execute();

                if (response.isSuccessful() && response.body() != null) {
                    DailyMeals dailyMeals = response.body();

                    if (MealNotificationScheduler.TYPE_DAILY_ALL.equals(finalType)) {
                        NotificationHelper.showAllMealsNotification(context, dailyMeals);
                    } else if (MealNotificationScheduler.TYPE_LUNCH.equals(finalType)) {
                        NotificationHelper.showLunchNotification(context, dailyMeals.lunch);
                    } else if (MealNotificationScheduler.TYPE_DINNER.equals(finalType)) {
                        NotificationHelper.showDinnerNotification(context, dailyMeals.dinner);
                    }
                    Log.i(TAG, "정시 학식 알림 발행 성공 (" + finalType + ")");
                } else {
                    Log.w(TAG, "학식 정보 조회 실패: HTTP " + response.code());
                }
            } catch (Exception e) {
                Log.e(TAG, "알림 데이터 조회 중 오류 발생", e);
            } finally {
                // 5. 다음 평일로 자동 재예약 및 PendingResult 종료
                MealNotificationScheduler.scheduleMealAlarm(context, finalReqCode, finalType, finalHour, finalMinute);
                pendingResult.finish();
            }
        });
    }
}
