package kr.ac.waltdev29.hakplace.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.waltdev29.hakplace.utils.MealNotificationScheduler;

/**
 * BootReceiver는 기기 재부팅 완료(ACTION_BOOT_COMPLETED) 시 호출되어
 * 기존에 활성화된 식단 알람 일정을 AlarmManager에 다시 등록합니다.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i(TAG, "기기 재부팅 감지: 식단 알림 스케줄 재등록 진행");
            MealNotificationScheduler.scheduleAll(context);
        }
    }
}
