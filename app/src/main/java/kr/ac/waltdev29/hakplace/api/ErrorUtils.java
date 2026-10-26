package kr.ac.waltdev29.hakplace.api;

import okhttp3.ResponseBody;
import org.json.JSONObject;

public class ErrorUtils {
    public static String parseError(ResponseBody responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody.string());
            if (jsonObject.has("detail")) {
                Object detail = jsonObject.get("detail");
                if (detail instanceof String) {
                    return (String) detail;
                } else if (detail instanceof org.json.JSONArray) {
                    // For validation errors
                    return ((org.json.JSONArray) detail).getJSONObject(0).getString("msg");
                }
            }
            boolean isJa = java.util.Locale.getDefault().getLanguage().equals("ja");
            return isJa ? "不明なエラーが発生しました。" : "알 수 없는 오류가 발생했습니다.";
        } catch (Exception e) {
            boolean isJa = java.util.Locale.getDefault().getLanguage().equals("ja");
            return isJa ? "エラー応答の処理中に問題が発生しました。" : "오류 응답을 처리하는 중 문제가 발생했습니다.";
        }
    }
}
