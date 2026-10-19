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
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
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

    private static String formatFoods(List<String> foods) {
        if (foods == null || foods.isEmpty()) {
            return "운영 정보 없음";
        }
        return TextUtils.join(", ", foods);
    }

    /**
     * 08:00 - 금일 조식, 중식, 석식을 하나의 BigTextStyle 카드로 통합 발행
     */
    public static void showAllMealsNotification(Context context, DailyMeals dailyMeals) {
        createNotificationChannel(context);
        if (!areNotificationsEnabled(context)) return;

        String dateStr = new SimpleDateFormat("M월 d일 (E)", Locale.KOREAN).format(new Date());
        String title = "🍱 " + dateStr + " 오늘의 학식 메뉴";

        StringBuilder sb = new StringBuilder();
        
        sb.append("🌅 [조식]\n");
        if (dailyMeals != null && dailyMeals.breakfast != null) {
            sb.append(formatFoods(dailyMeals.breakfast.foods));
        } else {
            sb.append("운영 없음");
        }
        sb.append("\n\n");

        sb.append("☀️ [중식]\n");
        if (dailyMeals != null && dailyMeals.lunch != null) {
            sb.append(formatFoods(dailyMeals.lunch.foods));
        } else {
            sb.append("운영 없음");
        }
        sb.append("\n\n");

        sb.append("🌙 [석식]\n");
        if (dailyMeals != null && dailyMeals.dinner != null) {
            sb.append(formatFoods(dailyMeals.dinner.foods));
        } else {
            sb.append("운영 없음");
        }

        String summaryText = "조식, 중식, 석식 메뉴를 확인해 보세요!";
        if (dailyMeals != null && dailyMeals.lunch != null && dailyMeals.lunch.foods != null && !dailyMeals.lunch.foods.isEmpty()) {
            summaryText = "점심 대표: " + dailyMeals.lunch.foods.get(0);
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

        String title = "☀️ 오늘의 점심 메뉴 도착!";
        String content = (lunch != null) ? formatFoods(lunch.foods) : "금일 점심 운영이 없습니다.";

        StringBuilder bigText = new StringBuilder();
        bigText.append(content);
        if (lunch != null && lunch.avg_rating > 0) {
            bigText.append(String.format(Locale.KOREAN, "\n\n⭐ 평균 평점: %.1f / 5.0", lunch.avg_rating));
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

        String title = "🌙 오늘의 저녁 메뉴 도착!";
        String content = (dinner != null) ? formatFoods(dinner.foods) : "금일 저녁 운영이 없습니다.";

        StringBuilder bigText = new StringBuilder();
        bigText.append(content);
        if (dinner != null && dinner.avg_rating > 0) {
            bigText.append(String.format(Locale.KOREAN, "\n\n⭐ 평균 평점: %.1f / 5.0", dinner.avg_rating));
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
