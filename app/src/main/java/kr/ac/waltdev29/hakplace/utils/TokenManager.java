package kr.ac.waltdev29.hakplace.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * TokenManager는 Android KeyStore와 EncryptedSharedPreferences를 활용하여
 * JWT 토큰 및 사용자 민감 정보를 안전하게 암호화하여 저장/관리하는 싱글톤 유틸리티입니다.
 */
public class TokenManager {
    private static final String TAG = "TokenManager";
    private static final String PREF_NAME_SECURE = "hakplace_secure_prefs";
    private static final String PREF_NAME_LEGACY = "hakplace_prefs";

    private static final String KEY_STUDENT_ID = "student_id";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_AUTO_LOGIN = "auto_login";

    private static volatile TokenManager instance;
    private final SharedPreferences prefs;

    private TokenManager(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences securePrefs = null;

        try {
            // Android KeyStore 기반 MasterKey 생성 (AES256-GCM)
            MasterKey masterKey = new MasterKey.Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            securePrefs = EncryptedSharedPreferences.create(
                    appContext,
                    PREF_NAME_SECURE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            // KeyStore 손상 등의 특수 기기 환경 예외 처리
            Log.e(TAG, "EncryptedSharedPreferences 초기화 실패, Fallback 모드로 전환", e);
            securePrefs = appContext.getSharedPreferences(PREF_NAME_SECURE, Context.MODE_PRIVATE);
        }

        this.prefs = securePrefs;

        // 기존 비암호화 SharedPreferences(hakplace_prefs)에 저장된 데이터가 있다면 안전하게 마이그레이션 후 평문 데이터 삭제
        migrateLegacyPreferences(appContext);
    }

    public static TokenManager getInstance(Context context) {
        if (instance == null) {
            synchronized (TokenManager.class) {
                if (instance == null) {
                    instance = new TokenManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * 구버전 평문 SharedPreferences 데이터를 암호화 저장소로 복사하고 원본 평문 파일을 삭제합니다.
     */
    private void migrateLegacyPreferences(Context context) {
        try {
            SharedPreferences legacyPrefs = context.getSharedPreferences(PREF_NAME_LEGACY, Context.MODE_PRIVATE);
            if (legacyPrefs.contains(KEY_ACCESS_TOKEN) || legacyPrefs.contains(KEY_STUDENT_ID)) {
                String legacyToken = legacyPrefs.getString(KEY_ACCESS_TOKEN, null);
                String legacyStudentId = legacyPrefs.getString(KEY_STUDENT_ID, null);
                boolean legacyAutoLogin = legacyPrefs.getBoolean(KEY_AUTO_LOGIN, false);

                SharedPreferences.Editor editor = prefs.edit();
                if (legacyToken != null) editor.putString(KEY_ACCESS_TOKEN, legacyToken);
                if (legacyStudentId != null) editor.putString(KEY_STUDENT_ID, legacyStudentId);
                editor.putBoolean(KEY_AUTO_LOGIN, legacyAutoLogin);
                editor.apply();

                // 평문 데이터 유출 방지를 위해 기존 파일 내용 즉시 소거
                legacyPrefs.edit().clear().apply();
                Log.i(TAG, "구버전 평문 토큰 마이그레이션 및 이전 SharedPreferences 소거 완료");
            }
        } catch (Exception e) {
            Log.w(TAG, "Legacy SharedPreferences 마이그레이션 중 오류 발생", e);
        }
    }

    public void saveAuth(String studentId, String token, boolean autoLogin) {
        prefs.edit()
                .putString(KEY_STUDENT_ID, studentId)
                .putString(KEY_ACCESS_TOKEN, token)
                .putBoolean(KEY_AUTO_LOGIN, autoLogin)
                .apply();
    }

    public void saveStudentId(String studentId) {
        prefs.edit().putString(KEY_STUDENT_ID, studentId).apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getStudentId() {
        return prefs.getString(KEY_STUDENT_ID, null);
    }

    public boolean isAutoLogin() {
        return prefs.getBoolean(KEY_AUTO_LOGIN, false);
    }

    public boolean hasValidToken() {
        String token = getAccessToken();
        return token != null && !token.trim().isEmpty();
    }

    /**
     * 인증 토큰 및 자동로그인 플래그만 초기화 (학번 유지는 필요할 때 사용)
     */
    public void clearAuth() {
        prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .putBoolean(KEY_AUTO_LOGIN, false)
                .apply();
    }

    /**
     * 로그아웃 등 완전한 세션 초기화가 필요할 때 모든 로컬 민감 데이터를 소거
     */
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
