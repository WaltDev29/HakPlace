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
            return "알 수 없는 오류가 발생했습니다.";
        } catch (Exception e) {
            return "오류 응답을 처리하는 중 문제가 발생했습니다.";
        }
    }
}
