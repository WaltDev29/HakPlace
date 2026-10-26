package kr.ac.waltdev29.hakplace.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.work.WorkManager;

import java.util.Calendar;

import kr.ac.waltdev29.hakplace.receiver.MealNotificationReceiver;

/**
 * MealNotificationScheduler는 학식 알림 설정(ON/OFF)을 관리하고,
 * 평일(월~금) 지정 시각(08:00, 11:30, 17:30)에 AlarmManager를 통해 정확한 알람을 예약/취소하는 모듈입니다.
 */
public class MealNotificationScheduler {
    private static final String TAG = "MealNotifScheduler";
    public static final String PREF_NAME = "hakplace_notification_settings";

    public static final String PREF_KEY_DAILY_ALL = "notify_daily_all";
    public static final String PREF_KEY_LUNCH = "notify_lunch";
    public static final String PREF_KEY_DINNER = "notify_dinner";

    public static final String ACTION_MEAL_ALARM_PREFIX = "kr.ac.waltdev29.hakplace.ACTION_MEAL_ALARM_";
    public static final String EXTRA_TYPE = "extra_meal_type";
    public static final String EXTRA_TARGET_HOUR = "extra_target_hour";
    public static final String EXTRA_TARGET_MINUTE = "extra_target_minute";
    public static final String EXTRA_REQUEST_CODE = "extra_request_code";

    public static final String TYPE_DAILY_ALL = "type_daily_all";
    public static final String TYPE_LUNCH = "type_lunch";
    public static final String TYPE_DINNER = "type_dinner";

    public static final int REQ_CODE_DAILY_ALL = 2001;
    public static final int REQ_CODE_LUNCH = 2002;
    public static final int REQ_CODE_DINNER = 2003;

    // 시간 정의 (평일 08:00 전체, 11:30 점심, 17:30 저녁)
    public static final int HOUR_DAILY_ALL = 8;
    public static final int MINUTE_DAILY_ALL = 0;

    public static final int HOUR_LUNCH = 11;
    public static final int MINUTE_LUNCH = 30;

    public static final int HOUR_DINNER = 17;
    public static final int MINUTE_DINNER = 30;

    // 구버전 WorkManager 작업명 (클린업용)
    public static final String WORK_NAME_DAILY_ALL = "work_meal_daily_all";
    public static final String WORK_NAME_LUNCH = "work_meal_lunch";
    public static final String WORK_NAME_DINNER = "work_meal_dinner";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isDailyAllEnabled(Context context) {
        return getPrefs(context).getBoolean(PREF_KEY_DAILY_ALL, true);
    }

    public static void setDailyAllEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(PREF_KEY_DAILY_ALL, enabled).apply();
        if (enabled) {
            scheduleMealAlarm(context, REQ_CODE_DAILY_ALL, TYPE_DAILY_ALL, HOUR_DAILY_ALL, MINUTE_DAILY_ALL);
        } else {
            cancelMealAlarm(context, REQ_CODE_DAILY_ALL, TYPE_DAILY_ALL);
        }
    }

    public static boolean isLunchEnabled(Context context) {
        return getPrefs(context).getBoolean(PREF_KEY_LUNCH, true);
    }

    public static void setLunchEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(PREF_KEY_LUNCH, enabled).apply();
        if (enabled) {
            scheduleMealAlarm(context, REQ_CODE_LUNCH, TYPE_LUNCH, HOUR_LUNCH, MINUTE_LUNCH);
        } else {
            cancelMealAlarm(context, REQ_CODE_LUNCH, TYPE_LUNCH);
        }
    }

    public static boolean isDinnerEnabled(Context context) {
        return getPrefs(context).getBoolean(PREF_KEY_DINNER, true);
    }

    public static void setDinnerEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(PREF_KEY_DINNER, enabled).apply();
        if (enabled) {
            scheduleMealAlarm(context, REQ_CODE_DINNER, TYPE_DINNER, HOUR_DINNER, MINUTE_DINNER);
        } else {
            cancelMealAlarm(context, REQ_CODE_DINNER, TYPE_DINNER);
        }
    }

    /**
     * 다음 '평일(월~금)' 타겟 시각(hour:minute)의 에포크 밀리초(Epoch Millis)를 계산합니다.
     * - 오늘이 평일이고 목표 시각 전이면 오늘 해당 시각 반환
     * - 오늘 해당 시각이 지났거나 주말이면 다음 평일(월요일 등)의 해당 시각 반환
     */
    public static long getNextWeekdayTriggerMillis(int targetHour, int targetMinute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, targetHour);
        cal.set(Calendar.MINUTE, targetMinute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar now = Calendar.getInstance();

        // 오늘 목표 시각이 이미 지났다면 하루 뒤로 이동
        if (now.compareTo(cal) >= 0) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 평일(월~금)이 될 때까지 요일 보정
        // Calendar.SUNDAY = 1, Calendar.SATURDAY = 7
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY) {
            cal.add(Calendar.DAY_OF_MONTH, 2); // 토요일 -> 월요일
        } else if (dayOfWeek == Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_MONTH, 1); // 일요일 -> 월요일
        }

        return cal.getTimeInMillis();
    }

    /**
     * AlarmManager에 평일 알림을 등록합니다. Doze(절전) 모드에서도 지정 시각에 깨어나도록 보장합니다.
     */
    public static void scheduleMealAlarm(Context context, int requestCode, String type, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        long triggerAtMillis = getNextWeekdayTriggerMillis(hour, minute);

        Intent intent = new Intent(context, MealNotificationReceiver.class);
        intent.setAction(ACTION_MEAL_ALARM_PREFIX + type);
        intent.putExtra(EXTRA_TYPE, type);
        intent.putExtra(EXTRA_TARGET_HOUR, hour);
        intent.putExtra(EXTRA_TARGET_MINUTE, minute);
        intent.putExtra(EXTRA_REQUEST_CODE, requestCode);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        try {
            // Android 12 (API 31)+ 에서 exact alarm 권한 확인 후 등록
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                } else {
                    // 권한 미부여 시 배터리 최적화 내에서 최대한 정시에 깨우는 allowWhileIdle 사용
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
            Log.i(TAG, "알림 예약 완료: type=" + type + ", triggerTime=" + new java.util.Date(triggerAtMillis));
        } catch (SecurityException se) {
            Log.w(TAG, "정확한 알람 권한 제한으로 인해 기본 알람으로 대체 등록합니다: " + se.getMessage());
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    /**
     * 등록된 AlarmManager 일정을 취소합니다.
     */
    public static void cancelMealAlarm(Context context, int requestCode, String type) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, MealNotificationReceiver.class);
        intent.setAction(ACTION_MEAL_ALARM_PREFIX + type);

        int flags = PendingIntent.FLAG_NO_CREATE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.i(TAG, "알림 취소 완료: type=" + type);
        }
    }

    /**
     * 구버전 WorkManager에 남아있을 수 있는 워커들을 모두 취소하여 새벽 00:30 중복 실행을 방지합니다.
     */
    public static void cleanUpLegacyWorkManager(Context context) {
        try {
            WorkManager wm = WorkManager.getInstance(context.getApplicationContext());
            wm.cancelUniqueWork(WORK_NAME_DAILY_ALL);
            wm.cancelUniqueWork(WORK_NAME_LUNCH);
            wm.cancelUniqueWork(WORK_NAME_DINNER);
            wm.cancelAllWorkByTag("hakplace_meal_notification");
            Log.i(TAG, "구버전 WorkManager 잔여 큐 정리 완료");
        } catch (Exception e) {
            Log.w(TAG, "WorkManager 정리 중 무시 가능한 예외", e);
        }
    }

    /**
     * 활성화된 모든 알림을 평일 일정에 맞춰 일괄 스케줄링하고, 구버전 워커를 정리합니다.
     */
    public static void scheduleAll(Context context) {
        // 기존 큐 정리
        cleanUpLegacyWorkManager(context);

        if (isDailyAllEnabled(context)) {
            scheduleMealAlarm(context, REQ_CODE_DAILY_ALL, TYPE_DAILY_ALL, HOUR_DAILY_ALL, MINUTE_DAILY_ALL);
        }
        if (isLunchEnabled(context)) {
            scheduleMealAlarm(context, REQ_CODE_LUNCH, TYPE_LUNCH, HOUR_LUNCH, MINUTE_LUNCH);
        }
        if (isDinnerEnabled(context)) {
            scheduleMealAlarm(context, REQ_CODE_DINNER, TYPE_DINNER, HOUR_DINNER, MINUTE_DINNER);
        }
    }
}
