package com.example.attendance.service;

import com.example.attendance.model.AccountRequest;
import com.example.attendance.model.AttendanceRequest;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    private static final Gson gson = new Gson();

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build();

    private static void postRequest(String url, Object object, final ApiResponseListener listener) {
        String jsonBody = gson.toJson(object);
        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.get("application/json; charset=utf-8")
        );
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    listener.onSuccess(response.body().string());
                } else {
                    assert response.body() != null;
                    listener.onFailure(response.body().string());
                }
            }
        });
    }

    private static void getRequest(String baseUrl, Map<String, Long> params, final ApiResponseListener listener) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(baseUrl)).newBuilder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Long> entry : params.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFailure("connect fail: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    listener.onSuccess(response.body().string());
                } else {
                    assert response.body() != null;
                    listener.onFailure(response.body().string());
                }
            }
        });
    }

    public static void postLogin(AccountRequest accountRequest, ApiResponseListener listener) {
        postRequest(Constants.LOGIN_URL, accountRequest, listener);
    }

    public static void postAttendance(AttendanceRequest attendanceRequest, ApiResponseListener listener) {
        postRequest(Constants.ATTENDANCE_URL, attendanceRequest, listener);
    }

    public static void getEmployeeId(Map<String, Long> params,ApiResponseListener listener){
        getRequest(Constants.EMPLOYEE_ID_URL,params,listener);
    }

    public static void getAttendanceHistory(ApiResponseListener listener) {
        getRequest(Constants.ATTENDANCE_URL,null, listener);
    }

    public interface ApiResponseListener {
        void onSuccess(String response);
        void onFailure(String error);
    }
}
