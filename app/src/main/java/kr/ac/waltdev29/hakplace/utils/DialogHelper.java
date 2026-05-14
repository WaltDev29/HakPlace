package kr.ac.waltdev29.hakplace.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.google.android.material.button.MaterialButton;

import kr.ac.waltdev29.hakplace.R;

public class DialogHelper {

    public interface OnConfirmListener {
        void onConfirm();
    }

    public interface OnDialogListener {
        void onConfirm();
        void onCancel();
    }

    /**
     * 기본 알림 모달 (확인 버튼 1개)
     */
    public static void showNotificationDialog(Context context, String title, String message, OnConfirmListener listener) {
        showNotificationDialog(context, R.drawable.ic_notifications, title, message, listener);
    }

    public static void showNotificationDialog(Context context, @DrawableRes int iconRes, String title, String message, OnConfirmListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_notification, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageView ivIcon = view.findViewById(R.id.ivDialogIcon);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);

        ivIcon.setImageResource(iconRes);
        tvTitle.setText(title);
        tvMessage.setText(message);

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onConfirm();
        });

        dialog.show();
    }

    /**
     * 확인/취소 선택 모달 (버전 2개 버튼)
     */
    public static void showConfirmDialog(Context context, String title, String message, OnDialogListener listener) {
        showConfirmDialog(context, R.drawable.ic_help, title, message, listener);
    }

    public static void showConfirmDialog(Context context, @DrawableRes int iconRes, String title, String message, OnDialogListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_confirm, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageView ivIcon = view.findViewById(R.id.ivDialogIcon);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);

        ivIcon.setImageResource(iconRes);
        tvTitle.setText(title);
        tvMessage.setText(message);

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onCancel();
        });

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onConfirm();
        });

        dialog.show();
    }

    /**
     * 시스템 오류 등 치명적 에러용 (확인 시 Activity 종료 가능)
     */
    public static void showSystemErrorDialog(android.app.Activity activity, String message) {
        showNotificationDialog(activity, R.drawable.ic_close, "시스템 오류", message, activity::finish);
    }
}
