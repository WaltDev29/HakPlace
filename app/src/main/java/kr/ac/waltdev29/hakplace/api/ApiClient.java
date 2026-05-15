package kr.ac.waltdev29.hakplace.api;

import kr.ac.waltdev29.hakplace.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;

    public static String getBaseUrl() {
        String baseUrl = "https://api.example.com/";
        try {
            if (BuildConfig.API_URL != null && !BuildConfig.API_URL.isEmpty()) {
                baseUrl = BuildConfig.API_URL;
                if (!baseUrl.endsWith("/")) {
                    baseUrl += "/";
                }
            }
        } catch (Throwable ignored) {}
        return baseUrl;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request req = chain.request().newBuilder()
                                .addHeader("Accept", "application/json")
                                .build();
                        return chain.proceed(req);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(getBaseUrl())
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
