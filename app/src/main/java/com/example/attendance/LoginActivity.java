package com.example.attendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.attendance.model.Account;
import com.example.attendance.model.AccountRequest;
import com.example.attendance.model.CustomToast;
import com.example.attendance.model.Utils;
import com.example.attendance.service.ApiClient;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "LoginActivity";
    EditText editPhone, editPassWord;
    RelativeLayout btnLogin;
    TextView inFor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "deviceId: "+getDeviceId(this));
        initView();
        setOnClick();
    }
    private void initView() {
        editPhone = findViewById(R.id.phone);
        editPassWord = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btn_login);
        inFor = findViewById(R.id.wifiInfo);
    }

    private void setOnClick() {
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_login){
            getFormDetail();
        }
    }

    private void getFormDetail() {
        String phoneNumber = editPhone.getText().toString().trim();
        String password = editPassWord.getText().toString().trim();
        String mobileId = getDeviceId(this);
        AccountRequest accountRequest = new AccountRequest(phoneNumber,password,mobileId);
        checkLogin(accountRequest);
    }

    private void checkLogin(AccountRequest accountRequest) {
        ApiClient.postLogin(accountRequest, new ApiClient.ApiResponseListener() {
            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                Account account = gson.fromJson(response, Account.class);
                int role = account.getRole();
                Utils.sharedPreferences(getApplicationContext(),"accountId",String.valueOf(account.getId()));
                getCurrentEmployeeId(account.getId());
                redirect(role);
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error.contains("connect")){
                            CustomToast.showToast(LoginActivity.this,"Lỗi kết nối",1000);
                        }
                        else if (error.contains("Account not found")){
                            CustomToast.showToast(LoginActivity.this,"Sai tài khoản hoặc mật khẩu",1000);
                        }
                        else if (error.contains("Invalid mobileId")){
                            CustomToast.showToast(LoginActivity.this,"Thiết bị không được phép truy cập",1000);
                        }
                        else {
                            CustomToast.showToast(LoginActivity.this,"Lỗi",1000);
                        }
                    }
                });
            }
        });
    }

    private void getCurrentEmployeeId(Long id) {
        Map<String, Long> params = new HashMap<>();
        params.put("accountId", id);
        ApiClient.getEmployeeId(params, new ApiClient.ApiResponseListener() {
            @Override
            public void onSuccess(String response) {
                try {
                    long employeeId = Long.parseLong(response);
                    Utils.sharedPreferences(getApplicationContext(),"employeeId",String.valueOf(employeeId));
                } catch (NumberFormatException e) {
                    Log.d(TAG,"Error parsing response: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(String error) {
                Log.d(TAG,"API request failed: " + error);
            }
        });
    }

    private void redirect(int role) {
        if (role == 2){
            Intent intent = new Intent(this, Splash.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CustomToast.showToast(LoginActivity.this,"Không có quyền truy cập",1000);
                }
            });
        }
    }
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}