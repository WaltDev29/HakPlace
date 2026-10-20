package kr.ac.waltdev29.hakplace.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import kr.ac.waltdev29.hakplace.worker.MealNotificationWorker;

/**
 * MealNotificationScheduler는 학식 알림 설정(ON/OFF)을 관리하고,
 * 각 시간대별(08:00, 11:30, 17:30) 백그라운드 WorkManager 일정을 예약/취소하는 모듈입니다.
 */
public class MealNotificationScheduler {
    public static final String PREF_NAME = "hakplace_notification_settings";

    public static final String PREF_KEY_DAILY_ALL = "notify_daily_all";
    public static final String PREF_KEY_LUNCH = "notify_lunch";
    public static final String PREF_KEY_DINNER = "notify_dinner";

    public static final String WORK_NAME_DAILY_ALL = "work_meal_daily_all";
    public static final String WORK_NAME_LUNCH = "work_meal_lunch";
    public static final String WORK_NAME_DINNER = "work_meal_dinner";

    public static final String TYPE_DAILY_ALL = "type_daily_all";
    public static final String TYPE_LUNCH = "type_lunch";
    public static final String TYPE_DINNER = "type_dinner";

    // 시간 정의
    public static final int HOUR_DAILY_ALL = 8;
    public static final int MINUTE_DAILY_ALL = 0;

    public static final int HOUR_LUNCH = 11;
    public static final int MINUTE_LUNCH = 30;

    public static final int HOUR_DINNER = 17;
    public static final int MINUTE_DINNER = 30;

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isDailyAllEnabled(Context context) {
        return getPrefs(context).getBoolean(PREF_KEY_DAILY_ALL, true);
    }

    public static void setDailyAllEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(PREF_KEY_DAILY_ALL, enabled).apply();
        if (enabled) {
            scheduleWork(context, WORK_NAME_DAILY_ALL, TYPE_DAILY_ALL, HOUR_DAILY_ALL, MINUTE_DAILY_ALL);
        } else {
            cancelWork(context, WORK_NAME_DAILY_ALL);
        }
    }

    public static boolean isLunchEnabled(Context context) {
        return getPrefs(context).getBoolean(PREF_KEY_LUNCH, true);
    }

    public static void setLunchEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(PREF_KEY_LUNCH, enabled).apply();
        if (enabled) {
            scheduleWork(context, WORK_NAME_LUNCH, TYPE_LUNCH, HOUR_LUNCH, MINUTE_LUNCH);
        } else {
            cancelWork(context, WORK_NAME_LUNCH);
        }
    }

    public static boolean isDinnerEnabled(Context context) {
        return getPrefs(context).getBoolean(PREF_KEY_DINNER, true);
    }

    public static void setDinnerEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(PREF_KEY_DINNER, enabled).apply();
        if (enabled) {
            scheduleWork(context, WORK_NAME_DINNER, TYPE_DINNER, HOUR_DINNER, MINUTE_DINNER);
        } else {
            cancelWork(context, WORK_NAME_DINNER);
        }
    }

    /**
     * 현재 시간 기준 다음 타겟 시각(hour:minute)까지의 지연 시간(ms)을 계산합니다.
     */
    public static long calculateInitialDelay(int targetHour, int targetMinute) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, targetMinute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        if (now.after(target)) {
            // 이미 오늘 해당 시각이 지났다면 다음 날로 설정
            target.add(Calendar.DAY_OF_MONTH, 1);
        }
        return target.getTimeInMillis() - now.getTimeInMillis();
    }

    /**
     * 특정 시간대의 알림 워커를 WorkManager에 등록합니다.
     */
    public static void scheduleWork(Context context, String workName, String notificationType, int hour, int minute) {
        long delay = calculateInitialDelay(hour, minute);

        Data inputData = new Data.Builder()
                .putString(MealNotificationWorker.KEY_TYPE, notificationType)
                .putInt(MealNotificationWorker.KEY_TARGET_HOUR, hour)
                .putInt(MealNotificationWorker.KEY_TARGET_MINUTE, minute)
                .putString(MealNotificationWorker.KEY_WORK_NAME, workName)
                .build();

        // 네트워크가 연결되어 있을 때만 실행하도록 제약조건 부여
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MealNotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag("hakplace_meal_notification")
                .build();

        WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
                workRequest
        );
    }

    public static void cancelWork(Context context, String workName) {
        WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork(workName);
    }

    /**
     * 활성화된 모든 알림(08:00, 11:30, 17:30)을 일괄 스케줄링합니다.
     */
    public static void scheduleAll(Context context) {
        if (isDailyAllEnabled(context)) {
            scheduleWork(context, WORK_NAME_DAILY_ALL, TYPE_DAILY_ALL, HOUR_DAILY_ALL, MINUTE_DAILY_ALL);
        }
        if (isLunchEnabled(context)) {
            scheduleWork(context, WORK_NAME_LUNCH, TYPE_LUNCH, HOUR_LUNCH, MINUTE_LUNCH);
        }
        if (isDinnerEnabled(context)) {
            scheduleWork(context, WORK_NAME_DINNER, TYPE_DINNER, HOUR_DINNER, MINUTE_DINNER);
        }
    }
}
