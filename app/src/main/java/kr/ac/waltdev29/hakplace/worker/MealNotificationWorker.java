package kr.ac.waltdev29.hakplace.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * @deprecated 학식 알림 시스템이 AlarmManager 기반(MealNotificationReceiver)으로 전환되었습니다.
 * 기존 기기의 WorkManager 데이터베이스에 남아있던 작업이 깨어났을 때
 * 알림 발행이나 재예약 없이 즉시 성공 처리하고 소멸되도록 no-op 처리합니다.
 */
@Deprecated
public class MealNotificationWorker extends Worker {
    private static final String TAG = "MealNotificationWorker";

    public MealNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "구버전 MealNotificationWorker 호출 감지: 안전하게 종료합니다.");
        return Result.success();
    }
}
