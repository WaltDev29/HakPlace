package kr.ac.waltdev29.hakplace.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.ac.waltdev29.hakplace.MenuTodayActivity;
import kr.ac.waltdev29.hakplace.R;
import kr.ac.waltdev29.hakplace.api.models.DailyMeals;
import kr.ac.waltdev29.hakplace.api.models.MealSchema;

/**
 * NotificationHelper는 알림 채널 관리, 권한 상태 확인,
 * 그리고 조식/중식/석식을 포함한 학식 알림을 생성 및 발행하는 유틸리티 클래스입니다.
 */
public class NotificationHelper {
    public static final String CHANNEL_ID = "hakplace_meal_channel";
    public static final String CHANNEL_NAME = "학식 알림";
    public static final String CHANNEL_DESC = "매일 아침 전체 학식 및 중식/석식 메뉴 알림을 수신합니다.";

    public static final int NOTIFICATION_ID_ALL = 1001;
    public static final int NOTIFICATION_ID_LUNCH = 1002;
    public static final int NOTIFICATION_ID_DINNER = 1003;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(context.getString(R.string.notif_channel_desc));
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * 사용자의 기기 시스템 설정에서 이 앱의 알림이 허용되어 있는지 여부를 확인합니다.
     */
    public static boolean areNotificationsEnabled(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * 알림 권한이 꺼져 있을 때 사용자를 앱 알림 설정 화면으로 직접 안내합니다.
     */
    public static void openNotificationSettings(Context context) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent(context, MenuTodayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getActivity(context, 0, intent, flags);
    }

    private static String formatFoods(Context context, List<String> foods) {
        if (foods == null || foods.isEmpty()) {
            return context.getString(R.string.notif_no_service);
        }
        return TextUtils.join(", ", foods);
    }

    /**
     * 08:00 - 금일 조식, 중식, 석식을 하나의 BigTextStyle 카드로 통합 발행
     */
    public static void showAllMealsNotification(Context context, DailyMeals dailyMeals) {
        createNotificationChannel(context);
        if (!areNotificationsEnabled(context)) return;

        String datePattern = context.getString(R.string.date_format_notification);
        String dateStr = new SimpleDateFormat(datePattern, Locale.getDefault()).format(new Date());
        String title = context.getString(R.string.notif_daily_all_title, dateStr);

        StringBuilder sb = new StringBuilder();
        
        sb.append(context.getString(R.string.notif_breakfast_label)).append("\n");
        if (dailyMeals != null && dailyMeals.breakfast != null) {
            sb.append(formatFoods(context, dailyMeals.breakfast.foods));
        } else {
            sb.append(context.getString(R.string.notif_no_service));
        }
        sb.append("\n\n");

        sb.append(context.getString(R.string.notif_lunch_label)).append("\n");
        if (dailyMeals != null && dailyMeals.lunch != null) {
            sb.append(formatFoods(context, dailyMeals.lunch.foods));
        } else {
            sb.append(context.getString(R.string.notif_no_service));
        }
        sb.append("\n\n");

        sb.append(context.getString(R.string.notif_dinner_label)).append("\n");
        if (dailyMeals != null && dailyMeals.dinner != null) {
            sb.append(formatFoods(context, dailyMeals.dinner.foods));
        } else {
            sb.append(context.getString(R.string.notif_no_service));
        }

        String summaryText = context.getString(R.string.notif_daily_all_summary);
        if (dailyMeals != null && dailyMeals.lunch != null && dailyMeals.lunch.foods != null && !dailyMeals.lunch.foods.isEmpty()) {
            summaryText = context.getString(R.string.notif_daily_all_summary_lunch, dailyMeals.lunch.foods.get(0));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(summaryText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(sb.toString()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(createPendingIntent(context));

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ALL, builder.build());
        } catch (SecurityException ignored) {}
    }

    /**
     * 11:30 - 오늘의 중식 단독 알림 발행
     */
    public static void showLunchNotification(Context context, MealSchema lunch) {
        createNotificationChannel(context);
        if (!areNotificationsEnabled(context)) return;

        String title = context.getString(R.string.notif_lunch_title);
        String content = (lunch != null) ? formatFoods(context, lunch.foods) : context.getString(R.string.notif_lunch_no_service);

        StringBuilder bigText = new StringBuilder();
        bigText.append(content);
        if (lunch != null && lunch.avg_rating > 0) {
            bigText.append(String.format(Locale.getDefault(), "\n\n" + context.getString(R.string.notif_rating_format), lunch.avg_rating));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText.toString()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(createPendingIntent(context));

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_LUNCH, builder.build());
        } catch (SecurityException ignored) {}
    }

    /**
     * 17:30 - 오늘의 석식 단독 알림 발행
     */
    public static void showDinnerNotification(Context context, MealSchema dinner) {
        createNotificationChannel(context);
        if (!areNotificationsEnabled(context)) return;

        String title = context.getString(R.string.notif_dinner_title);
        String content = (dinner != null) ? formatFoods(context, dinner.foods) : context.getString(R.string.notif_dinner_no_service);

        StringBuilder bigText = new StringBuilder();
        bigText.append(content);
        if (dinner != null && dinner.avg_rating > 0) {
            bigText.append(String.format(Locale.getDefault(), "\n\n" + context.getString(R.string.notif_rating_format), dinner.avg_rating));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText.toString()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(createPendingIntent(context));

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DINNER, builder.build());
        } catch (SecurityException ignored) {}
    }
}
